import uuid
import falcon
from falcon import testing
from app.auth.sessions import RoleType
from app.domain.models import Whitelist
from app.domain.services_setting import ServicesSetting
from app.rest.resources.resource_white_list import WhiteListResource


def test_resource_white_list_get(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = WhiteListResource(service=service)
    app.add_route("/white-list/{oid:int}", res)
    client = testing.TestClient(app)

    data_json = {"username": f"foo:{token}", "role": "admin"}
    user = Whitelist(**data_json)
    user.save(force_insert=True)

    resp = client.simulate_get(
        f"/white-list/{user.id}", json=data_json, headers={"Authorization": token}
    )

    assert resp.status == falcon.HTTP_OK

    user_dict = resp.json
    assert user_dict
    assert user_dict.get("username") == user.username


def test_resource_white_list_post(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = WhiteListResource(service=service)
    app.add_route("/white-list", res)
    client = testing.TestClient(app)

    data_json = {"username": f"foo:{token}", "role": "admin"}

    resp = client.simulate_post(
        "/white-list", json=data_json, headers={"Authorization": token}
    )

    assert resp.status == falcon.HTTP_OK
    print(resp.text)


def test_resource_white_list_delete(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = WhiteListResource(service=service)
    app.add_route("/white-list/{oid:int}", res)
    client = testing.TestClient(app)

    data_json = {"username": f"foo:{token}", "role": "admin"}
    user = Whitelist(**data_json)
    user.save(force_insert=True)

    resp = client.simulate_delete(
        f"/white-list/{user.id}", headers={"Authorization": token}
    )

    assert resp.status == falcon.HTTP_OK
