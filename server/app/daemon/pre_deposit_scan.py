import os
import threading
import glob
from app.domain.services_job import ServicesJob


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
                self.handle_ingest()
            finally:
                self.is_running.wait(1.0)

    def handle_ingest(self):
        all_flow_settings = self.repo_flow_setting.get_all()
        for flow_setting in all_flow_settings:
            if self.is_running.is_set():
                self.log.warning(f"Pipeline is not running")
                break

            if not flow_setting.enabled:
                self.log.warning(
                    f"Disabled Material Flow: {flow_setting.id} {flow_setting.materialFlowId}"
                )
                continue

            root_path = flow_setting.rootPath
            if not os.path.exists(root_path):
                self.log.error(f"Root path does not exist: {root_path}")
                continue

            # List directories in the root path
            for sub_folder_name in os.listdir(root_path):
                sub_folder_full_path = os.path.join(root_path, sub_folder_name)

                if not os.path.isdir(sub_folder_full_path):
                    continue

                if sub_folder_full_path in self.processing_jobs:
                    self.log.debug(
                        f"Ignore the [processing] subfolder: {sub_folder_full_path}"
                    )
                    continue

                # Check for "done" file
                if os.path.exists(os.path.join(sub_folder_full_path, "done")):
                    self.log.debug(
                        f"Ignore subfolder {sub_folder_full_path}, 'done' file found."
                    )
                    self.processing_jobs[sub_folder_full_path] = True
                    continue

                # Identify or Create Job
                job = self.repo_deposit_job.get_by_flow_id_and_injection_title(
                    flow_setting.id, sub_folder_name
                )
                if job is None:
                    job = self.deposit_job_service.job_initial(
                        sub_folder_full_path, sub_folder_name, flow_setting
                    )
                    self.log.info(f"Created a new job: {job.id} {job.injectionTitle}")

                if job.state in ["PAUSED", "CANCELED"]:
                    self.log.debug(
                        f"Ignore subfolder, paused or cancelled: {sub_folder_full_path}"
                    )
                    continue

                if job.stage != "INGEST":
                    self.log.debug(
                        f"Ignore subfolder, already ingested: {sub_folder_full_path}"
                    )
                    self.processing_jobs[sub_folder_full_path] = True
                    continue

                # Check for the completion sidecar file (e.g., "ready.txt")
                complete_file = os.path.join(
                    sub_folder_full_path, flow_setting.injectionCompleteFileName
                )
                if not os.path.exists(complete_file):
                    self.log.debug(
                        f"Ignore subfolder {sub_folder_full_path}, completion file not found"
                    )
                    continue

                # Calculate stats (simplified implementation of stat.stat)
                file_count = 0
                file_size = 0
                stream_path = os.path.join(
                    sub_folder_full_path, flow_setting.streamLocation or ""
                )
                for root, dirs, files in os.walk(stream_path):
                    for f in files:
                        file_count += 1
                        file_size += os.path.getsize(os.path.join(root, f))

                # Update job
                job = self.deposit_job_service.job_update_files_stat(
                    job, file_count, file_size
                )
                job = self.deposit_job_service.job_scan_complete(job)

                self.log.info(f"Ingested new job : {job.id}")
                self.processing_jobs[sub_folder_full_path] = True
