import uuid
import falcon
from falcon import testing
from app.auth.sessions import RoleType
from app.domain.models import Whitelist
from app.domain.services_setting import ServicesSetting
from app.rest.resources.resource_white_list import WhiteListResource


def test_get(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = WhiteListResource(service=service)
    app.add_route("/white-list", res)
    app.add_route("/white-list/{oid:int}", res)
    client = testing.TestClient(app)

    # Get all
    resp = client.simulate_get(f"/white-list", headers={"Authorization": token})
    assert resp.status == falcon.HTTP_OK

    # Get a specific row
    data_json = {"username": f"foo:{token}", "role": "admin"}
    user = Whitelist(**data_json)
    user.save(force_insert=True)

    resp = client.simulate_get(
        f"/white-list/{user.id}", headers={"Authorization": token}
    )

    assert resp.status == falcon.HTTP_OK

    user_dict = resp.json
    assert user_dict
    assert user_dict.get("username") == user.username


def test_post(env_args, session_manager):
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
    new_user_data = resp.json
    assert new_user_data
    assert new_user_data.get("id")
    assert new_user_data.get("username") == data_json.get("username")
    assert new_user_data.get("role") == data_json.get("role")


def test_put(env_args, session_manager):
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
    user_instance = Whitelist(**data_json)
    user_instance.save(force_insert=True)
    data_json["id"] = user_instance.id

    resp = client.simulate_put(
        "/white-list", json=data_json, headers={"Authorization": token}
    )

    assert resp.status == falcon.HTTP_OK
    new_user_data = resp.json
    assert new_user_data
    assert new_user_data.get("id") == user_instance.id
    assert new_user_data.get("username") == data_json.get("username")
    assert new_user_data.get("role") == data_json.get("role")


def test_delete(env_args, session_manager):
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
