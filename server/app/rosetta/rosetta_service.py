from typing import List, Optional, Any
from app.rosetta.rosetta_apis import RosettaRestApi
from common.metadata import SipStatusInfo


class RosettaWebService:
    def __init__(self, args):
        self.rest_dps_api = RosettaRestApi(args.rosetta_dps_url)
        self.rest_sip_spi = RosettaRestApi(args.rosetta_sip_url)

    def get_producers_raw(
        self, deposit_account, limit: int = 10, offset: int = 0, name: str = None
    ) -> str:
        if not name:
            path = f"/producers?limit={limit}&offset={offset}"
        else:
            path = f"/producers?limit={limit}&offset={offset}&name={name}"
        return self.rest_dps_api.fetch(deposit_account, "GET", path, None)

    def get_producers(self, deposit_account) -> List[dict]:
        producers = []
        offset = 0
        while True:
            ret = self.rest_dps_api.fetch(
                deposit_account, "GET", f"/producers?limit=100&offset={offset}", None
            )
            rsp = self._json_to_dict(ret)

            self.log.debug(f"Got producers, offset={offset}")

            if rsp and rsp.get("total_record_count", 0) > 0 and rsp.get("producer"):
                producers.extend(rsp.get("producer"))
                offset += 1  # In Rosetta API, offset often refers to page index or record index
            else:
                break

        self.log.debug(f"{len(producers)} producers with account: {deposit_account}")
        return producers

    def get_producer_profile_id(
        self, deposit_account, producer_id: str
    ) -> Optional[str]:
        ret = self.rest_dps_api.fetch(
            deposit_account, "GET", f"/producers/{producer_id}", None
        )
        rsp = self._json_to_dict(ret)

        # Accessing profile -> id from the response JSON
        if rsp and rsp.get("profile"):
            return rsp["profile"].get("id")
        else:
            self.log.error(
                f"Can not find the producer profile with the producer id: {producer_id}"
            )
            return None

    def is_valid_producer(self, deposit_account, producer_id: str) -> bool:
        profile_id = self.get_producer_profile_id(deposit_account, producer_id)
        return bool(profile_id)

    def get_material_flows_raw(
        self,
        deposit_account,
        producer_id: str,
        limit: int,
        offset: int,
        name: str = None,
    ) -> Optional[str]:
        profile_id = self.get_producer_profile_id(deposit_account, producer_id)
        if not profile_id:
            return None

        if not name:
            path = f"/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset}"
        else:
            path = f"/producers/producer-profiles/{profile_id}/material-flows?limit={limit}&offset={offset}&name={name}"

        return self.rest_dps_api.fetch(deposit_account, "GET", path, None)

    def get_material_flows(self, deposit_account, producer_id: str) -> List[dict]:
        material_flows = []
        profile_id = self.get_producer_profile_id(deposit_account, producer_id)

        if not profile_id:
            return material_flows

        offset = 0
        while True:
            rsp = self.rest_dps_api.fetch(
                deposit_account,
                "GET",
                f"/producers/producer-profiles/{profile_id}/material-flows?limit=100&offset={offset}",
                None,
            )

            if (
                rsp
                and rsp.get("total_record_count", 0) > 0
                and rsp.get("profile_material_flow")
            ):
                material_flows.extend(rsp.get("profile_material_flow"))
                offset += 1
            else:
                break
        return material_flows

    def is_valid_material_flow(
        self, deposit_account, producer_id: str, material_flow_id: str
    ) -> bool:
        material_flows = self.get_material_flows(deposit_account, producer_id)
        for flow in material_flows:
            if str(flow.get("id")) == str(material_flow_id):
                return True
        return False

    def deposit(
        self,
        deposit_account,
        injection_root_directory: str,
        deposit_user_producer_id: str,
        material_flow_id: str,
    ):
        try:
            if not self.is_valid_producer(deposit_account, deposit_user_producer_id):
                self.log.warn(f"Invalid producer: {deposit_user_producer_id}")

            if not self.is_valid_material_flow(
                deposit_account, deposit_user_producer_id, material_flow_id
            ):
                self.log.warn(f"Invalid material flow: {material_flow_id}")

            # Constructing the request body as a dictionary (replaces DtoDepositReq)
            req_body = {
                "subdirectory": injection_root_directory,
                "producer": {"id": deposit_user_producer_id},
                "material_flow": {"id": material_flow_id},
            }

            ret = self.rest_dps_api.fetch(
                deposit_account, "POST", "/deposits", req_body
            )

            result = False
            sip_id = ""
            sip_reason = ""

            if ret:
                status = ret.get("status", "").lower()
                if status not in ["rejected", "declined"]:
                    sip_id = ret.get("sip_id", "")
                    result = bool(sip_id)

                sip_reason = ret.get("sip_reason", "")

            # Assuming ResultOfDeposit has a static create method or constructor
            return result, sip_id, sip_reason

        except Exception as e:
            self.log.error(
                f"Deposit failed: {deposit_account} {injection_root_directory} {deposit_user_producer_id} {material_flow_id}"
            )
            raise e

    def get_sip_status_info(self, deposit_account, sip_id: str):
        ret = self.rest_sip_spi.fetch(deposit_account, "GET", f"/sips/{sip_id}", None)
        # In Python, we can just return the dict or wrap it in a SipStatusInfo object
        return SipStatusInfo(**ret) if ret else None
