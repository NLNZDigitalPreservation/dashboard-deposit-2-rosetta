import json
import uuid
import falcon
from falcon import testing
import responses
from app.auth.sessions import RoleType
from app.rest.resources.resource_raw_producer import (
    RawProducerResource,
    RawProducerRequestCommand,
)
from pathlib import Path
from app.domain.models import DepositAccount
from app.rosetta.rosetta_service import RosettaWebService
from common.utils.dataclass_utils import dataclass_as_dict

url = "/raw-producers"


@responses.activate
def test_get(env_args, session_manager, deposit_account, data_resources):
    limit = 10
    offset = 0
    rsp_data = json.loads(
        Path(data_resources).joinpath("producers.json").read_text(encoding="utf-8")
    )

    req_url = f"{env_args.rosetta_dps_url}/producers?limit={limit}&offset={offset}"
    responses.add(responses.GET, req_url, json=rsp_data, status=200)

    token = f"{uuid.uuid4()}"
    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    rosetta = RosettaWebService(env_args)
    # service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = RawProducerResource(rosetta=rosetta)
    app.add_route(url, res)
    app.add_route(url + "/{oid:int}", res)
    client = testing.TestClient(app)

    # Get a specific row
    instance = DepositAccount()
    instance.depositUserInstitute = "INS01"
    instance.depositUserName = f"test:{uuid.uuid4()}"
    instance.depositUserPassword = "******"

    instance.save(force_insert=True)

    cmd = RawProducerRequestCommand(
        depositAccountId=instance.id, limit=limit, offset=offset, name=None
    )
    data_json = dataclass_as_dict(cmd)

    # Get all
    resp = client.simulate_get(url, json=data_json, headers={"Authorization": token})
    assert resp.status == falcon.HTTP_OK

    ret_dict = resp.json
    assert ret_dict
    assert ret_dict.get("producer")
    assert ret_dict.get("total_record_count")
