from datetime import datetime as dt

from app.data.models import FixityTask
from app.task.fixity_pipeline import FixityPipeline
from common.metadata import FixityType, FixityScope

name_task = "This is a test task"


def test_bau_all(env_args, fixity_db, rosetta_db):
    task = FixityTask(fixity_type=FixityType.BAU.value, fixity_scope=FixityScope.ALL.value)
    pipeline = FixityPipeline(args=env_args, db_fixity=fixity_db, db_rosetta=rosetta_db)
    ret = pipeline.run(task)
    assert ret


def test_bau_only_failed(env_args, fixity_db, rosetta_db):
    task = FixityTask(fixity_type=FixityType.BAU.value, fixity_scope=FixityScope.ONLY_FAILED.value)
    pipeline = FixityPipeline(args=env_args, db_fixity=fixity_db, db_rosetta=rosetta_db)
    ret = pipeline.run(task)
    assert ret


def test_bau_not_succeed(env_args, fixity_db, rosetta_db):
    task = FixityTask(fixity_type=FixityType.BAU.value, fixity_scope=FixityScope.ALL_BUT_SUCCEED.value)
    pipeline = FixityPipeline(args=env_args, db_fixity=fixity_db, db_rosetta=rosetta_db)
    ret = pipeline.run(task)
    assert ret
