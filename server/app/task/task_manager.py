import logging
import time
from threading import Event, Lock, Thread
from typing import Optional

from app.data.models import FixityTask
from app.data.dao_global_setting import GlobalSettingsDao
from app.task.fixity_pipeline import FixityPipeline
from common.metadata import FixityTaskState


class TaskManager:
    def __init__(self, args, db_fixity, db_rosetta):
        self.args = args
        self.db_fixity = db_fixity
        self.db_rosetta = db_rosetta
        self.pipeline: Optional[FixityPipeline] = None
        self.lock = Lock()
        self.is_running = Event()
        self.event_loop = Thread(target=self.execute_tasks)
        self.event_loop.start()

    def close(self):
        self.is_running.set()
        with self.lock:
            if self.pipeline is not None:
                self.pipeline.close()
        self.event_loop.join(timeout=3.0)

    def _execute_tasks(self):
        global_settings = GlobalSettingsDao.get()
        if self.is_running.is_set() or global_settings.is_paused():
            logging.info("Pipeline is ignored")
            return

        cur_time = time.time()
        task: FixityTask = (
            FixityTask.select()
            .where((FixityTask.state == FixityTaskState.INITIALED.value) & (FixityTask.start_time <= cur_time) & (FixityTask.end_time >= cur_time))
            .order_by(FixityTask.start_time.asc())
            .limit(1)
            .first()
        )

        if task is None:
            return

        task.state = FixityTaskState.RUNNING.value
        task.actual_start_time = cur_time
        task.save()

        self.fixity_pipeline = FixityPipeline(args=self.args, db_fixity=self.db_fixity, db_rosetta=self.db_rosetta)
        ret = self.fixity_pipeline.run(task=task)

        if ret:
            task.state = FixityTaskState.SUCCESS.value
        else:
            task.state = FixityTaskState.FAILED.value
        task.actual_end_time = time.time()
        task.save()

        with self.lock:
            self.fixity_pipeline = None

    def execute_tasks(self):
        # Rescheduled the stopped running tasks
        FixityTask.update(state=FixityTaskState.INITIALED.value).where(FixityTask.state == FixityTaskState.RUNNING.value).execute()

        while not self.is_running.is_set():
            try:
                self._execute_tasks()
            except Exception as ex:
                logging.error(f"Failed to execute the task, {ex}")
                logging.exception(ex)
            finally:
                self.is_running.wait(timeout=10)
