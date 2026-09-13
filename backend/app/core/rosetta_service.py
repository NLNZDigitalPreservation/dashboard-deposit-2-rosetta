import logging
from dataclasses import dataclass
from typing import Optional

from app.core.rosetta_rest_api import RosettaRestApi
from common.data.models import DepositAccount


@dataclass
class SipStatusInfo:
    link: Optional[str] = None
    id: Optional[str] = None
    externalId: Optional[str] = None
    externalSystem: Optional[str] = None
    module: str = "HUB"
    stage: Optional[str] = None
    status: Optional[str] = None
    numberOfIEs: Optional[str] = None
    iePids: Optional[str] = None


@dataclass
class ResultOfDeposit:
    is_successful: bool
    sip_id: Optional[str]
    result_message: Optional[str]

    @staticmethod
    def create(success: bool, sip_id: Optional[str], result_message: Optional[str]):
        return ResultOfDeposit(success, sip_id, result_message)

    def isSuccess(self) -> bool:
        return self.is_successful

    def getResultMessage(self) -> Optional[str]:
        return self.result_message

    def getSipID(self) -> Optional[str]:
        return self.sip_id


@dataclass
class DtoDepositRsp:
    id: Optional[str] = None
    creation_date: Optional[str] = None
    submission_date: Optional[str] = None
    update_date: Optional[str] = None
    status: Optional[str] = None
    title: Optional[str] = None
    sip_id: Optional[str] = None


class RosettaService:
    def __init__(self, args):
        self.args = args
        self.dps_api = RosettaRestApi(args.rosetta_rest_api_dps_url)
        self.sip_api = RosettaRestApi(args.rosetta_rest_api_sip_url)

    def get_producers(
        self,
        deposit_account: DepositAccount,
        limit: int = 100,
        offset: int = 0,
        name: Optional[str] = None,
    ):
        if not name:
            path = f"/producers?limit={limit}&offset={offset}"
        else:
            path = f"/producers?limit={limit}&offset={offset}&name={name}"
        return self.dps_api.fetch(deposit_account, "GET", path)

    def get_producer_profile_id(
        self, deposit_account, producer_id: str
    ) -> Optional[str]:
        ret = self.dps_api.fetch(deposit_account, "GET", f"/producers/{producer_id}")
        if ret is not None and ret.get("profile") is not None:
            return ret.get("profile").get("id")
        logging.error(
            "Can not find the producer profile with the producer id: %s", producer_id
        )
        return None

    def is_valid_producer(self, deposit_account, producer_id: str) -> bool:
        return not self._is_empty(
            self.get_producer_profile_id(deposit_account, producer_id)
        )

    def get_material_flows(
        self,
        deposit_account,
        producer_id: str,
        limit: int = 100,
        offset: int = 0,
        name: Optional[str] = None,
    ):
        profile_id = self.get_producer_profile_id(deposit_account, producer_id)
        if self._is_empty(profile_id):
            return None

        if not name:
            path = f"/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset}"
        else:
            path = f"/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset}&name={name}"
        return self.dps_api.fetch(deposit_account, "GET", path)

    def deposit(
        self,
        deposit_account,
        injection_root_directory: str,
        deposit_user_producer_id: str,
        material_flow_id: str,
    ) -> ResultOfDeposit:
        try:
            if not self.is_valid_producer(deposit_account, deposit_user_producer_id):
                logging.warning("Invalid producer: %s", deposit_user_producer_id)
                return ResultOfDeposit.create(False, "", "Invalid producer")

            req_body = {
                "link": "string",
                "subdirectory": injection_root_directory,
                "producer": {"value": deposit_user_producer_id},
                "material_flow": {"value": material_flow_id},
            }

            rsp = self.dps_api.fetch(deposit_account, "POST", "/deposits", req_body)

            result = False
            sip_id = ""
            sip_reason = ""
            if rsp is not None:
                if not self._equals_ignore_case(
                    rsp.get("status"), "Rejected"
                ) and not self._equals_ignore_case(rsp.get("status"), "Declined"):
                    result = not self._is_empty(rsp.get("sip_id"))
                sip_id = rsp.get("sip_id") or ""
                sip_reason = rsp.get("sip_reason") or ""

            return ResultOfDeposit.create(result, sip_id, sip_reason)
        except Exception as exc:
            logging.error(
                "Deposit failed: %s %s %s %s",
                deposit_account,
                injection_root_directory,
                deposit_user_producer_id,
                material_flow_id,
            )
            raise

    def get_sip_status_info(
        self, deposit_account, sip_id: str
    ) -> Optional[SipStatusInfo]:
        rsp = self.sip_api.fetch(deposit_account, "GET", f"/sips/{sip_id}")
        return self._build_object(rsp, SipStatusInfo)

    def set_dps_rest_api(self, dps_rest_api_url: str):
        self.dps_rest_api_url = dps_rest_api_url

    def set_sip_rest_api(self, sip_rest_api_url: str):
        self.sip_rest_api_url = sip_rest_api_url

    def set_auth_client(self, auth_client):
        self.auth_client = auth_client

    @staticmethod
    def _is_empty(value: Optional[str]) -> bool:
        return value is None or len(str(value)) == 0

    @staticmethod
    def _equals_ignore_case(left: Optional[str], right: Optional[str]) -> bool:
        if left is None or right is None:
            return left == right
        return str(left).lower() == str(right).lower()

    @staticmethod
    def _build_object(payload, target_class):
        if payload is None:
            return None
        if isinstance(payload, target_class):
            return payload
        if isinstance(payload, dict):
            return target_class(**payload)
        return target_class(payload)
