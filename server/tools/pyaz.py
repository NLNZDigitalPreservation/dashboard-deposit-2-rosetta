import json
import logging
import os.path
import shlex
import subprocess
import glob
import random
from pathlib import Path
from dotenv import load_dotenv

from azure.storage.blob import BlobClient, BlobProperties, BlobServiceClient

from common.db.db_access_rosetta import RosettaDatabaseHandler
from common.shared import config
from common.utils import log_utils
from tests.utils import fixture_utils
from tests.utils.fixity_utils import md5_checksum

load_dotenv(dotenv_path="./tests/.env")


def parse_args():
    parser = config.Parser(description="Blob Storage Deposit Tool")
    parser.add_log_level()
    parser.add_rosetta_arguments()
    parser.add_blob_storage_arguments()
    parser.add_pyaz_arguments()

    args = parser.parse_known_args()[0]

    args.connection_string = f"DefaultEndpointsProtocol=http;AccountName={args.account_name};AccountKey={args.account_key};BlobEndpoint=http://localhost:10000/{args.account_name};"

    return args


def call_command(args, cmd_str: str):
    cmd_str += f' --connection-string "{args.connection_string}"'

    cmd_list = shlex.split(cmd_str)
    result = subprocess.run(cmd_list, capture_output=True, text=True)

    if result.returncode != 0:
        raise RuntimeError(f"Failed to process: {cmd_str}, error:{result.stderr}")

    return result.stdout


def extract_account_key(connection_string):
    parts = dict(item.split("=", 1) for item in connection_string.strip().split(";") if "=" in item)
    return parts.get("AccountKey")


def fetch_properties(args, blob_url):
    credential = extract_account_key(args.connection_string)
    blob = BlobClient.from_blob_url(blob_url=blob_url, credential=credential)
    return blob.get_blob_properties()


def fetch_properties2(args, blob_name):
    blob_service_client = BlobServiceClient.from_connection_string(args.connection_string)
    blob_client = blob_service_client.get_blob_client(container=args.container_name, blob=blob_name)
    return blob_client.get_blob_properties()


def import_batch_rosetta_db(args, uploaded_batch_result_str):
    rosetta_db = RosettaDatabaseHandler(args)
    data_depositor = fixture_utils.DataIngest(rosetta_db=rosetta_db)

    _, storage_id = data_depositor.get_or_insert_storage_parameter()

    uploaded_batch_blobs = json.loads(uploaded_batch_result_str)
    for blob in uploaded_batch_blobs:
        entity = fixture_utils.FileEntity()
        # entity.blob_url = blob['Blob']
        blob_name = blob["name"]
        entity.blob_url = f"http://localhost:10000/{args.account_name}/{args.container_name}/{blob_name}"
        properties: BlobProperties = fetch_properties2(args, blob_name)
        entity.etag = properties.etag
        entity.index_location = properties.name
        entity.file_size = properties.size
        entity.check_sum_md5 = properties.content_settings.content_md5.hex()

        items = entity.index_location.split("_")
        entity_path = items[0]
        entity.storage_entity_type = "IE" if entity.index_location.endswith("ie.xml") else "FILE"
        if entity.storage_entity_type == "IE":
            entity.stored_entity_id = entity_path.split("/")[0]
        else:
            entity.stored_entity_id = entity_path.split("/")[-1]

        data_depositor.insert_permanent_index(entity=entity, sp_storage_id=storage_id)

    data_depositor.close()


def import_ie_directory(args):
    root_url = f"http://localhost:10000/{args.account_name}/{args.container_name}/"
    rosetta_db = RosettaDatabaseHandler(args)
    data_depositor = fixture_utils.DataIngest(rosetta_db=rosetta_db)

    sp_id, sp_storage_id = data_depositor.get_or_insert_storage_parameter(root_location=root_url)

    root_dir_len = len(args.directory) + 1
    file_list = glob.glob(args.directory + "/**/*", recursive=True)
    rets = []
    for file_path in file_list:
        p = Path(file_path)
        if not p.is_file():
            continue

        file_name = p.name
        if file_name == "ie.xml":
            storage_entity_type = "IE"
            stored_entity_id = p.parent.name
        else:
            storage_entity_type = "FILE"
            idx = file_name.index("_")
            stored_entity_id = file_name[0:idx]

        version = random.randint(1, 11)
        _, ext = os.path.splitext(file_name)

        p_directory = str(p.parent)
        index_location_dir = p_directory[root_dir_len:]
        index_location = os.path.join(index_location_dir, f"V{version}-{stored_entity_id}{ext}")

        format_file_path = f"'{file_path}'"
        cmd = f"az storage blob upload --overwrite --container-name {args.container_name} --name {index_location} --file {format_file_path}"
        ret = call_command(args, cmd)
        logging.debug(ret)

        entity = fixture_utils.FileEntity()
        entity.version = version
        entity.stored_entity_id = stored_entity_id
        entity.storage_entity_type = storage_entity_type
        entity.index_location = index_location
        entity.blob_url = f"{root_url}{index_location}"
        entity.file_size = p.stat().st_size
        entity.check_sum_md5 = md5_checksum(file_path)

        data_depositor.insert_permanent_index(entity=entity, sp_storage_id=sp_storage_id)

        rets.append(entity)
    return rets


def main(args):
    print(args)
    log_utils.init("DEBUG")

    if args.command == "cc":
        cmd = f"az storage container create --name {args.container_name}"
    elif args.command == "dc":
        cmd = f"az storage container delete --name {args.container_name}"
    elif args.command == "lc":
        cmd = f"az storage container list"
    elif args.command == "lb":
        cmd = f"az storage blob list --container-name {args.container_name}"
    elif args.command == "id":
        cmd = f"az storage blob upload-batch --overwrite --destination {args.container_name} --source {args.directory}"
    elif args.command == "if":
        cmd = f"az storage blob upload --overwrite --container-name {args.container_name} --name {args.index_location} --file {args.index_location}"
    elif args.command == "db":
        cmd = f"az storage blob delete --container-name {args.container_name} --name {args.stored_entity_id}"
    elif args.command == "ied":
        import_ie_directory(args)
        return
    else:
        print(f"Unknown command: {args.command}")
        return

    ret = call_command(args, cmd)

    if args.command == "id":
        blobs = call_command(args=args, cmd_str=f'az storage blob list --container-name {args.container_name} --connection-string "{args.connection_string}"')
        import_batch_rosetta_db(args, blobs)

    print(ret)


if __name__ == "__main__":
    _args = parse_args()
    main(_args)
