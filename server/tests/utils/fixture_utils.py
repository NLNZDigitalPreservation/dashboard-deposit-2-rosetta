import logging
from dataclasses import dataclass
import requests
from datetime import datetime
from common.db.db_access_rosetta import RosettaDatabaseHandler
from tools import pyaz
from tools.oracle_models import RosettaPermanentIndex

ROOT_DIR = "http://localhost:10000/devstoreaccount1/fixity-dev/"


@dataclass
class FileEntity:
    index_location: str = None
    file_name: str = None
    storage_entity_type: str = "IE"
    stored_entity_id: str = None
    check_sum_md5: str = None
    file_size: int = None
    etag: str = None
    blob_url: str = None
    version: int = 1


def blob_event(entity: FileEntity):
    data = {
        "topic": "/subscriptions/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/resourceGroups/myrg/providers/Microsoft.Storage/storageAccounts/myblobstorageaccount",
        "subject": f"{entity.index_location}",
        "eventType": "Microsoft.Storage.BlobCreated",
        "eventTime": "2017-08-16T20:33:51.0595757Z",
        "id": "4d96b1d4-0001-00b3-58ce-16568c064fab",
        "data": {
            "api": "PutBlockList",
            "clientRequestId": "d65ca2e2-a168-4155-b7a4-2c925c18902f",
            "requestId": "4d96b1d4-0001-00b3-58ce-16568c000000",
            "eTag": f"{entity.etag}",
            "contentType": "text/plain",
            "contentLength": 0,
            "blobType": "BlockBlob",
            "url": f"{entity.blob_url}",
            "sequencer": "00000000000000EB0000000000046199",
            "storageDiagnostics": {"batchId": "dffea416-b46e-4613-ac19-0371c0c5e352"},
        },
        "dataVersion": "",
        "metadataVersion": "1",
    }
    try:
        requests.post(url="http://localhost:3000/api/webhook", json=data)
        logging.info(f"Trigger a blob event: {entity.blob_url}")
    except Exception as ex:
        logging.warning(f"Failed to trigger a blob event: {entity.blob_url}")


