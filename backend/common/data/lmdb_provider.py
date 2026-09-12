import logging
import threading
from pathlib import Path

import lmdb
import orjson


class LMDBWriter:
    def __init__(self, path, map_size=128 * 1024**3, batch_size=50000):
        Path(path).parent.mkdir(parents=True, exist_ok=True)
        self.map_size = map_size
        self.env = lmdb.open(
            str(path),
            map_size=self.map_size,
            subdir=False,
            lock=False,
            writemap=False,
            map_async=True,
        )
        self.batch_size = batch_size
        self.count = 0

        # 1. Create a Lock to serialize access from multiple threads
        self.lock = threading.Lock()

        # Start the first transaction
        self.txn = self.env.begin(write=True)

    def put(self, key, value):
        logging.debug("Putting key=%s", key)
        # 2. Acquire lock before touching the transaction or counter
        with self.lock:
            try:
                self.txn.put(orjson.dumps(key), orjson.dumps(value))
            except lmdb.MapFullError:
                self._resize_and_renew_txn()
                self.txn.put(orjson.dumps(key), orjson.dumps(value))
            self.count += 1

            if self.count >= self.batch_size:
                self._commit_internal()
                self.count = 0

    def get(self, key):
        logging.debug("Getting key=%s", key)
        with self.env.begin(write=False) as txn:
            value = txn.get(orjson.dumps(key))
            if value is not None:
                return orjson.loads(value)
            return None

    def commit(self):
        """Public commit method (thread-safe)"""
        with self.lock:
            self._commit_internal()

    def _commit_internal(self):
        """
        Internal commit logic.
        MUST be called while holding self.lock to ensure safety.
        """
        if (
            self.count > 0
        ):  # Only commit if we have pending writes (optional optimization)
            self.txn.commit()
            self.txn = self.env.begin(write=True)
            self.count = 0
        # Note: If count was 0, we could choose to do nothing,
        # but renewing txn is safer to ensure we don't hold an old view forever.
        # For simplicity in this pattern, we just keep the txn alive.

    def _resize_and_renew_txn(self):
        """
        Increase map size and renew writer transaction when map is full.
        MUST be called while holding self.lock.
        """
        try:
            # Preserve writes already buffered in the current transaction.
            self.txn.commit()
        except lmdb.Error:
            self.txn.abort()
        self.count = 0
        self.map_size *= 2
        self.env.set_mapsize(self.map_size)
        logging.warning("LMDB map full, resized to %s bytes", self.map_size)
        self.txn = self.env.begin(write=True)

    def close(self):
        with self.lock:
            if self.count > 0:
                self.txn.commit()  # Commit final data
            else:
                self.txn.abort()  # Abort empty txn

            self.env.sync()
            self.env.close()


class LMDBReader:
    def __init__(self, path, map_size=128 * 1024**3, batch_size=2000):
        self.batch_size = batch_size
        Path(path).parent.mkdir(parents=True, exist_ok=True)
        self.env = lmdb.open(
            path,
            map_size=map_size,
            subdir=False,
            lock=False,
            readonly=True,
            readahead=True,
            max_readers=batch_size,
        )

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

    def iterate(self):
        with self.env.begin(write=False) as txn:
            cursor = txn.cursor()
            for key, value in cursor:
                yield orjson.loads(value)

    @property
    def total_count(self):
        with self.env.begin(write=False) as txn:
            stats = txn.stat()
            count = stats["entries"]
        return count

    def close(self):
        self.env.close()
