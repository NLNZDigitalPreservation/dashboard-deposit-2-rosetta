from app.domain.models import DepositAccount
from app.rosetta.rosetta_service import RosettaWebService


def test_get_raw_producers(env_args):
    rosetta = RosettaWebService(env_args)
    deposit_account = DepositAccount(
        depositUserInstitute=env_args.test_institution,
        depositUserName=env_args.test_deposit_account,
        depositUserPassword=env_args.test_deposit_password,
    )
    ret = rosetta.get_producers_raw(deposit_account)
    print(ret)
