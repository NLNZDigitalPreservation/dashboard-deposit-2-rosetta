import time
import uuid
from playhouse.shortcuts import model_to_dict
import falcon
from falcon import testing
from app.auth.sessions import RoleType
from app.domain.models import GlobalSetting
from app.domain.services_setting import ServicesSetting
from app.rest.resources.resource_global_settings import GlobalSettingsResource


def test_get(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = GlobalSettingsResource(service=service)
    app.add_route("/global-settings", res)
    client = testing.TestClient(app)

    resp = client.simulate_get(f"/global-settings", headers={"Authorization": token})

    assert resp.status == falcon.HTTP_OK

    global_settings_dict = resp.json
    assert global_settings_dict
    assert global_settings_dict.get("id") == 1


def test_post(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = GlobalSettingsResource(service=service)
    app.add_route("/global-settings", res)
    client = testing.TestClient(app)

    resp = client.simulate_get(f"/global-settings", headers={"Authorization": token})
    assert resp.status == falcon.HTTP_OK
    global_settings_dict = resp.json

    global_settings_instance = GlobalSetting(**global_settings_dict)
    global_settings_instance.paused = True
    global_settings_instance.pausedStartTime = time.time() + 60
    global_settings_instance.pausedEndTime = time.time() + 120
    global_settings_instance.delayUnit = "X"

    data_json = model_to_dict(global_settings_instance)

    resp = client.simulate_post(
        "/global-settings", json=data_json, headers={"Authorization": token}
    )

    assert resp.status == falcon.HTTP_OK
    new_user_data = resp.json
    assert new_user_data
    assert new_user_data.get("id") == 1
    assert new_user_data.get("paused") == True
    assert new_user_data.get("delayUnit") == "X"


def test_put(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = GlobalSettingsResource(service=service)
    app.add_route("/global-settings", res)
    client = testing.TestClient(app)

    resp = client.simulate_get(f"/global-settings", headers={"Authorization": token})
    assert resp.status == falcon.HTTP_OK
    global_settings_dict = resp.json

    global_settings_instance = GlobalSetting(**global_settings_dict)
    global_settings_instance.paused = True
    global_settings_instance.pausedStartTime = time.time() + 60
    global_settings_instance.pausedEndTime = time.time() + 120
    global_settings_instance.delayUnit = "X"

    data_json = model_to_dict(global_settings_instance)

    resp = client.simulate_put(
        "/global-settings", json=data_json, headers={"Authorization": token}
    )

    assert resp.status == falcon.HTTP_OK
    new_user_data = resp.json
    assert new_user_data
    assert new_user_data.get("id") == 1
    assert new_user_data.get("paused") == True
    assert new_user_data.get("delayUnit") == "X"


def test_delete(env_args, session_manager):
    token = f"{uuid.uuid4()}"

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    service = ServicesSetting(env_args, session_manager)

    app = falcon.App()
    res = GlobalSettingsResource(service=service)
    app.add_route("/global-settings", res)
    client = testing.TestClient(app)

    try:
        resp = client.simulate_delete(
            f"/global-settings", headers={"Authorization": token}
        )
        assert resp.status != falcon.HTTP_OK
    except falcon.HTTPMethodNotAllowed:
        assert True
