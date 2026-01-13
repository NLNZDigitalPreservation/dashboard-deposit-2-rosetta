import uuid
from playhouse.shortcuts import model_to_dict
import falcon
from falcon import testing
from app.auth.sessions import RoleType
from app.domain.models import DepositAccount
from app.domain.services_setting import ServicesSetting
from app.rest.resources.resource_deposit_account import DepositAccountResource

url = "/deposit-account"


def test_get(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = DepositAccountResource(service=service)
    app.add_route(url, res)
    app.add_route(url + "/{oid:int}", res)
    client = testing.TestClient(app)

    # Get all
    resp = client.simulate_get(url, headers={"Authorization": token})
    assert resp.status == falcon.HTTP_OK

    # Get a specific row
    instance = DepositAccount()
    instance.depositUserInstitute = "INS01"
    instance.depositUserName = f"test:{uuid.uuid4()}"
    instance.depositUserPassword = "******"

    instance.save(force_insert=True)

    resp = client.simulate_get(f"{url}/{instance.id}", headers={"Authorization": token})

    assert resp.status == falcon.HTTP_OK

    ret_dict = resp.json
    assert ret_dict
    assert ret_dict.get("id") == instance.id
    assert ret_dict.get("depositUserName") == instance.depositUserName


def test_post(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = DepositAccountResource(service=service)
    app.add_route(url, res)
    client = testing.TestClient(app)

    instance = DepositAccount()
    instance.depositUserInstitute = "INS01"
    instance.depositUserName = f"test:{uuid.uuid4()}"
    instance.depositUserPassword = "******"
    data_json = model_to_dict(instance)

    resp = client.simulate_post(url, json=data_json, headers={"Authorization": token})

    assert resp.status == falcon.HTTP_OK
    ret_dict = resp.json
    assert ret_dict
    assert ret_dict.get("id")
    assert ret_dict.get("depositUserName") == instance.depositUserName


def test_put(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = DepositAccountResource(service=service)
    app.add_route(url, res)
    client = testing.TestClient(app)

    instance = DepositAccount()
    instance.depositUserInstitute = "INS01"
    instance.depositUserName = f"test:{uuid.uuid4()}"
    instance.depositUserPassword = "******"
    instance.save(force_insert=True)
    data_json = model_to_dict(instance)

    resp = client.simulate_put(url, json=data_json, headers={"Authorization": token})

    assert resp.status == falcon.HTTP_OK
    ret_dict = resp.json
    assert ret_dict
    assert ret_dict.get("id")
    assert ret_dict.get("depositUserName") == instance.depositUserName


def test_delete(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = DepositAccountResource(service=service)
    app.add_route(url + "/{oid:int}", res)
    client = testing.TestClient(app)

    instance = DepositAccount()
    instance.depositUserInstitute = "INS01"
    instance.depositUserName = f"test:{uuid.uuid4()}"
    instance.depositUserPassword = "******"
    instance.save(force_insert=True)

    resp = client.simulate_delete(
        f"{url}/{instance.id}", headers={"Authorization": token}
    )

    assert resp.status == falcon.HTTP_OK
