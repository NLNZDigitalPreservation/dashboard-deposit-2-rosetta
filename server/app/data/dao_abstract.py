from dataclasses import is_dataclass, asdict
import os
import threading
from common.db.lmdb_provider import LMDBWriter, LMDBReader


class IdGenerator:
    def __init__(self, args, table_name):
        self.persistent_storage = os.path.join(args.persistent_storage, "id_generator")
        self.db_writer = LMDBWriter(self.persistent_storage)
        self.db_reader = LMDBReader(self.persistent_storage)
        self.table_name = table_name
        self.lock = threading.Lock()

    def next_id(self):
        with self.lock:
            current_id = self.db_reader.get(self.table_name)
            if current_id is None:
                current_id = 0
            new_id = current_id + 1
            self.db_writer.put(self.table_name, new_id)
            return new_id


class DAOAbstract:
    def __init__(self, args, table_name, data_clazz):
        self.data_clazz = data_clazz
        self.persistent_storage = os.path.join(args.persistent_storage, table_name)
        self.db_writer = LMDBWriter(self.persistent_storage)
        self.db_reader = LMDBReader(self.persistent_storage)
        self.id_generator = IdGenerator(args, table_name)
        self.lock = threading.Lock()

    def delete(self, data_id):
        data_instance = self.get(data_id)
        self.db_writer.delete(data_id)
        return data_instance

    def save(self, data):
        if data is None:
            return None
        if is_dataclass(data):
            data = asdict(data)

        if data.get("id") is None:
            data["id"] = self.id_generator.next_id()

        self.db_writer.put(data["id"], data)
        return self.data_clazz(**data)

    def get(self, data_id):
        data = self.db_reader.get(data_id)
        if data is None:
            return None
        return self.data_clazz(**data)

    def get_dict(self, data_id):
        data = self.db_reader.get(data_id)
        return data

    def all_keys(self):
        return self.db_reader.all_keys()

    def all_data(self):
        datasets = []
        for data in self.db_reader.all_data():
            datasets.append(self.data_clazz(**data))
        return datasets

    def all_data_dict(self):
        datasets = []
        for data in self.db_reader.all_data():
            datasets.append(data)
        return datasets

    def close(self):
        self.db_writer.close()
        self.db_reader.close()
