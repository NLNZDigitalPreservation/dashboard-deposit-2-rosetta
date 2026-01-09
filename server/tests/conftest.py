import logging
import os
from pathlib import Path

import pytest
from dotenv import load_dotenv
from app.data.models import db_manager
from common.db.db_access_fixity import FixityDatabaseHandler
from common.db.db_access_rosetta import RosettaDatabaseHandler
from common.shared import config, exiting
from common.utils import log_utils

# import pyaz
from common.utils.blob_storage import BlobStorageAccess
from tools import ingest, pyaz
from tools.oracle_models import oracle_db_manager


env_path = Path.cwd() / ".env"
load_dotenv()

logging.getLogger("azure.core").setLevel(logging.WARNING)
log_utils.init()


@pytest.fixture(scope="session")
def env_args():
    parser = config.Parser(description="This is the fixity")

    parser.add_log_level()
    parser.add_dbms_arguments()
    parser.add_rosetta_arguments()
    parser.add_blob_storage_arguments()
    parser.add_app_main_arguments(api_port=3000)
    parser.add_pyaz_arguments()
    parser.add_ldap_arguments()

    args = parser.parse_known_args()[0]

    args.connection_string = f"DefaultEndpointsProtocol=http;AccountName={args.account_name};AccountKey={args.account_key};BlobEndpoint=http://localhost:10000/{args.account_name};"

    # Create the blob container
    blob_client = BlobStorageAccess(args=args)
    blob_client.init(args.connection_string, args.container_name)

    yield args


@pytest.fixture(scope="session", autouse=True)
def rosetta_db(env_args):
    db_handler = RosettaDatabaseHandler(env_args)
    logging.info("Initialized the rosetta db handler")
    # fixture_utils.import_files_to_blob_storage(env_args=env_args, directory="/mnt/c/workspace/data/fixity")
    yield db_handler
    exiting.add_before_exit_callback(db_handler.close)


@pytest.fixture(scope="session")
def fixity_db(env_args):
    db_handler = FixityDatabaseHandler(env_args)
    yield db_handler
    exiting.add_before_exit_callback(db_handler.close)


@pytest.fixture(scope="session")
def durable_func_req():
    req_json = {"start_pi_id": -1, "batch_size": 30}
    return req_json


@pytest.fixture(scope="session")
def data_resources():
    # Absolute path of the script
    script_path = os.path.abspath(__file__)
    test_dir = Path(script_path).parent
    data_resources_dir = os.path.join(test_dir, "data_resources")

    yield data_resources_dir


@pytest.fixture(scope="session")
def peewee_db(env_args):
    db_manager.initializae_models(args=env_args)
    yield db_manager

    db_manager.close()


@pytest.fixture(scope="session")
def peewee_oracle(env_args):
    oracle_db_manager.initialize_models(args=env_args)
    yield oracle_db_manager

    oracle_db_manager.close()


@pytest.fixture(scope="session")
def mocked_blobs(env_args, data_resources, peewee_db):
    env_args.command = "ied"
    env_args.directory = data_resources

    rets = pyaz.import_ie_directory(env_args)

    yield rets


@pytest.fixture(scope="session")
def mocked_files(env_args, data_resources):
    env_args.directory = data_resources
    rets = ingest.deposit(env_args)
    yield rets
