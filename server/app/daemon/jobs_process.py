from collections import defaultdict
from datetime import datetime, time
import logging
from pathlib import Path
import threading
from app.domain import *
from app.domain.services_job import ServicesJob
from common.metadata import EnumDepositJobStage, EnumDepositJobState


class PipelineJobsProcess:
    def __init__(self, args, service_job):
        self.args = args
        self.service_job: ServicesJob = service_job
        self.is_running = threading.Event()
        self._event_loop = threading.Thread(target=self.event_loop)
        self._event_loop.start()

    def close(self):
        self.is_running.set()
        self._event_loop.join(timeout=5.0)

    def event_loop(self):
        while not self.is_running.is_set():
            try:
                self.pipeline()
            finally:
                self.is_running.wait(1.0)

    def pipeline(self):
        logging.debug("On timer heartbeat: pipeline.")

        if self.is_global_paused():
            logging.info("Skip the paused timeslot in pipeline.")
            return

        all_jobs: list[DepositJob] = list(DepositJob.select())
        if not all_jobs:
            return

        # Group jobs by flow setting ID
        all_job_groups = defaultdict(list)
        for job in all_jobs:
            flow_setting_id = job.appliedFlowSettingId
            all_job_groups[flow_setting_id].append(job)

        for flow_setting_id, jobs in all_job_groups.items():
            try:
                flow_setting: FlowSetting = FlowSetting.get_or_none(flow_setting_id)
                if flow_setting is None:
                    self.service_job.handle_flow_missing_jobs(flow_setting_id, jobs)
                    continue

                if not flow_setting.is_enabled():
                    logging.warning(
                        "Disabled Material Flow: %s %s",
                        flow_setting.id,
                        flow_setting.materialFlowName,
                    )
                    continue

                deposit_account = DepositAccount.get_or_none(
                    DepositAccount.id == flow_setting.depositAccountId
                )
                if deposit_account is None:
                    logging.error(
                        "The related deposit account does not exist: %s",
                        flow_setting.depositAccountId,
                    )
                    continue

                count_running = sum(
                    1
                    for job in jobs
                    if job.stage == EnumDepositJobStage.DEPOSIT.value
                    and job.state == EnumDepositJobState.RUNNING.value
                )

                now = datetime.now()
                now_day = now.weekday()  # Monday=0 .. Sunday=6
                max_concurrency_jobs = flow_setting.get_weekly_max_concurrency()[
                    now_day
                ]

                logging.debug(
                    "Now: %s, day: %s, maxConcurrencyJobs: %s, countRunning: %s",
                    now.isoformat(),
                    now_day,
                    max_concurrency_jobs,
                    count_running,
                )

                for job in jobs:
                    try:
                        if job.get_state() == EnumDepositJobState.PAUSED:
                            logging.debug("Skip the Paused job: %s", job.id)
                            continue

                        if self.service_job.handle_inactive_pruning(flow_setting, job):
                            continue

                        injection_path = Path(flow_setting.rootPath) / job.injectionPath
                        if not injection_path.is_dir():
                            logging.info(
                                "The original directory does not exist: %s %s",
                                job.id,
                                job.injectionPath,
                            )
                            continue

                        # Only launch the deposit task when Rosetta is idle
                        if count_running < max_concurrency_jobs:
                            if self.service_job.handle_deposit(
                                deposit_account, flow_setting, injection_path, job
                            ):
                                count_running += 1

                        self.service_job.handle_polling_status(deposit_account, job)
                        self.service_job.handle_finalize(
                            flow_setting, injection_path, job
                        )
                        self.service_job.handle_history_pruning(
                            flow_setting, injection_path, job
                        )

                    except Exception as ex:
                        logging.error(
                            "Failed to process job: %s",
                            job.get_id(),
                            exc_info=ex,
                        )

                jobs.clear()

            except Exception as ex:
                logging.error(
                    "Failed to process flow: %s",
                    flow_setting_id,
                    exc_info=ex,
                )

        all_job_groups.clear()
        all_jobs.clear()