class DataIngest:
    ie = "IE41361816"

    def __init__(self, rosetta_db: RosettaDatabaseHandler):
        self.rosetta_db = rosetta_db
        self.conn = rosetta_db.create_connection()

    def close(self):
        self.rosetta_db.close_connection(self.conn)

    def _get_root_location_from_storage_parameter(self, root_location=ROOT_DIR):
        sql = f"""
                           SELECT sp.id, sp.storage_id
                           FROM {self.rosetta_db.rosetta_db_schemaprefix}_SHR00.storage_parameter sp
                           WHERE sp.value='{root_location}' and sp.key='DIR_ROOT'
                       """
        with self.conn.cursor() as cursor:
            cursor.execute(sql)

            rosetta_rows = cursor.fetchall()
            if len(rosetta_rows) == 0:
                return None, None
            for row in rosetta_rows:
                return row[0], row[1]

    def get_or_insert_storage_parameter(self, root_location=ROOT_DIR):
        sp_id, sp_storage_id = self._get_root_location_from_storage_parameter(root_location)
        if sp_id is not None and sp_storage_id is not None:
            return sp_id, sp_storage_id

        sql = f"""
                       INSERT INTO {self.rosetta_db.rosetta_db_schemaprefix}_SHR00.storage_parameter
                       (value, key, storage_id)
                       VALUES
                       ('{root_location}', 'DIR_ROOT', {int(datetime.now().timestamp())})
                   """

        with self.conn.cursor() as cursor:
            cursor.execute(sql)
            self.conn.commit()
            sp_id, sp_storage_id = self._get_root_location_from_storage_parameter(root_location=root_location)
            return sp_id, sp_storage_id

    def is_permanent_index_existing(self, entity: FileEntity):
        sql = f"""
                   SELECT pi.id
                   FROM {self.rosetta_db.rosetta_db_schemaprefix}_PER00.permanent_index pi
                   WHERE pi.stored_entity_id='{entity.stored_entity_id}'
               """

        with self.conn.cursor() as cursor:
            logging.debug(sql)
            cursor.execute(sql)

            rows = cursor.fetchall()
            return rows

    def overwrite_permanent_index_existing(self, existing_rows):
        sql = f"""
                           DELETE
                           FROM {self.rosetta_db.rosetta_db_schemaprefix}_PER00.permanent_index pi
                           WHERE pi.id=:1
                       """

        with self.conn.cursor() as cursor:
            logging.debug(sql)
            cursor.executemany(sql, existing_rows)
            self.conn.commit()
        logging.info("Deleted the existing rows")

    def insert_permanent_index(self, entity: FileEntity, sp_storage_id):
        # existing_rows = self.is_permanent_index_existing(entity)
        # if len(existing_rows) > 0:
        #     self.overwrite_permanent_index_existing(existing_rows)
        #     logging.info(f"Overwrote the existing entity: {entity.index_location}")
        # Delete the existing rows
        # query = RosettaPermanentIndex.delete().where(RosettaPermanentIndex.stored_entity_id == entity.stored_entity_id)
        # query.execute()
        query = RosettaPermanentIndex.select().where(RosettaPermanentIndex.stored_entity_id == entity.stored_entity_id)
        for data in query:
            RosettaPermanentIndex.delete_by_id(data.id)

        if sp_storage_id is None:
            sp_storage_id = 0

        sql = f"""
                    INSERT INTO {self.rosetta_db.rosetta_db_schemaprefix}_PER00.permanent_index 
                    (index_location, file_size, check_sum, check_sum_type, phys_check_sum, phys_check_sum_type,
                       storage_entity_type, storage_id, stored_entity_id, version) 
                    VALUES 
                    ('{entity.index_location}', {entity.file_size}, '{entity.check_sum_md5}', 'MD5', '{entity.check_sum_md5}', 'MD5',
                    '{entity.storage_entity_type}', {sp_storage_id}, '{entity.stored_entity_id}', {entity.version})
                """

        with self.conn.cursor() as cursor:
            try:
                logging.debug(sql)
                cursor.execute(sql)
                self.conn.commit()
                blob_event(entity)
            except Exception as ex:
                logging.exception(ex)
                self.conn.rollback()

    def insert_permanent_indexes(self, dataset):
        sp_id, sp_storage_id = self.get_or_insert_storage_parameter()
        if sp_id is None or sp_storage_id is None:
            logging.error("Failed to get or insert the storage parameter")
            return

        for entity in dataset:
            self.insert_permanent_index(entity, sp_storage_id)

        self.conn.commit()


@dataclass
class BlobImportCommand:
    command = "id"
    container_name = ""
    directory = ""
    connection_string = ""
    rosetta_db_hostname = None
    rosetta_db_port = None
    rosetta_db_username = None
    rosetta_db_password = None
    rosetta_db_sid = None
    rosetta_db_service_name = None
    rosetta_db_schemaprefix = None


def import_files_to_blob_storage(env_args, directory):
    cmd = BlobImportCommand()
    cmd.rosetta_db_hostname = env_args.rosetta_db_hostname
    cmd.rosetta_db_port = env_args.rosetta_db_port
    cmd.rosetta_db_username = env_args.rosetta_db_username
    cmd.rosetta_db_password = env_args.rosetta_db_password
    cmd.rosetta_db_sid = env_args.rosetta_db_sid
    cmd.rosetta_db_service_name = env_args.rosetta_db_service_name
    cmd.rosetta_db_schemaprefix = env_args.rosetta_db_schemaprefix
    cmd.container_name = env_args.container_name
    cmd.connection_string = env_args.connection_string
    cmd.directory = directory

    pyaz.main(cmd)
