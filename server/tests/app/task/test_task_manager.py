import logging
import time
from datetime import timedelta
import inspect

from app.data.models import FixityTask
from app.task.fixity_pipeline import FixityPipeline
from app.task.task_manager import TaskManager
from common.metadata import FixityScope, FixityTaskState, FixityType, TaskState
from common.shared import exiting

NAME_PREFIX = "[Task manager]"


def test_run_scheduled(env_args, fixity_db, rosetta_db, peewee_db):
    task_name = f"{NAME_PREFIX}-{inspect.currentframe().f_code.co_name}"
    FixityTask.delete().where(FixityTask.name == task_name).execute()

    task: FixityTask = FixityTask()
    task.name = task_name
    task.fixity_type = FixityType.BAU.value
    task.fixity_scope = FixityScope.ALL.value
    task.start_time = time.time() - timedelta(hours=1).seconds
    task.end_time = time.time() + timedelta(hours=1).seconds
    task.state = FixityTaskState.INITIALED.value
    task.save(force_insert=True)

    task_manager = TaskManager(env_args, fixity_db, rosetta_db)
    exiting.add_before_exit_callback(task_manager.close)

    retry_count = 0
    while retry_count < 15:
        retry_count += 1
        time.sleep(1.0)
        task = FixityTask.get_or_none(FixityTask.name == task_name)
        assert task is not None
        if task.state not in (FixityTaskState.INITIALED.value, FixityTaskState.RUNNING.value):
            break
        logging.info(f"Running: {task_name}")

    task = FixityTask.get_or_none(FixityTask.name == task_name)
    assert task is not None
    assert task.state == FixityTaskState.SUCCESS.value


def test_run_unstarted_time(env_args, fixity_db, rosetta_db, peewee_db):
    task_name = f"{NAME_PREFIX}-{inspect.currentframe().f_code.co_name}"
    FixityTask.delete().where(FixityTask.name == task_name).execute()

    task: FixityTask = FixityTask()
    task.name = task_name
    task.fixity_type = FixityType.BAU.value
    task.fixity_scope = FixityScope.ALL.value
    task.start_time = time.time() + timedelta(hours=1).seconds
    task.end_time = time.time() + timedelta(hours=2).seconds
    task.state = FixityTaskState.INITIALED.value
    task.save(force_insert=True)

    task_manager = TaskManager(env_args, fixity_db, rosetta_db)
    exiting.add_before_exit_callback(task_manager.close)

    retry_count = 0
    while retry_count < 15:
        retry_count += 1
        time.sleep(1.0)
        task = FixityTask.get_or_none(FixityTask.name == task_name)
        assert task is not None
        if task.state not in (FixityTaskState.INITIALED.value, FixityTaskState.RUNNING.value):
            break
        logging.info(f"Running: {task_name}")

    task = FixityTask.get_or_none(FixityTask.name == task_name)
    assert task is not None
    assert task.state == FixityTaskState.INITIALED.value


def test_run_ended_time(env_args, fixity_db, rosetta_db, peewee_db):
    task_name = f"{NAME_PREFIX}-{inspect.currentframe().f_code.co_name}"
    FixityTask.delete().where(FixityTask.name == task_name).execute()

    task: FixityTask = FixityTask()
    task.name = task_name
    task.fixity_type = FixityType.BAU.value
    task.fixity_scope = FixityScope.ALL.value
    task.start_time = time.time() - timedelta(hours=2).seconds
    task.end_time = time.time() - timedelta(hours=1).seconds
    task.state = FixityTaskState.INITIALED.value
    task.save(force_insert=True)

    task_manager = TaskManager(env_args, fixity_db, rosetta_db)
    exiting.add_before_exit_callback(task_manager.close)

    retry_count = 0
    while retry_count < 15:
        retry_count += 1
        time.sleep(1.0)
        task = FixityTask.get_or_none(FixityTask.name == task_name)
        assert task is not None
        if task.state not in (FixityTaskState.INITIALED.value, FixityTaskState.RUNNING.value):
            break
        logging.info(f"Running: {task_name}")

    task = FixityTask.get_or_none(FixityTask.name == task_name)
    assert task is not None
    assert task.state == FixityTaskState.INITIALED.value


def test_run_queued_scheduled(env_args, fixity_db, rosetta_db, peewee_db):
    # Previous running
    task_name_0 = f"{NAME_PREFIX}-{inspect.currentframe().f_code.co_name}-0"
    FixityTask.delete().where(FixityTask.name == task_name_0).execute()

    task_0: FixityTask = FixityTask()
    task_0.name = task_name_0
    task_0.fixity_type = FixityType.BAU.value
    task_0.fixity_scope = FixityScope.ALL.value
    task_0.start_time = time.time() - timedelta(hours=0.5).seconds
    task_0.end_time = time.time() + timedelta(hours=1).seconds
    task_0.state = FixityTaskState.INITIALED.value
    task_0.save(force_insert=True)

    task_name_1 = f"{NAME_PREFIX}-{inspect.currentframe().f_code.co_name}-1"
    FixityTask.delete().where(FixityTask.name == task_name_1).execute()

    task_1: FixityTask = FixityTask()
    task_1.name = task_name_1
    task_1.fixity_type = FixityType.BAU.value
    task_1.fixity_scope = FixityScope.ALL.value
    task_1.start_time = time.time() - timedelta(hours=1).seconds
    task_1.end_time = time.time() + timedelta(hours=1).seconds
    task_1.state = FixityTaskState.INITIALED.value
    task_1.save(force_insert=True)

    task_manager = TaskManager(env_args, fixity_db, rosetta_db)
    exiting.add_before_exit_callback(task_manager.close)

    retry_count = 0
    while retry_count < 30:
        retry_count += 1
        time.sleep(1.0)
        tasks = FixityTask.select().where((FixityTask.name == task_name_0) | (FixityTask.name == task_name_1))
        assert tasks is not None
        assert len(tasks) == 2

        if tasks[0].state in (FixityTaskState.INITIALED.value, FixityTaskState.RUNNING.value):
            continue
        if tasks[1].state in (FixityTaskState.INITIALED.value, FixityTaskState.RUNNING.value):
            continue
        break

    task_0 = FixityTask.get_or_none(FixityTask.name == task_name_0)
    assert task_0 is not None
    assert task_0.state == FixityTaskState.SUCCESS.value

    task_1 = FixityTask.get_or_none(FixityTask.name == task_name_1)
    assert task_1 is not None
    assert task_1.state == FixityTaskState.SUCCESS.value

    assert task_1.actual_start_time < task_0.actual_start_time
