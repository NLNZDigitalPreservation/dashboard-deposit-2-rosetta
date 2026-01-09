import logging
from typing import Optional

import falcon

from common.db import blob_storage_access as bs
from common.metadata import DEFAULT_CONTAINER_NAME


class BlobStorageResource:
    def __init__(self, blob_storage_access):
        pass

    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        rsp.status_code = falcon.HTTP_OK
        rsp.media = bs.get_properties_of_all_blobs(container_name=DEFAULT_CONTAINER_NAME)

    def on_post(self, req: falcon.Request, rsp: falcon.Response, blob_name: Optional[str]):
        form = req.get_media()
        for part in form:
            logging.info(f"received file: {blob_name} {part.filename} {part.content_type}")
            bs.put_blob_data(
                blob_name=blob_name,
                data=part.stream,
                container_name=DEFAULT_CONTAINER_NAME,
                force_overwrite=True,
            )
            break
        rsp.status_code = falcon.HTTP_OK
        rsp.media = f"Put {blob_name} to blob storage"

    def on_delete(self, req: falcon.Request, rsp: falcon.Response, blob_name: Optional[str]):
        bs.delete_blob(blob_name=blob_name, container_name=DEFAULT_CONTAINER_NAME)
        rsp.status_code = falcon.HTTP_OK
        rsp.media = True
