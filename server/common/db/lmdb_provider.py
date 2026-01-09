from pathlib import Path
import threading
import lmdb
import orjson


class LMDBWriter:
    def __init__(self, path, map_size=32 * 1024**3, batch_size=50000):
        Path(path).parent.mkdir(parents=True, exist_ok=True)
        self.env = lmdb.open(str(path), map_size=map_size, subdir=False, lock=False, writemap=False, map_async=True)
        self.batch_size = batch_size
        self.count = 0

        # 1. Create a Lock to serialize access from multiple threads
        self.lock = threading.Lock()

        # Start the first transaction
        self.txn = self.env.begin(write=True)

    def put(self, key, value):
        # 2. Acquire lock before touching the transaction or counter
        with self.lock:
            self.txn.put(orjson.dumps(key), orjson.dumps(value))
            self.count += 1

            if self.count >= self.batch_size:
                self._commit_internal()

    def get(self, key):
        with self.env.begin(write=False) as txn:
            value = txn.get(orjson.dumps(key))
            if value is not None:
                return orjson.loads(value)
            return None

    def delete(self, key):
        with self.env.begin(write=False) as txn:
            txn.delete(orjson.dumps(key))

    def commit(self):
        """Public commit method (thread-safe)"""
        with self.lock:
            self._commit_internal()

    def _commit_internal(self):
        """
        Internal commit logic.
        MUST be called while holding self.lock to ensure safety.
        """
        if self.count > 0:  # Only commit if we have pending writes (optional optimization)
            self.txn.commit()
            self.txn = self.env.begin(write=True)
            self.count = 0
        # Note: If count was 0, we could choose to do nothing,
        # but renewing txn is safer to ensure we don't hold an old view forever.
        # For simplicity in this pattern, we just keep the txn alive.

    def close(self):
        with self.lock:
            if self.count > 0:
                self.txn.commit()  # Commit final data
            else:
                self.txn.abort()  # Abort empty txn

            self.env.sync()
            self.env.close()


class LMDBReader:
    def __init__(self, path, map_size=16 * 1024**3, batch_size=2000):
        self.batch_size = batch_size
        Path(path).parent.mkdir(parents=True, exist_ok=True)
        self.env = lmdb.open(path, map_size=map_size, subdir=False, lock=False, readonly=True, readahead=True, max_readers=batch_size)

    def get(self, key):
        with self.env.begin(write=False) as txn:
            value = txn.get(orjson.dumps(key))
            if value is not None:
                return orjson.loads(value)
            return None

    def batch_get(self):
        with self.env.begin(write=False) as txn:
            cursor = txn.cursor()
            batch = []
            for key, value in cursor:
                batch.append(orjson.loads(value))
                if len(batch) >= self.batch_size:
                    yield batch
                    batch = []
            if batch:
                yield batch

    def batch_get_keys(self):
        with self.env.begin(write=False) as txn:
            cursor = txn.cursor()
            batch = []
            for key, value in cursor:
                batch.append(orjson.loads(key))
                if len(batch) >= self.batch_size:
                    yield batch
                    batch = []
            if batch:
                yield batch

    def all_data(self):
        with self.env.begin(write=False) as txn:
            cursor = txn.cursor()
            batch = []
            for key, value in cursor:
                batch.append(orjson.loads(value))
            return batch

    def all_keys(self):
        with self.env.begin(write=False) as txn:
            cursor = txn.cursor()
            batch = []
            for key, value in cursor:
                batch.append(orjson.loads(key))
            return batch

    @property
    def total_count(self):
        with self.env.begin(write=False) as txn:
            stats = txn.stat()
            count = stats["entries"]
        return count

    def close(self):
        self.env.close()
