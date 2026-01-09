from typing import Optional

import falcon
import orjson

from app.auth.sessions import RoleType, SessionManager
from app.data import RepoDepositAccount, DepositAccount
from app.data.repo import DataRepository
from common.utils import helper
from common.utils.dataclass_utils import dataclass_as_camel_dict


class WhiteListResource:
    def __init__(
        self,
        session_manager: SessionManager,
        repo: DataRepository,
    ):
        self.session_manager = session_manager
        self.repo_deposit_account: RepoDepositAccount = repo.deposit_account

    def on_get(self, req: falcon.Request, rsp: falcon.Response, oid: Optional[int] = None):
        if oid is None:
            rsp.status = falcon.HTTP_OK
            rsp.media = self.repo_deposit_account.all_data_as_camel_dict()
        else:
            row = self.repo_deposit_account.get_as_camel_dict(oid)
            if row is None:
                raise falcon.HTTPNotFound(description=f"No user found in the deposit account with id={oid}")
            rsp.status = falcon.HTTP_OK
            rsp.media = row

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        helper.assert_empty("id", data_json)
        helper.assert_not_empty("username", data_json)
        helper.assert_not_empty("role", data_json)

        user = WhiteList(**data_json)
        existing_user = self.repo_white_list.get_by_username(user.username)
        if existing_user is not None:
            raise falcon.HTTPBadRequest(title="Bad Request", description=f"The user {user.username} does exist")

        self.assert_privilege(req, rsp, user=user)
        user = self.repo_white_list.save(user)

        rsp.status = falcon.HTTP_OK
        rsp.media = dataclass_as_camel_dict(user)

    def on_put(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        helper.assert_not_empty("id", data_json)

        user = WhiteList(**data_json)

        self.assert_privilege(req, rsp, user=user)
        user = self.repo_white_list.save(user)
        rsp.status = falcon.HTTP_OK
        rsp.media = dataclass_as_camel_dict(user)

    def on_delete(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        helper.assert_not_empty("id", data_json)

        user = WhiteList(**data_json)

        self.assert_privilege(req, rsp, user=user)
        self.repo_white_list.delete(user.id)

        rsp.status = falcon.HTTP_OK
        rsp.media = dataclass_as_camel_dict(user)
