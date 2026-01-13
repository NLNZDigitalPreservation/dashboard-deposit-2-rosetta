from typing import Optional

import falcon
import orjson
from playhouse.shortcuts import model_to_dict

from app.auth.sessions import RoleType, SessionManager
from app.domain.services_setting import ServicesSetting
from app.domain.models import DepositAccount


class DepositAccountResource:
    def __init__(self, service):
        self.service: ServicesSetting = service

    def on_get(
        self, req: falcon.Request, rsp: falcon.Response, oid: Optional[int] = None
    ):
        if oid is None:
            rows = list(DepositAccount.select().dicts())
            rsp.status = falcon.HTTP_OK
            rsp.media = rows
        else:
            row = (
                DepositAccount.select().where(DepositAccount.id == oid).dicts().first()
            )
            if row is None:
                raise falcon.HTTPNotFound(
                    description=f"No user found in the deposit account with id={oid}"
                )
            rsp.status = falcon.HTTP_OK
            rsp.media = row

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        deposit_account = self.service.save_deposit_account(
            data_json=data_json, is_insert=True
        )

        rsp.status = falcon.HTTP_OK
        rsp.media = model_to_dict(deposit_account)

    def on_put(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        deposit_account = self.service.save_deposit_account(
            data_json=data_json, is_insert=False
        )

        rsp.status = falcon.HTTP_OK
        rsp.media = model_to_dict(deposit_account)

    def on_delete(self, req: falcon.Request, rsp: falcon.Response, oid: int):
        self.service.delete_deposit_account(deposit_account_id=oid)
        rsp.status = falcon.HTTP_OK
