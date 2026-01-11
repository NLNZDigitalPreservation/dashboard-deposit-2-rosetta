import logging
import os
from pathlib import Path

import pytest
from dotenv import load_dotenv
from common.shared import config, exiting
from common.utils import log_utils

from app.domain.models import init_db, close_db

env_path = Path.cwd() / ".env"
load_dotenv()
log_utils.init()


@pytest.fixture(scope="session")
def env_args():
    parser = config.Parser(description="This is the dashboard cases")

    parser.add_log_level()
    parser.add_app_main_arguments(api_port=3000)
    parser.add_ldap_arguments()

    parser.add_env_argument(
        "--test-institution",
        default="INS00",
        help="Test institution code",
    )

    parser.add_env_argument(
        "--test-deposit-account",
        default="testuser",
        help="Test deposit account name",
    )

    parser.add_env_argument(
        "--test-deposit-password",
        default="*******",
        help="Test deposit account password",
    )

    args = parser.parse_known_args()[0]

    yield args


@pytest.fixture(scope="session")
def db_handler(env_args):
    db = init_db("/tmp")
    yield db
    exiting.add_before_exit_callback(close_db)


@pytest.fixture(scope="session")
def data_resources():
    # Absolute path of the script
    script_path = os.path.abspath(__file__)
    test_dir = Path(script_path).parent
    data_resources_dir = os.path.join(test_dir, "data_resources")

    yield data_resources_dir
