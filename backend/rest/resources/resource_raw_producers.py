from dataclasses import dataclass
from typing import Optional

import falcon
import orjson
from playhouse.shortcuts import model_to_dict

from app.auth.sessions import RoleType, SessionManager
from common.data.models import WhiteList, DepositAccount
from common.utils import helper


@dataclass
class RawProducersCommand:
    deposit_account_id: int
    offset: int
    limit: int
    name: Optional[str] = None


class RawProducerResource:
    def __init__(self, session_manager: SessionManager):
        self.session_manager = session_manager

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        cmd: RawProducersCommand = orjson.loads(
            req.stream.read(), object_hook=RawProducersCommand
        )
        deposit_account = DepositAccount.get_or_none(DepositAccount.id == cmd.deposit_account_id)
        if deposit_account is None:
            raise falcon.HTTPNotFound(
                description=f"No deposit account found with id={cmd.deposit_account_id}"
            )

    
        

        if oid is None:
            rsp.status = falcon.HTTP_OK
            rsp.media = list(WhiteList.select().dicts())
        else:
            row = WhiteList.get_or_none(WhiteList.id == oid)
            if row is None:
                raise falcon.HTTPNotFound(
                    description=f"No user found in the white list with id={oid}"
                )
            rsp.status = falcon.HTTP_OK
            rsp.media = model_to_dict(row)
