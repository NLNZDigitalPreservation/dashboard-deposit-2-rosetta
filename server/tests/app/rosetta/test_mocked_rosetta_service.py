import responses
from pathlib import Path
import json
from app.domain.models import DepositAccount
from app.rosetta.rosetta_service import RosettaWebService
from common.metadata import SipStatusInfo


@responses.activate
def test_get_raw_producers(env_args, deposit_account, data_resources):
    limit = 10
    offset = 0
    rsp_data = json.loads(
        Path(data_resources).joinpath("producers.json").read_text(encoding="utf-8")
    )

    req_url = f"{env_args.rosetta_dps_url}/producers?limit={limit}&offset={offset}"
    responses.add(responses.GET, req_url, json=rsp_data, status=200)

    rosetta = RosettaWebService(env_args)

    producers = rosetta.get_producers_raw(deposit_account, limit=limit, offset=offset)
    assert producers is not None
    assert producers.get("producer") is not None
    assert len(producers.get("producer")) == 10
    assert producers.get("total_record_count") is not None
    assert producers.get("total_record_count") > 0


@responses.activate
def test_get_raw_materials(env_args, deposit_account, data_resources):
    producer_id = "953094554"
    profile_id = "4593040323"
    limit = 10
    offset = 0

    responses.add(
        responses.GET,
        f"{env_args.rosetta_dps_url}/producers/{producer_id}",
        json={"profile": {"id": profile_id}},
        status=200,
    )

    rsp_data = json.loads(
        Path(data_resources).joinpath("materialflows.json").read_text(encoding="utf-8")
    )

    req_url = f"{env_args.rosetta_dps_url}/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset}"
    responses.add(responses.GET, req_url, json=rsp_data, status=200)

    rosetta = RosettaWebService(env_args)

    materials = rosetta.get_material_flows_raw(
        deposit_account, producer_id=producer_id, limit=limit, offset=offset
    )
    assert materials is not None
    assert materials.get("profile_material_flow") is not None
    assert len(materials.get("profile_material_flow")) > 0
    assert materials.get("total_record_count") is not None
    assert materials.get("total_record_count") > 0


@responses.activate
def test_deposit_accepted(env_args, deposit_account, data_resources):
    producer_id = "741697717"
    material_id = "853329458"
    profile_id = "741700519"
    limit = 100
    offset = 0

    responses.add(
        responses.GET,
        f"{env_args.rosetta_dps_url}/producers/{producer_id}",
        json={"profile": {"id": profile_id}},
        status=200,
    )

    rsp_data = json.loads(
        Path(data_resources).joinpath("materialflows.json").read_text(encoding="utf-8")
    )

    req_url = f"{env_args.rosetta_dps_url}/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset}"
    responses.add(responses.GET, req_url, json=rsp_data, status=200)

    req_url = f"{env_args.rosetta_dps_url}/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset+1}"
    responses.add(responses.GET, req_url, json=None, status=404)

    responses.add(
        responses.GET,
        f"{env_args.rosetta_dps_url}/producers/{producer_id}",
        json={"profile": {"id": profile_id}},
        status=200,
    )

    rsp_data = json.loads(
        Path(data_resources).joinpath("materialflows.json").read_text(encoding="utf-8")
    )

    req_url = f"{env_args.rosetta_dps_url}/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset}"
    responses.add(responses.GET, req_url, json=rsp_data, status=200)

    rsp_data = json.loads(
        Path(data_resources)
        .joinpath("deposit-accepted.json")
        .read_text(encoding="utf-8")
    )

    req_url = f"{env_args.rosetta_dps_url}/deposits"
    responses.add(responses.POST, req_url, json=rsp_data, status=200)

    rosetta = RosettaWebService(env_args)
    result, sip_id, sip_reason = rosetta.deposit(
        deposit_account=deposit_account,
        injection_root_directory="/tmp",
        deposit_user_producer_id=producer_id,
        material_flow_id=material_id,
    )
    assert result is True
    assert sip_id


