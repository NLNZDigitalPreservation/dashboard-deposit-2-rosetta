from typing import Optional

import falcon
import orjson
from playhouse.shortcuts import model_to_dict

from app.auth.sessions import RoleType, SessionManager
from app.domain.services_setting import ServicesSetting
from app.domain.models import DepositAccount


class RawProducersResource:
    def __init__(self, rosetta):
        self.rosetta: ServicesSetting = rosetta

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
