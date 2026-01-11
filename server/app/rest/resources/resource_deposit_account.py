from typing import Optional

import falcon
import orjson

from app.auth.sessions import RoleType, SessionManager
from app.domain.services_setting import DataServices
from common.utils.dataclass_utils import dataclass_as_dict


class DepositAccountResource:
    def __init__(
        self,
        session_manager: SessionManager,
        repo: DataServices,
    ):
        self.session_manager = session_manager
        self.repo = repo

    def on_get(self, req: falcon.Request, rsp: falcon.Response, oid: Optional[int] = None):
        if oid is None:
            rsp.status = falcon.HTTP_OK
            rsp.media = self.repo.repo_deposit_account.all_data_dict()
        else:
            row = self.repo.repo_deposit_account.get_dict(oid)
            if row is None:
                raise falcon.HTTPNotFound(description=f"No user found in the deposit account with id={oid}")
            rsp.status = falcon.HTTP_OK
            rsp.media = row

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)
        deposit_account = self.repo.save_deposit_account(data_json, is_insert=True)

        rsp.status = falcon.HTTP_OK
        rsp.media = dataclass_as_dict(deposit_account)

    def on_put(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)
        deposit_account = self.repo.save_deposit_account(data_json, is_insert=False)

        rsp.status = falcon.HTTP_OK
        rsp.media = dataclass_as_dict(deposit_account)

    def on_delete(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)
        deposit_account = self.repo.delete_deposit_account(data_json)

        rsp.status = falcon.HTTP_OK
        rsp.media = dataclass_as_dict(deposit_account)
