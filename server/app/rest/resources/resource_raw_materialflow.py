from typing import Optional
from dataclasses import dataclass
import falcon
import orjson

from app.domain.models import DepositAccount
from app.rosetta.rosetta_service import RosettaWebService


@dataclass
class RawMaterialFlowRequestCommand:
    depositAccountId: int
    producerId: str
    offset: int
    limit: int
    name: str


class RawMaterialFlowResource:
    def __init__(self, rosetta):
        self.rosetta: RosettaWebService = rosetta

    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        cmd = RawMaterialFlowRequestCommand(**data_json)

        deposit_account = DepositAccount.get_or_none(
            DepositAccount.id == cmd.depositAccountId
        )
        if deposit_account is None:
            raise falcon.HTTPBadRequest(
                description=f"There is no Deposit Account related to the id {cmd.depositAccountId}"
            )

        rows = self.rosetta.get_material_flows_raw(
            deposit_account=deposit_account,
            producer_id=cmd.producerId,
            limit=cmd.limit,
            offset=cmd.offset,
            name=cmd.name,
        )

        rsp.status = falcon.HTTP_OK
        rsp.media = rows

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        self.on_get(req, rsp)