@responses.activate
def test_deposit_declined(env_args, deposit_account, data_resources):
    producer_id = "741697717"
    material_id = "853329458"
    profile_id = "741700519"
    limit = 100
    offset = 0

    responses.add(
        responses.GET,
        f"{env_args.rosetta_dps_url}/producers/{producer_id}",
        json={"profile": {"id": profile_id}},
        status=200,
    )

    rsp_data = json.loads(
        Path(data_resources).joinpath("materialflows.json").read_text(encoding="utf-8")
    )

    req_url = f"{env_args.rosetta_dps_url}/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset}"
    responses.add(responses.GET, req_url, json=rsp_data, status=200)

    req_url = f"{env_args.rosetta_dps_url}/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset+1}"
    responses.add(responses.GET, req_url, json=None, status=404)

    responses.add(
        responses.GET,
        f"{env_args.rosetta_dps_url}/producers/{producer_id}",
        json={"profile": {"id": profile_id}},
        status=200,
    )

    rsp_data = json.loads(
        Path(data_resources).joinpath("materialflows.json").read_text(encoding="utf-8")
    )

    req_url = f"{env_args.rosetta_dps_url}/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset}"
    responses.add(responses.GET, req_url, json=rsp_data, status=200)

    rsp_data = json.loads(
        Path(data_resources)
        .joinpath("deposit-declined.json")
        .read_text(encoding="utf-8")
    )

    req_url = f"{env_args.rosetta_dps_url}/deposits"
    responses.add(responses.POST, req_url, json=rsp_data, status=200)

    rosetta = RosettaWebService(env_args)
    result, sip_id, sip_reason = rosetta.deposit(
        deposit_account=deposit_account,
        injection_root_directory="/tmp",
        deposit_user_producer_id=producer_id,
        material_flow_id=material_id,
    )
    assert result is False
    assert not sip_id


@responses.activate
def test_sip_status_polling_succeed(env_args, deposit_account, data_resources):
    rsp_data = json.loads(
        Path(data_resources)
        .joinpath("sipstatusinfo-succeed.json")
        .read_text(encoding="utf-8")
    )
    req_url = f"{env_args.rosetta_sip_url}/sips/{env_args.test_sip_id}"
    responses.add(responses.GET, req_url, json=rsp_data, status=200)

    rosetta = RosettaWebService(env_args)
    sip_status: SipStatusInfo = rosetta.get_sip_status_info(
        deposit_account=deposit_account, sip_id=env_args.test_sip_id
    )
    assert sip_status is not None
    assert sip_status.module == "HUB"
    assert sip_status.stage == "Finished"
    assert sip_status.status == "APPROVED"


@responses.activate
def test_sip_status_polling_failed(env_args, deposit_account, data_resources):
    rsp_data = json.loads(
        Path(data_resources)
        .joinpath("sipstatusinfo-failed.json")
        .read_text(encoding="utf-8")
    )
    req_url = f"{env_args.rosetta_sip_url}/sips/{env_args.test_sip_id}"
    responses.add(responses.GET, req_url, json=rsp_data, status=200)

    rosetta = RosettaWebService(env_args)
    sip_status = rosetta.get_sip_status_info(
        deposit_account=deposit_account, sip_id=env_args.test_sip_id
    )
    assert sip_status is not None
    assert sip_status.module == "HUB"
    assert sip_status.stage == "Finished"
    assert sip_status.status == "ERROR"


@responses.activate
def test_sip_status_polling_ongoing(env_args, deposit_account, data_resources):
    rsp_data = json.loads(
        Path(data_resources)
        .joinpath("sipstatusinfo-ongoing.json")
        .read_text(encoding="utf-8")
    )
    req_url = f"{env_args.rosetta_sip_url}/sips/{env_args.test_sip_id}"
    responses.add(responses.GET, req_url, json=rsp_data, status=200)

    rosetta = RosettaWebService(env_args)
    sip_status = rosetta.get_sip_status_info(
        deposit_account=deposit_account, sip_id=env_args.test_sip_id
    )
    assert sip_status is not None
    assert sip_status.module == "HUB"
    assert sip_status.stage == "Ongoing"
    assert sip_status.status == "STEP"
