import enum
import logging
from dataclasses import dataclass
from typing import Optional
from playhouse.shortcuts import model_to_dict
import time
import falcon
import orjson

from app.auth.sessions import SessionManager
from app.data.models import FixityTask
from app.task.task_manager import TaskManager


@dataclass
class FixityOperation:
    action: str
    fixity_type: Optional[str] = None
    forced_sync: Optional[bool] = False


class ActionType(enum.Enum):
    START = "start"
    RESTART = "restarts"
    PATCH = "patch"
    RESUME = "resume"
    STOP = "stop"
    ARCHIVE = "archive"


class TaskResource:
    def __init__(self, session_manager: SessionManager, task_manager: TaskManager):
        self.session_manager = session_manager
        self.task_manager = task_manager

    def on_get(self, req: falcon.Request, rsp: falcon.Response, oid=None):
        if oid is None:
            datesets = FixityTask.select()
        else:
            datesets = FixityTask.get_or_none(FixityTask.id == oid)
        rsp.status = falcon.HTTP_OK
        rsp.media = datesets

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        task: FixityTask = FixityTask(**data_json)
        if task.name is None or len(task.name) == 0:
            err = "The name can not be empty"
            logging.error(err)
            raise falcon.HTTPBadRequest(description=err)

        if task.id:
            old_task = FixityTask.get_or_none(FixityTask.id == task.id)
            if old_task is None:
                err = f"The task has been deleted, can not be updated"
                logging.error(err)
                raise falcon.HTTPBadRequest(description=err)

        old_task = FixityTask.get_or_none(FixityTask.name == task.name)
        if old_task is not None and old_task.id != task.id:
            err = f"Duplicated name: {task.name}"
            logging.error(err)
            raise falcon.HTTPBadRequest(description=err)

        if task.id is None:
            task.creation_date = time.time()
            task.save(force_insert=True)
        else:
            task.save(force_insert=False)
        rsp.status = falcon.HTTP_OK
        rsp.media = model_to_dict(task)

    def on_delete(self, req: falcon.Request, rsp: falcon.Response):
        pass
