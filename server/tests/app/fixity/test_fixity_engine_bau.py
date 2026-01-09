import logging
from typing import Optional

from app.data_sync.data_sync_full_erase import DataSyncFullErase
from app.data.models import PermanentIndex, FixityTask
from app.fixity import fixity_engine_bau as bau_engines
from common.metadata import FixityResultCode, FixityScope, FixityType
from tools import pyaz
from tests.utils.fixture_utils import FileEntity
from tests.utils.individual_data_resources import IndividualDataResources

FILE_STORED_ENTITY_ID = "FL41361820"
METS_STORED_ENTITY_ID = "IE41361819"


def test_fixity_full(env_args, fixity_db, rosetta_db, peewee_db, mocked_files):
    env_args.blob_url_prefix = ""

    blobs = mocked_files
    # data_sync = DataSyncFullErase(args=env_args, db_fixity=fixity_db, db_rosetta=rosetta_db)
    # data_sync.sync_data()

    task = FixityTask(
        name="Daily fixity check",
        fixity_type=FixityType.BAU.value,
        fixity_scope=FixityScope.ALL.value,
        scheduled_time=1734172800,  # unix timestamp
        sync_data_flag=True,
        start_time=0,
        end_time=0,
        state=0,
    )
    bau_engines.fixity_files(args=env_args, db_fixity=fixity_db, task=task)
    bau_engines.fixity_mets(args=env_args, db_fixity=fixity_db, task=task)

    for blob in blobs:
        data: Optional[PermanentIndex] = PermanentIndex.get_or_none(PermanentIndex.stored_entity_id == blob.stored_entity_id)
        assert data
        if data.storage_entity_type == "IE":
            assert data.pir_mets_check_state == FixityResultCode.SUCCESS

        assert data.pir_file_check_state == FixityResultCode.SUCCESS
        assert data.pir_file_checksum == blob.check_sum_md5


def test_fixity_patch(env_args, fixity_db, rosetta_db, peewee_db, mocked_files):
    env_args.blob_url_prefix = ""
    blobs_map = {blob.stored_entity_id: blob for blob in mocked_files}
    # data_sync = DataSyncFullErase(args=env_args, db_fixity=fixity_db, db_rosetta=rosetta_db)
    # data_sync.sync_data()

    task = FixityTask(
        name="Daily fixity check",
        fixity_type=FixityType.BAU.value,
        fixity_scope=FixityScope.ONLY_FAILED.value,
        scheduled_time=1734172800,  # unix timestamp
        sync_data_flag=True,
        start_time=0,
        end_time=0,
        state=0,
    )

    def _patch_file():
        blob: FileEntity = blobs_map.get(FILE_STORED_ENTITY_ID)
        assert blob is not None
        job: Optional[PermanentIndex] = PermanentIndex.get_or_none(PermanentIndex.stored_entity_id == FILE_STORED_ENTITY_ID)
        job.pir_file_check_state = FixityResultCode.UNKNOWN_ERROR
        job.pir_file_checksum = None
        job.save()

        bau_engines.fixity_files(args=env_args, db_fixity=fixity_db, task=task)

        job = PermanentIndex.get_or_none(PermanentIndex.stored_entity_id == FILE_STORED_ENTITY_ID)
        assert job is not None
        assert job.pir_file_check_state == FixityResultCode.SUCCESS
        assert job.pir_file_checksum == blob.check_sum_md5

    def _patch_mets():
        blob: FileEntity = blobs_map.get(METS_STORED_ENTITY_ID)
        assert blob is not None
        job: Optional[PermanentIndex] = PermanentIndex.get_or_none(PermanentIndex.stored_entity_id == METS_STORED_ENTITY_ID)
        job.pir_mets_check_state = FixityResultCode.UNKNOWN_ERROR
        job.save()

        bau_engines.fixity_mets(args=env_args, db_fixity=fixity_db, task=task)

        job = PermanentIndex.get_or_none(PermanentIndex.stored_entity_id == METS_STORED_ENTITY_ID)
        assert job is not None
        assert job.pir_mets_check_state == FixityResultCode.SUCCESS

        file_blob: FileEntity = blobs_map.get(FILE_STORED_ENTITY_ID)
        file_job: Optional[PermanentIndex] = PermanentIndex.get_or_none(PermanentIndex.stored_entity_id == FILE_STORED_ENTITY_ID)
        assert file_job is not None
        assert file_job.pir_mets_check_state == FixityResultCode.SUCCESS
        assert file_job.pir_mets_checksum == file_blob.check_sum_md5

    _patch_file()
    _patch_mets()
