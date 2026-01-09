import logging

import pytest
from tools import pyaz
from common.utils.blob_storage import BlobStorageAccess
from tests.conftest import env_args
from tests.utils import FileEntity


@pytest.mark.skip
def test_get_properties(env_args, mocked_blobs):
    blobs = mocked_blobs
    assert blobs is not None
    assert len(blobs) >= 1

    blob: FileEntity = blobs[0]
    assert blob is not None
    # blob_storage_access = BlobStorageAccess(env_args)
    # blob_storage_access.get_blob_properties(blob_url=blob.blob_url)
    # blob_properties = pyaz.fetch_properties2(env_args, blob.index_location)

    # "http://localhost:10000/devstoreaccount1/fixity-dev/IE41361816/REP41361817/FL41361818_NLNZ-20250212021007358-00000-24708~wlgqawctweb01.natlib.govt.nz~8443.warc"
    blob_properties: BlobStorageAccess = pyaz.fetch_properties(env_args, blob_url=blob.blob_url)

    logging.info(blob_properties)
