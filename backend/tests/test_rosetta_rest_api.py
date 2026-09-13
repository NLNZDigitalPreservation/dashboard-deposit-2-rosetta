import base64

import pytest

from app.core.rosetta_rest_api import RosettaRestApi
from common.data.models import DepositAccount


@pytest.fixture()
def deposit_account():
    return DepositAccount(
        deposit_user_name="NLZNDashboard",
        deposit_user_institute="INS00",
        deposit_user_password="Password01",
    )


def test_basic_auth_header_is_encoded_correctly(deposit_account):
    api = RosettaRestApi("https://example.invalid")

    expected_credential = "NLZNDashboard-institutionCode-INS00:Password01"
    expected_header = "Basic " + base64.b64encode(expected_credential.encode()).decode()

    assert api._basic_auth_header(deposit_account) == expected_header


def test_fetch_raises_on_http_error(monkeypatch, deposit_account):
    api = RosettaRestApi("https://example.invalid")

    class FakeResponse:
        ok = False
        text = "boom"

    def fake_request(**kwargs):
        return FakeResponse()

    monkeypatch.setattr("app.core.rosetta_rest_api.requests.request", fake_request)

    with pytest.raises(RuntimeError, match="Failed to request"):
        api.fetch(deposit_account, "GET", "/status")


def test_fetch_returns_json_payload(monkeypatch, deposit_account):
    api = RosettaRestApi("https://example.invalid")

    class FakeResponse:
        ok = True
        text = "{}"

        @staticmethod
        def json():
            return {"status": "Inprocess"}

    def fake_request(**kwargs):
        return FakeResponse()

    monkeypatch.setattr("app.core.rosetta_rest_api.requests.request", fake_request)

    payload = api.fetch(deposit_account, "GET", "/status")

    assert payload == {"status": "Inprocess"}
