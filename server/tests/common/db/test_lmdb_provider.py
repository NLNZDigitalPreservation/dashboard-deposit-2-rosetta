import logging
import os
import shutil
import orjson
import pytest
from common.db.lmdb_provider import LMDBReader, LMDBWriter
import threading
import time
import uuid


@pytest.fixture
def db_path(tmp_path):
    db_file = f"{tmp_path}/{uuid.uuid4()}"  # Each test gets its own temp directory
    yield db_file

    # Cleanup is handled by tmp_path fixture
    if os.path.exists(db_file):
        shutil.rmtree(db_file, ignore_errors=True)


def test_lmdb_writer_and_reader(db_path):
    """
    Scenario: Test the basic put/batch_get methods.
    """
    writer = LMDBWriter(db_path, batch_size=3)
    data = {
        "key1": {"name": "Alice", "age": 30},
        "key2": {"name": "Bob", "age": 25},
        "key3": {"name": "Charlie", "age": 35},
        "key4": {"name": "Diana", "age": 28},
    }

    for key, value in data.items():
        writer.put(key, value)
    writer.close()

    reader = LMDBReader(db_path, batch_size=2)
    for key, expected_value in data.items():
        value = reader.get(key)
        assert value == expected_value

    all_values = []
    for batch in reader.batch_get():
        all_values.extend(batch)

    assert len(all_values) == len(data)
    reader.close()


def test_reader_updates_and_isolation(db_path):
    """
    Scenario: Test if a reader sees updates committed by a writer.
    """
    # 1. Setup Writer and Write Initial Data
    writer = LMDBWriter(db_path)
    writer.put("user_1", "ver_1")
    writer.commit()

    # 2. Setup Reader
    reader = LMDBReader(db_path)

    # Check initial value
    assert reader.get("user_1") == "ver_1"

    # 3. Update Data
    writer.put("user_1", "ver_2")
    writer.commit()

    # 4. Reader should see the new value
    # Your class opens a NEW transaction inside get(), so it sees the update immediately.
    assert reader.get("user_1") == "ver_2"

    # 5. Snapshot Isolation (Advanced)
    # If we hold a transaction open manually, it should NOT see updates
    with reader.env.begin(write=False) as snapshot_txn:
        # Currently sees "ver_2"
        val = orjson.loads(snapshot_txn.get(orjson.dumps("user_1")))
        assert val == "ver_2"

        # Writer updates to "ver_3"
        writer.put("user_1", "ver_3")
        writer.commit()

        # The 'snapshot_txn' should still see "ver_2"
        old_val = orjson.loads(snapshot_txn.get(orjson.dumps("user_1")))
        assert old_val == "ver_2"

    # But a fresh get() sees "ver_3"
    assert reader.get("user_1") == "ver_3"

    writer.close()
    reader.close()


def test_concurrent_writes_blocking(db_path):
    """
    Scenario 2: Ensure multiple threads writing to the same DB block each other safely.
    """
    max_count = 10 * 1000
    w = LMDBWriter(db_path, batch_size=1000)

    def writer_worker(start_idx, count):

        # This will BLOCK here if another thread holds the write lock
        for i in range(start_idx, start_idx + count):
            w.put(f"key_{i}", f"val_{i}")
            time.sleep(0.0001)  # Small sleep to encourage overlap attempts

    # Create two threads trying to write at the same time
    t1 = threading.Thread(target=writer_worker, args=(0, max_count))
    t2 = threading.Thread(target=writer_worker, args=(max_count, max_count))

    t1.start()
    t2.start()

    t1.join()
    t2.join()
    w.close()

    # Validation phase
    reader = LMDBReader(db_path)

    # Ensure all 100 keys exist.
    # If concurrency failed, we might have lost data or corrupted the DB.
    found = 0
    for i in range(max_count * 2):
        val = reader.get(f"key_{i}")

        if val == f"val_{i}":
            found += 1
        else:
            logging.error(f"Missing or incorrect value for key_{i}: {val}")

    reader.close()

    assert found == max_count * 2
