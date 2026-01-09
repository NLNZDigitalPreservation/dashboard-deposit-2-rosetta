import enum
import logging
from queue import Queue
import threading
from attr import dataclass
import requests
from datetime import datetime as dt

from app.fixity.fixity_data_store import FixityStoreAbstract
from common.metadata import FixityResultCode

from app.worker.azure_common import DurableSession, OrchestratorTransaction, SessionStatus, query_status


class AzureDurableExecutorAbstract:
    def __init__(self, args, service_url):
        self.args = args
        self.max_worker_timeout_seconds = args.max_worker_timeout_hours * 3600
        self.azure_func = f"{args.fixity_worker_url}/{service_url}"
        self.is_running = threading.Event()
        self.queue_db_cache = Queue()
        self.executor_thread = threading.Thread(target=self._event_loop, daemon=True)
        self.executor_thread.start()

    def close(self):
        self.is_running.set()
        self.executor_thread.join(timeout=30.0)

    def close_after_finished(self):
        # Waitting for all the jobs
        while True:
            if self.queue_db_cache.empty():
                break
            else:
                self.is_running.wait(3.0)
        self.close()

    def submit_job(self, entity):
        if self.is_running.is_set():
            return False, "The executor is closed"

        rsp = requests.post(self.azure_func, json=entity)
        if not rsp.ok:
            err = f"Durable function response: {rsp.status_code} {rsp.text}"
            logging.error(err)
            return self.gen_result_entity(entity=entity, state=FixityResultCode.UNKNOWN_ERROR)

        rsp_json = rsp.json()

        transaction = OrchestratorTransaction(**rsp_json)

        session = DurableSession(id=rsp_json["id"], tstamp=dt.now().timestamp(), transaction=transaction, input=entity, output=None)
        session.event = threading.Event()
        self.queue_db_cache.put(session)

        # wait until the event is set
        session.event.wait(timeout=self.max_worker_timeout_seconds)

        # Purge the history from the durable function
        requests.delete(transaction.purgeHistoryDeleteUri)

        return session.output

    def gen_result_entity(self, entity, state, desc):
        pass

    def _polling_one_round(self):
        # Move sessions from queue to LMDB
        q_size = self.queue_db_cache.qsize()
        for _ in range(q_size):
            if self.is_running.is_set():
                break

            if self.queue_db_cache.empty():
                break

            session: DurableSession = self.queue_db_cache.get()

            # Check for timeout
            cur = dt.now().timestamp()
            if cur - session.tstamp > self.max_worker_timeout_seconds:
                logging.error(f"Session timeout: {session.id} {session.input}")
                session.output = self.gen_result_entity(session.input, FixityResultCode.WORKER_TIMEOUT.value, "Worker timeout")
                session.event.set()
                continue

            status, output = query_status(transaction=session.transaction)
            if status in (SessionStatus.running, SessionStatus.system_error):
                self.queue_db_cache.put(session)
                continue
            elif status is SessionStatus.not_exist:
                logging.error(f"Session not exist, may be purged: {session.id} {session.input}")
                session.output = self.gen_result_entity(session.input, FixityResultCode.WORKER_FAILED.value, "Worker session not exist")
            elif status is SessionStatus.success:
                session.output = output
            elif status is SessionStatus.failed:
                session.output = self.gen_result_entity(session.input, FixityResultCode.WORKER_FAILED, output.get("message", None))
            else:
                logging.error(f"Unknown session status: {status} for session {session.id}")
                session.output = self.gen_result_entity(session.input, FixityResultCode.UNKNOWN_ERROR, "Unknown status code")

            session.event.set()

    def _event_loop(self):
        while not self.is_running.is_set():
            try:
                self._polling_one_round()
            except Exception as ex:
                logging.error("Exception in polling_one_round")
                logging.exception(ex)
            finally:
                self.is_running.wait(1.0)
