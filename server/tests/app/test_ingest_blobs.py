import pytest

from tests.utils import fixture_utils


@pytest.mark.skip
def test_ingest_blobs(env_args):
    fixture_utils.import_files_to_blob_storage(env_args=env_args, directory="/mnt/c/workspace/data/fixity")
