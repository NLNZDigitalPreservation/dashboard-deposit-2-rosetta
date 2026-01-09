import logging
import os.path
import glob
import random
from pathlib import Path
from dotenv import load_dotenv
from peewee import fn


from common.shared import config
from common.utils import log_utils
from tests.utils.fixity_utils import md5_checksum
from app.data.models import PermanentIndex

load_dotenv(dotenv_path="./tests/.env")


def parse_args():
    parser = config.Parser(description="Blob Storage Deposit Tool")
    parser.add_log_level()
    parser.add_rosetta_arguments()
    parser.add_blob_storage_arguments()
    # parser.add_pyaz_arguments()

    parser.add_env_argument(
        "--file-path",
        default="",
        help="The path of the file",
    )

    parser.add_env_argument(
        "--directory",
        default="",
        help="The path of the directory",
    )

    args = parser.parse_known_args()[0]

    return args


def get_ingest_entity(file_path: str):
    if not file_path:
        return None

    p = Path(file_path)
    if not p.is_file():
        return None

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

    PermanentIndex.delete().where(PermanentIndex.stored_entity_id == stored_entity_id).execute()

    max_id = PermanentIndex.select(fn.MAX(PermanentIndex.id)).scalar()
    if max_id is None:
        max_id = 1
    else:
        max_id += 1

    entity = PermanentIndex()
    entity.id = max_id
    entity.index_location = file_path
    entity.version = version
    entity.stored_entity_id = stored_entity_id
    entity.storage_entity_type = storage_entity_type
    entity.file_size = p.stat().st_size
    entity.check_sum_md5 = md5_checksum(file_path=file_path)

    entity.save(force_insert=True)

    return entity


def deposit(args):
    rets = []
    entity = get_ingest_entity(args.file_path)
    if entity is not None:
        rets.append(entity)

    if args.directory:
        file_list = glob.glob(args.directory + "/**/*", recursive=True)
    else:
        file_list = []

    for file_path in file_list:
        entity = get_ingest_entity(file_path)
        if entity is not None:
            rets.append(entity)

    logging.info(f"Ingested the file: {args.file_path}, directory: {args.directory}")
    return rets


def main(args):
    print(args)
    log_utils.init("DEBUG")
    deposit(args)


if __name__ == "__main__":
    _args = parse_args()
    main(_args)
