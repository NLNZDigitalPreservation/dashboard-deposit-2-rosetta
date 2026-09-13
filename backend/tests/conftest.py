import logging
import os
import sys
from pathlib import Path
from urllib.parse import urlparse

import pytest

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))


# from common.progress_bar import ProgressManager
from common.utils import log_utils

env_path = Path.cwd() / ".env"

logging.getLogger("azure.core").setLevel(logging.WARNING)
log_utils.init()


@pytest.fixture(scope="session")
def data_resources():
    # Absolute path of the script
    script_path = os.path.abspath(__file__)
    test_dir = Path(script_path).parent
    data_resources_dir = os.path.join(test_dir, "data_resources")
    data_resources_dir += "/"
    yield data_resources_dir
