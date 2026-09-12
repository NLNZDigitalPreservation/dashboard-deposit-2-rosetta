import threading
import time
from queue import Queue
from typing import Optional

from app.fixity_task import FixityTask
from common.data.models import FixityReport, GlobalSettings


class GlobalSettingsDao:
    G_ID = 1
    lock = threading.Lock()
    latest_task_queue = Queue(
        maxsize=32
    )  # A queue to hold the latest task for real-time progress updates

    @staticmethod
    def forced_get() -> GlobalSettings:
        with GlobalSettingsDao.lock:
            instance = GlobalSettings.get_or_none(
                GlobalSettings.id == GlobalSettingsDao.G_ID
            )
            if instance is None:
                instance = GlobalSettings(id=GlobalSettingsDao.G_ID, paused=False)
                instance.save(force_insert=True)
            return instance

    @staticmethod
    def get() -> GlobalSettings:
        return GlobalSettingsDao.forced_get()

    @staticmethod
    def save(data_dict) -> GlobalSettings:
        settings = GlobalSettings(**data_dict)
        return GlobalSettingsDao.save_instance(settings)

    @staticmethod
    def save_instance(settings: GlobalSettings) -> GlobalSettings:
        settings.id = GlobalSettingsDao.G_ID
        settings.save()
        GlobalSettingsDao.latest_task_queue.put(settings.task)
        return GlobalSettingsDao.forced_get()

    @staticmethod
    def update_progress(report=None, progress=None) -> GlobalSettings:
        update_data = {
            "report": report,
            "progress": progress,
        }

        GlobalSettings.update(**update_data).where(
            GlobalSettings.id == GlobalSettingsDao.G_ID
        ).execute()

        return GlobalSettingsDao.forced_get()

    @staticmethod
    def get_task() -> Optional[FixityTask]:
        """Load the current FixityTask from GlobalSettings.task JSON field."""
        g_settings = GlobalSettingsDao.forced_get()
        if g_settings.task is None:
            return None
        return FixityTask.from_dict(g_settings.task)

    @staticmethod
    def save_task(task: FixityTask) -> FixityTask:
        """Persist a FixityTask into GlobalSettings.task."""
        GlobalSettingsDao.forced_get()
        task_dict = task.to_dict()
        GlobalSettings.update(
            task=task_dict,
        ).where(GlobalSettings.id == GlobalSettingsDao.G_ID).execute()
        GlobalSettingsDao.latest_task_queue.put(task_dict)
        return GlobalSettingsDao.get_task()

    @staticmethod
    def update_task(**kwargs) -> Optional[FixityTask]:
        """Update specific fields on the current task and persist."""
        task = GlobalSettingsDao.get_task()
        if task is None:
            return None
        for key, value in kwargs.items():
            setattr(task, key, value)
        task = GlobalSettingsDao.save_task(task)
        return task

    @staticmethod
    def archive():
        task = GlobalSettingsDao.get_task()
        g = GlobalSettingsDao.forced_get()
        report = g.report
        payload_progress = g.progress
        FixityReport.create(
            task=task.to_dict(),
            report=report,
            progress=payload_progress,
            archived_time=time.time(),
        )
