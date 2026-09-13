import base64
import logging

import requests
from common.data.models import DepositAccount

# T = TypeVar("T")


# @dataclass
# class Producer:
#     active: bool = False
#     id: Optional[str] = None
#     name: Optional[str] = None


# @dataclass
# class DtoProducersRsp:
#     producer: Optional[List[Producer]] = None
#     total_record_count: int = 0


# @dataclass
# class ProducerProfile:
#     id: Optional[str] = None
#     name: Optional[str] = None
#     link: Optional[str] = None


# @dataclass
# class DtoProducerDetailRsp:
#     id: Optional[str] = None
#     name: Optional[str] = None
#     link: Optional[str] = None
#     profile: Optional[ProducerProfile] = None


# @dataclass
# class MaterialFlow:
#     active: bool = False
#     id: Optional[str] = None
#     name: Optional[str] = None


# @dataclass
# class DtoMaterialFlowRsp:
#     total_record_count: int = 0
#     profile_material_flow: Optional[List[MaterialFlow]] = None


class RosettaRestApi:
    def __init__(self, root_url):
        self.root_url = root_url.rstrip("/")

    def _basic_auth_header(self, deposit_account) -> str:
        credential = f"{deposit_account.deposit_user_name}-institutionCode-{deposit_account.deposit_user_institute}:{deposit_account.deposit_user_password}"
        token = base64.b64encode(credential.encode("utf-8")).decode("ascii")
        return f"Basic {token}"

    def fetch(
        self, deposit_account: DepositAccount, method: str, path: str, req_body=None
    ):

        url = f"{self.root_url}/{path.lstrip('/')}"
        headers = {
            "Authorization": self._basic_auth_header(deposit_account),
            "Accept": "application/json",
            "Content-Type": "application/json",
        }

        try:
            response = requests.request(
                method=method, url=url, data=req_body, headers=headers, timeout=120
            )
            if not response.ok:
                err = f"Failed to request: {path}, error: {response.text}. Account: {deposit_account}"
                logging.error(err)
                raise RuntimeError(err)
            return response.json()
        except Exception as exc:
            logging.error("Failed to request %s", path, exc_info=exc)
            raise RuntimeError(exc) from exc
