import logging
import os
from datetime import datetime, timedelta
import threading
import shutil

import falcon
from app.domain import *

GLOBAL_SINGLE_ID = 1


class ServicesJob:
    def __init__(self, args):
        self.args = args
        self.lock = threading.Lock()

    def handle_flow_missing_jobs(self, flow_setting_id, jobs):
        logging.warning("Flow setting not found: %s", flow_setting_id)

    def handle_deposit(self, deposit_account, flow_setting, job) -> bool:
        if not (job.stage == "DEPOSIT" and job.state == "INITIALED"):
            self.log.debug(f"Skip deposit job for: {flow_setting.id} at status [{job.stage}] [{job.state}]")
            return False

        try:
            result = self.rosetta_web_service.deposit(deposit_account, job.injectionTitle, flow_setting.producerId, flow_setting.materialFlowId)
        except Exception as e:
            self.log.error(f"Failed to submit job: {e}")
            return False

        if result.is_success():
            self.deposit_job_service.job_deposit_accept(job, result.sip_id, flow_setting)
            self.log.info(f"Job [{job.injectionTitle}] submitted successfully, SIPId=[{result.sip_id}]")
            return True
        else:
            self.deposit_job_service.job_deposit_reject(job, result.result_message)
            self.log.warning(f"Job [{job.injectionTitle}] submission failed: {result.result_message}")
            return False

    def handle_polling_status(self, deposit_account, job):
        if job.stage != "DEPOSIT" or job.state != "RUNNING":
            return

        try:
            sip_info = self.rosetta_web_service.get_sip_status_info(deposit_account, job.sipId)
            self.log.info(f"Polling job {job.id}: {sip_info.stage} / {sip_info.status}")
            self.deposit_job_service.job_update_status(job, sip_info)
        except Exception as e:
            self.log.error(f"Failed to scan deposit job status: {e}")

    def handle_finalize(self, flow_setting, job):
        # Finalize successful jobs
        is_ready = (job.stage == "DEPOSIT" and job.state == "SUCCEED") or (job.stage == "FINALIZE" and job.state in ["INITIALED", "RUNNING"])

        if is_ready:
            # Check/Create 'done' file
            if not os.path.exists(job.injectionPath):
                self.log.error(f"Original directory does not exist: {job.injectionPath}")
                return

            done_file_path = os.path.join(job.injectionPath, "done")
            if not os.path.exists(done_file_path):
                try:
                    with open(done_file_path, "w") as f:
                        f.write("done")
                except Exception as e:
                    self.log.error(f"Failed to create done file: {e}")
                    return

            job = self.deposit_job_service.job_finalize_end(job, "SUCCEED")
            self.log.info(f"Finalized normal job: {job.id}")

        elif job.state == "CANCELED" and job.stage not in ["FINALIZE", "FINISHED"]:
            job = self.deposit_job_service.job_finalize_end(job, job.state)
            self.log.info(f"Finalized cancelled job: {job.id}")

        # Post-Finalize Logic (Backup and Delete)
        if job.stage == "FINALIZE" and job.state in ["SUCCEED", "CANCELED"]:
            self._try_to_backup_actual_contents(flow_setting, job)
            if not job.backupCompleted:
                return

            self._try_to_delete_actual_contents(flow_setting, job)
            if not job.actualContentDeleted:
                return

            self.deposit_job_service.job_finished_end(job, job.state)
            self.log.info(f"The job is finished: {job.id}")

    def handle_inactive_pruning(self, flow_setting, job) -> bool:
        if not (job.stage == "FINISHED" and job.state in ["SUCCEED", "CANCELED"]):
            return False

        # Calculate time from epoch milliseconds
        finished_time_ts = job.finishedTime if job.finishedTime else job.latestTime
        finished_dt = datetime.fromtimestamp(finished_time_ts / 1000.0)

        deadline_time = finished_dt + timedelta(days=flow_setting.maxActiveDays)
        if deadline_time > datetime.now():
            return False

        self.deposit_job_service.job_delete(job)
        self.log.info(f"Pruned inactive job: {job.id}")
        return True

    def _try_to_backup_actual_contents(self, flow_setting, job):
        if job.backupCompleted:
            return

        backup_option = str(flow_setting.actualContentBackupOptions).lower()
        if not backup_option or backup_option == "notbackup":
            self.deposit_job_service.job_completed_backup(job)
            return

        sub_folders_text = flow_setting.backupSubFolders or ""
        sub_folders = [line.strip() for line in sub_folders_text.splitlines() if line.strip()]

        if not sub_folders:
            self.deposit_job_service.job_completed_backup(job)
            return

        target_dir = os.path.join(flow_setting.backupPath, job.injectionTitle)

        try:
            if os.path.exists(target_dir):
                shutil.rmtree(target_dir)
            os.makedirs(target_dir, exist_ok=True)

            for sub_folder in sub_folders:
                src = os.path.join(job.injectionPath, sub_folder)
                if os.path.exists(src):
                    if backup_option == "backupsubfolder":
                        dest = os.path.join(target_dir, sub_folder)
                        shutil.copytree(src, dest)
                    elif backup_option == "backupcontentswithoutsubfoldername":
                        # Copy contents of src into target_dir
                        for item in os.listdir(src):
                            s = os.path.join(src, item)
                            d = os.path.join(target_dir, item)
                            if os.path.isdir(s):
                                shutil.copytree(s, d)
                            else:
                                shutil.copy2(s, d)

            self.deposit_job_service.job_completed_backup(job)
        except Exception as e:
            self.log.error(f"Failed to backup contents for job {job.id}: {e}")

    def _try_to_delete_actual_contents(self, flow_setting, job):
        if job.actualContentDeleted:
            return

        del_option = flow_setting.actualContentDeleteOptions

        is_delete = False
        if del_option == "deleteInstantly":
            is_delete = True
        elif del_option == "deleteExceedMaxStorageDays":
            deadline = datetime.now() - timedelta(days=flow_setting.maxSaveDays)
            update_dt = datetime.fromtimestamp(job.latestTime / 1000.0)
            if update_dt < deadline:
                is_delete = True

        if is_delete:
            try:
                # Be very careful with automated deletion
                if os.path.exists(job.injectionPath) and len(job.injectionPath) > 5:
                    shutil.rmtree(job.injectionPath)
                    self.deposit_job_service.job_deleted_actual_content(job)
                    self.log.info(f"Deleted actual contents for job: {job.id}")
            except Exception as e:
                self.log.error(f"Failed to delete directory {job.injectionPath}: {e}")
        else:
            # If option is 'notDelete'
            self.deposit_job_service.job_deleted_actual_content(job)
