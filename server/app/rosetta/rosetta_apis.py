import base64
import json
import logging
import requests
from typing import Any, Optional


class RosettaRestApi:
    def __init__(self, rosettaRestApiUrl: str):
        self.rosettaRestApiUrl = rosettaRestApiUrl

    def _get_basic_authentication_header(self, depositAccount) -> str:
        # Note: using camelCase properties to match your Peewee models
        credential = (
            f"{depositAccount.depositUserName}-institutionCode-"
            f"{depositAccount.depositUserInstitute}:{depositAccount.depositUserPassword}"
        )
        # Base64 encode the string
        encoded_bytes = base64.b64encode(credential.encode("utf-8"))
        encoded_str = encoded_bytes.decode("utf-8")
        return f"Basic {encoded_str}"

    def fetch(self, depositAccount, method: str, path: str, reqBody: Any = None) -> str:
        url = f"{self.rosettaRestApiUrl}{path}"

        # Prepare Headers
        headers = {
            "Authorization": self._get_basic_authentication_header(depositAccount),
            "Accept": "application/json",
            "Content-Type": "application/json",
        }

        json_payload = reqBody if reqBody is not None else {}

        try:
            # Execute request
            # We use 'json=' which automatically handles serialization and headers
            response = requests.request(
                method=method,
                url=url,
                headers=headers,
                json=json_payload,
                timeout=300,
                # verify="NZGovtCA342.crt",
                verify=False,
            )  # Good practice to include a timeout

            # Error handling (Status != 200)
            if response.status_code != 200:
                err_msg = (
                    f"Failed to request: {path}, error: {response.text}. "
                    f"Account: {depositAccount}"
                )
                logging.error(err_msg)
                return None

            return response.json()
        except requests.exceptions.RequestException as e:
            logging.error(f"HTTP Request failed: {e}")
            raise Exception(f"Failed to connect to Rosetta: {e}")
