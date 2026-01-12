from typing import Optional

import falcon
import orjson

from app.domain.models import Whitelist
from app.domain.services_setting import ServicesSetting


class WhiteListResource:
    def __init__(self, service):
        self.service: ServicesSetting = service

    def on_get(
        self, req: falcon.Request, rsp: falcon.Response, oid: Optional[int] = None
    ):
        if oid is None:
            rsp.status = falcon.HTTP_OK
            rsp.media = Whitelist.select().dicts()
        else:
            row = Whitelist.select().where(Whitelist.id == oid).dicts().first()
            if row is None:
                raise falcon.HTTPNotFound(
                    description=f"No user found in the white list with id={oid}"
                )
            rsp.status = falcon.HTTP_OK
            rsp.media = row

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        token = req.get_header("Authorization")
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        user = self.service.save_white_list(
            token=token, data_json=data_json, is_insert=True
        )

        rsp.status = falcon.HTTP_OK
        rsp.media = Whitelist.select().where(Whitelist.id == user.id).dicts().first()

    def on_put(self, req: falcon.Request, rsp: falcon.Response):
        token = req.get_header("Authorization")
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        user = self.service.save_white_list(
            token=token, data_json=data_json, is_insert=True
        )

        rsp.status = falcon.HTTP_OK
        rsp.media = Whitelist.select().where(Whitelist.id == user.id).dicts().first()

    def on_delete(self, req: falcon.Request, rsp: falcon.Response, oid: int):
        token = req.get_header("Authorization")
        self.service.delete_white_list(token=token, user_id=oid)
        rsp.status = falcon.HTTP_OK
