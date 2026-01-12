from app.domain.models import DepositAccount
from app.rosetta.rosetta_service import RosettaWebService


def test_get_raw_producers_materials(env_args, deposit_account):
    rosetta = RosettaWebService(env_args)

    producers = rosetta.get_producers_raw(deposit_account, limit=10)
    assert producers is not None
    assert producers.get("producer") is not None
    assert len(producers.get("producer")) == 10
    assert producers.get("total_record_count") is not None
    assert producers.get("total_record_count") > 0

    producer = producers["producer"][0]
    assert producer

    producer_id = producer.get("id")
    assert producer_id is not None

    materials = rosetta.get_material_flows_raw(deposit_account, producer_id=producer_id)
    assert materials is not None
    assert materials.get("profile_material_flow") is not None
    assert len(materials.get("profile_material_flow")) > 0
    assert materials.get("total_record_count") is not None
    assert materials.get("total_record_count") > 0


def test_deposit_none_existing_path(env_args, deposit_account):
    rosetta = RosettaWebService(env_args)
    result, _, _ = rosetta.deposit(
        deposit_account=deposit_account,
        injection_root_directory="/tmp",
        deposit_user_producer_id=env_args.test_producer_id,
        material_flow_id=env_args.test_material_flow_id,
    )
    assert result is False


def test_sip_status_polling(env_args, deposit_account):
    rosetta = RosettaWebService(env_args)
    sip_status = rosetta.get_sip_status_info(
        deposit_account=deposit_account, sip_id=env_args.test_sip_id
    )
    assert sip_status is not None
    assert sip_status.module is not None
