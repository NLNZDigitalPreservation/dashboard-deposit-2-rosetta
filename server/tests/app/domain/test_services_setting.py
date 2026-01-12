import time
import uuid
from app.auth.sessions import RoleType
from app.domain.services_setting import ServicesSetting


def test_get_global_setting(env_args, session_manager):
    service = ServicesSetting(env_args, session_manager)

    g_setting = service.get_global_setting()

    assert g_setting is not None
    assert g_setting.id == 1


def test_save_global_setting(env_args, session_manager):
    service = ServicesSetting(env_args, session_manager)

    g_setting = service.get_global_setting()

    assert g_setting is not None
    assert g_setting.id == 1

    g_setting.paused = False
    g_setting.save()

    data_json = {
        "paused": True,
        "pausedStartTime": time.time() + 10,
        "pausedEndTime": time.time() + 20,
        "delays": 60,
        "delayUnit": "S",
    }
    g_setting = service.save_global_setting(data_json)
    assert g_setting

    try:
        data_json = {
            "paused": True,
            "pausedStartTime": time.time() + 50,
            "pausedEndTime": time.time() + 20,
            "delays": 60,
            "delayUnit": "S",
        }
        g_setting = service.save_global_setting(data_json)
        assert False
    except:
        assert True

    try:
        data_json = {
            "paused": True,
            "pausedStartTime": time.time() - 30,
            "pausedEndTime": time.time() - 20,
            "delays": 60,
            "delayUnit": "S",
        }
        g_setting = service.save_global_setting(data_json)
        assert False
    except:
        assert True


def test_save_white_list_with_bootstrap(env_args, session_manager):
    token = f"{uuid.uuid4()}"
    service = ServicesSetting(env_args, session_manager)

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    data_json = {"username": f"foo:{token}", "role": "admin"}
    user = service.save_white_list(token=token, data_json=data_json, is_insert=True)
    assert user


def test_save_white_list_with_admin(env_args, session_manager):
    token = f"{uuid.uuid4()}"
    service = ServicesSetting(env_args, session_manager)

    session_manager.add_session(token=token, username="admin", role=RoleType.ADMIN)

    data_json = {"username": f"foo:{token}", "role": "admin"}
    user = service.save_white_list(token=token, data_json=data_json, is_insert=True)
    assert user


def test_save_white_list_with_normal(env_args, session_manager):
    token = f"{uuid.uuid4()}"
    service = ServicesSetting(env_args, session_manager)

    session_manager.add_session(token=token, username="normal", role=RoleType.NORMAL)

    data_json = {"username": f"foo:{token}", "role": "admin"}
    try:
        user = service.save_white_list(token=token, data_json=data_json, is_insert=True)
        assert False
    except:
        assert True


def test_save_white_list_duplicated_added(env_args, session_manager):
    token = f"{uuid.uuid4()}"
    service = ServicesSetting(env_args, session_manager)

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    data_json = {"username": f"foo:{token}", "role": "admin"}
    user = service.save_white_list(token=token, data_json=data_json, is_insert=True)
    assert user

    try:
        user = service.save_white_list(token=token, data_json=data_json, is_insert=True)
        assert False
    except:
        assert True


def test_save_white_list_delete(env_args, session_manager):
    token = f"{uuid.uuid4()}"
    service = ServicesSetting(env_args, session_manager)

    session_manager.add_session(
        token=token, username="bootstrap", role=RoleType.BOOTSTRAP
    )

    data_json = {"username": f"foo:{token}", "role": "admin"}
    user = service.save_white_list(token=token, data_json=data_json, is_insert=True)
    assert user

    user = service.delete_white_list(token=token, user_id=user.id)
