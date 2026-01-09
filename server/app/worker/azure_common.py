import enum
import logging
from queue import Queue
import threading
from typing import Optional
from attr import dataclass
import requests
from datetime import datetime, timedelta
from app.fixity.fixity_data_store import FixityStoreAbstract
from common.metadata import FixityResultCode, FixityResultOptions
from common.progress_bar import ProgressBar


@dataclass
class OrchestratorTransaction:
    id: str
    statusQueryGetUri: str
    sendEventPostUri: str
    terminatePostUri: str
    # rewindPostUri: str
    purgeHistoryDeleteUri: str
    restartPostUri: str
    suspendPostUri: str
    resumePostUri: str


@dataclass
class DurableSession:
    id: str
    tstamp: float
    transaction: OrchestratorTransaction
    input: Optional[dict] = None
    ret_code: Optional[int] = None
    output: Optional[dict | str] = None

    def debug(self):
        logging.debug(f"{self.ret_code} {self.output}")

    def info(self):
        logging.info(f"{self.ret_code} {self.output}")

    def error(self):
        logging.error(f"{self.ret_code} {self.output}")

    def commit_result(self, ret_code: int, output: dict | str):
        self.ret_code = ret_code
        self.output = output
        if ret_code != FixityResultCode.SUCCESS:
            self.error()
        else:
            self.info()


# runtimeStatus Value	Description
# Pending	The orchestration has been scheduled but hasn't started yet.
# Running	The orchestration is currently executing.
# Completed	The orchestration finished successfully.
# ContinuedAsNew	The orchestration restarted itself with new input.
# Failed	The orchestration encountered an unhandled exception.
# Terminated	The orchestration was manually terminated.
# Canceled	The orchestration was canceled (e.g., via timeout or manual action).
class RuntimeStatus(enum.Enum):
    pending = "Pending"
    running = "Running"
    completed = "Completed"
    continued_as_new = "ContinuedAsNew"
    failed = "Failed"
    terminated = "Terminated"
    canceled = "Canceled"


class SessionStatus(enum.Enum):
    success = 0
    running = 1
    failed = 2
    not_exist = 3
    system_error = 9


def query_status(transaction):
    try:
        rsp = requests.get(transaction.statusQueryGetUri)
        if rsp.status_code == 404:
            logging.error(f"Session not exist:{transaction.statusQueryGetUri}")
            return SessionStatus.not_exist, None
        elif not rsp.ok:
            logging.error(f"Failed to query status:{rsp.status_code} {rsp.text}")
            return SessionStatus.failed, None

        status_query_json = rsp.json()
        runtime_status = status_query_json["runtimeStatus"]
        if runtime_status in [
            RuntimeStatus.pending.value,
            RuntimeStatus.continued_as_new.value,
        ]:
            logging.debug(f"Continue: {transaction.statusQueryGetUri}, runtime_status={runtime_status}")
            return SessionStatus.running, None

        if runtime_status in [
            RuntimeStatus.failed.value,
            RuntimeStatus.terminated.value,
            RuntimeStatus.canceled.value,
        ]:
            logging.error(f"The durable function was {runtime_status}")
            return SessionStatus.failed, status_query_json.get("output", None)

        if runtime_status in [RuntimeStatus.running.value]:
            return SessionStatus.running, None

        if runtime_status in [RuntimeStatus.completed.value]:
            return SessionStatus.success, status_query_json.get("output", None)
    except Exception as ex:
        logging.error(f"Failed to query status for {transaction.statusQueryGetUri}")
        logging.exception(ex)
        return SessionStatus.system_error, str(ex)


class DurableFunctionWorker:
    def __init__(self, args, worker_api_url: str, data_store):
        self.worker_api_url = f"{args.fixity_worker_url}/{worker_api_url}"
        self.max_workers = threading.Semaphore(args.max_workers)
        self.max_worker_timeout = timedelta(hours=args.max_worker_timeout_hours)
        self.blob_url_prefix = args.blob_url_prefix
        self.data_store: FixityStoreAbstract = data_store
        self.is_running = threading.Event()
        self.is_finalizing = False
        self.sessions = {}
        self.lock = threading.Lock()
        self.event_loop = threading.Thread(target=self._event_loop, daemon=True)
        self.event_loop.start()

    def close(self):
        self.is_running.set()
        self.data_store.close()
        self.sessions.clear()
        if self.event_loop.is_alive():
            self.event_loop.join(timeout=3.0)

    def join(self):
        self.is_finalizing = True
        self.event_loop.join()

    def join_prefix_uri(self, data):
        index_location: Optional[str] = data.get("index_location", None)
        if index_location is None:
            return data
        if index_location.startswith("/"):
            data["index_location"] = f"{self.blob_url_prefix}{index_location}"
        else:
            data["index_location"] = f"{self.blob_url_prefix}/{index_location}"
        return data

    def submit_job(self, datasets):
        self.max_workers.acquire()
        req_body = [self.join_prefix_uri(data) for data in datasets]
        rsp = requests.post(self.worker_api_url, json=req_body)
        if not rsp.ok:
            err = f"Durable function response: {rsp.status_code} {rsp.text}"
            logging.error(err)
            self.data_store.persist_failed_dataset(dataset=session.input, state=FixityResultCode.WORKER_FAILED, desc=err)
            return False
        transaction = OrchestratorTransaction(**rsp.json())
        session = DurableSession(id=transaction.id, tstamp=datetime.now(), transaction=transaction, input=datasets)
        with self.lock:
            self.sessions[session.id] = session
        return True

    def _polling_worker_executor(self):
        try:
            with self.lock:
                keys = list(self.sessions.keys())

            for key in keys:
                if self.is_running.is_set():
                    break

                with self.lock:
                    session: DurableSession = self.sessions.get(key)
                if session is None:
                    continue

                status, output = query_status(transaction=session.transaction)
                if status in (SessionStatus.running, SessionStatus.system_error):
                    continue

                if datetime.now() - session.tstamp > self.max_worker_timeout:
                    session.commit_result(FixityResultCode.WORKER_TIMEOUT, FixityResultOptions[FixityResultCode.WORKER_TIMEOUT])
                elif status is SessionStatus.not_exist:
                    session.commit_result(FixityResultCode.WORKER_FAILED, f"Session not exist, may be purged: {session.id}")
                elif status is SessionStatus.success:
                    session.commit_result(FixityResultCode.SUCCESS, output)
                elif status is SessionStatus.failed:
                    session.commit_result(FixityResultCode.WORKER_FAILED, output.get("message", None))
                else:
                    session.commit_result(FixityResultCode.UNKNOWN_ERROR, "Unknown status code")

                # Purge the history from the durable function
                requests.delete(session.transaction.purgeHistoryDeleteUri)
                with self.lock:
                    if key in self.sessions:
                        del self.sessions[key]

                if session.ret_code == FixityResultCode.SUCCESS:
                    self.data_store.persist_dataset(output)
                else:
                    self.data_store.persist_failed_dataset(dataset=session.input, state=session.ret_code, desc=output)
        except Exception as ex:
            logging.error(f"Error polling worker executor: {ex}")
        finally:
            self.is_running.wait(timeout=0.5)

    def _event_loop(self):
        while not self.is_running.is_set():
            with self.lock:
                if self.is_finalizing and len(self.sessions.keys()) == 0:
                    break

            try:
                self._polling_worker_executor()
            except Exception as ex:
                logging.error(f"Error polling worker executor: {ex}")
            finally:
                self.is_running.wait(timeout=0.5)
