import json
from pathlib import Path
from types import SimpleNamespace

import pytest

from app.core.rosetta_service import ResultOfDeposit, RosettaService, SipStatusInfo
from common.data.models import DepositAccount


def read_fixture(name: str) -> dict:
    fixture_path = Path(__file__).parent / "data_resources" / name
    return json.loads(fixture_path.read_text(encoding="utf-8"))


@pytest.fixture()
def deposit_account():
    return DepositAccount(
        deposit_user_name="NLZNDashboard",
        deposit_user_institute="INS00",
        deposit_user_password="Password01",
    )


@pytest.fixture()
def service():
    args = SimpleNamespace(
        rosetta_rest_api_dps_url="https://example.invalid/dps",
        rosetta_rest_api_sip_url="https://example.invalid/sip",
    )
    return RosettaService(args)


def test_get_producer_profile_id_returns_nested_profile_id(
    service, deposit_account, monkeypatch
):
    monkeypatch.setattr(
        service.dps_api,
        "fetch",
        lambda *_args, **_kwargs: {"profile": {"id": "profile-123"}},
    )

    assert (
        service.get_producer_profile_id(deposit_account, "producer-1") == "profile-123"
    )


def test_get_producer_profile_id_returns_none_for_missing_profile(
    service, deposit_account, monkeypatch
):
    monkeypatch.setattr(service.dps_api, "fetch", lambda *_args, **_kwargs: {})

    assert service.get_producer_profile_id(deposit_account, "producer-1") is None


def test_deposit_returns_success_when_api_returns_sip_id(
    service, deposit_account, monkeypatch
):
    responses = iter(
        [
            {"profile": {"id": "profile-123"}},
            read_fixture("deposit-inprogress.json"),
        ]
    )

    monkeypatch.setattr(
        service.dps_api, "fetch", lambda *_args, **_kwargs: next(responses)
    )

    result = service.deposit(deposit_account, "/inbox/job", "producer-1", "flow-1")

    assert isinstance(result, ResultOfDeposit)
    assert result.isSuccess() is True
    assert result.getSipID() == "12345"
    assert result.getResultMessage() == "accepted"


def test_deposit_returns_failure_when_api_rejects(
    service, deposit_account, monkeypatch
):
    responses = iter(
        [
            {"profile": {"id": "profile-123"}},
            read_fixture("deposit-declined.json"),
        ]
    )

    monkeypatch.setattr(
        service.dps_api, "fetch", lambda *_args, **_kwargs: next(responses)
    )

    result = service.deposit(deposit_account, "/inbox/job", "producer-1", "flow-1")

    assert result.isSuccess() is False
    assert result.getSipID() == "12345"
    assert result.getResultMessage() == "accepted"


def test_get_sip_status_info_builds_dataclass(service, deposit_account, monkeypatch):
    monkeypatch.setattr(
        service.sip_api,
        "fetch",
        lambda *_args, **_kwargs: read_fixture("sipstatusinfo-succeed.json"),
    )

    sip_info = service.get_sip_status_info(deposit_account, "12345")

    assert isinstance(sip_info, SipStatusInfo)
    assert sip_info.id == "12345"
    assert sip_info.stage == "Finished"
    assert sip_info.status == "APPROVED"
