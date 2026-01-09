from app.data_sync.data_sync_full_erase import DataSyncFullErase
from app.data_sync.data_sync_full_incremental import DataSyncFullIncremental
from app.data_sync.data_sync_patch import DataSyncPatch
from app.data.models import PermanentIndex
from common.metadata import FixityResultCode, FixityType


def test_data_sync_full_erase(env_args, fixity_db, rosetta_db, peewee_db):
    # Clear all the data in the permanent_index table
    with fixity_db.pool.connection() as conn:
        conn.execute("TRUNCATE TABLE permanent_index RESTART IDENTITY CASCADE;")

    count = PermanentIndex.select().count()
    assert count == 0

    proc = DataSyncFullErase(env_args, fixity_db, rosetta_db)
    proc.sync_data()

    rosetta_count = rosetta_db.get_count(sql_query=f"select count(1) from {rosetta_db.rosetta_db_schemaprefix}_PER00.permanent_index where storage_entity_type in ('FILE','IE')")

    count = PermanentIndex.select().count()
    assert count == rosetta_count


def test_data_sync_full_incremantal_based_empty_fixity_db(env_args, fixity_db, rosetta_db, peewee_db):
    # Clear all the data in the permanent_index table
    with fixity_db.pool.connection() as conn:
        conn.execute("TRUNCATE TABLE permanent_index RESTART IDENTITY CASCADE;")

    count = PermanentIndex.select().count()
    assert count == 0

    proc = DataSyncFullIncremental(env_args, fixity_db, rosetta_db)
    proc.sync_data()

    rosetta_count = rosetta_db.get_count(sql_query=f"select count(1) from {rosetta_db.rosetta_db_schemaprefix}_PER00.permanent_index where storage_entity_type in ('FILE','IE')")

    count = PermanentIndex.select().count()
    assert count == rosetta_count


def test_data_sync_full_incremantal_based_existing_data(env_args, fixity_db, rosetta_db, peewee_db):
    # Ingest all the data in the permanent_index table
    proc = DataSyncFullErase(env_args, fixity_db, rosetta_db)
    proc.sync_data()

    count = PermanentIndex.select().count()
    assert count > 0

    query = PermanentIndex.select()
    for row in query:
        row.check_sum = None
        row.save()

    query = PermanentIndex.select()
    for row in query:
        assert row.check_sum is None

    proc = DataSyncFullIncremental(env_args, fixity_db, rosetta_db)
    proc.sync_data()

    rosetta_count = rosetta_db.get_count(sql_query=f"select count(1) from {rosetta_db.rosetta_db_schemaprefix}_PER00.permanent_index where storage_entity_type in ('FILE','IE')")
    count = PermanentIndex.select().count()
    assert count == rosetta_count

    query = PermanentIndex.select()
    for row in query:
        assert row.check_sum is not None


def test_data_sync_patch(env_args, fixity_db, rosetta_db, peewee_db):
    # Ingest all the data in the permanent_index table
    proc = DataSyncFullErase(env_args, fixity_db, rosetta_db)
    proc.sync_data()

    count = PermanentIndex.select().count()
    assert count > 0

    updated_all = PermanentIndex.update({PermanentIndex.pir_file_check_state: FixityResultCode.SUCCESS, PermanentIndex.pir_mets_check_state: FixityResultCode.SUCCESS}).execute()
    assert updated_all == count

    updated_ie = (
        PermanentIndex.update({PermanentIndex.pir_mets_check_state: FixityResultCode.CHECKSUM_NOT_MATCH, PermanentIndex.check_sum: None}).where(PermanentIndex.storage_entity_type == "IE").execute()
    )
    assert updated_ie > 0

    # The trigger events sync will not patch the IE fixity results
    proc = DataSyncPatch(env_args, fixity_db, rosetta_db)
    proc.sync_data(fixity_task_type=FixityType.BLOB_EVENT)
    query = PermanentIndex.select().where(PermanentIndex.storage_entity_type == "IE")
    for row in query:
        assert row.pir_mets_check_state == FixityResultCode.CHECKSUM_NOT_MATCH
        assert row.check_sum is None

    # The BAU sync will patch all the fixity results
    proc = DataSyncPatch(env_args, fixity_db, rosetta_db)
    proc.sync_data(fixity_task_type=FixityType.BAU)
    query = PermanentIndex.select().where(PermanentIndex.storage_entity_type == "IE")
    for row in query:
        assert row.pir_mets_check_state == FixityResultCode.INITIALED
        assert row.check_sum is not None
