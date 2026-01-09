import logging
import urllib.parse
import uuid

import requests


class PdsClient:
    TEST_PDS_TOKEN = "241200811372143992420081372111"
    PDS_HANDLER_KEYWORD = "&pds_handle="

    def __init__(self, rosetta_pds_url, bootstrap_password, test_mode=False):
        self.pds_url = rosetta_pds_url
        self.bootstrap_password = bootstrap_password
        self.test_mode = test_mode

    def login(self, institution: str, username: str, password: str):
        if self.test_mode:
            # return PdsClient.TEST_PDS_TOKEN
            return f"{uuid.uuid4()}"

        if username == "bootstrap":
            if password == self.bootstrap_password:
                token = uuid.uuid4()
                return f"bootstrap-{token}"
            else:
                return None

        credential = {
            "func": "login",
            "institute": institution,
            "bor_id": username,
            "bor_verification": password,
        }
        data_url = urllib.parse.urlencode(credential)
        rsp = requests.post(self.pds_url, data_url)
        if not rsp.ok:
            logging.error(f"Failed to login: {username}, error code: {rsp.status_code}, error: {rsp.text}")
            return None

        authorization_content = rsp.text
        if authorization_content is None or PdsClient.PDS_HANDLER_KEYWORD not in authorization_content:
            logging.error(f"Bad response from pds service: {username}")
            return None

        start_pos = authorization_content.index(PdsClient.PDS_HANDLER_KEYWORD) + len(PdsClient.PDS_HANDLER_KEYWORD)
        end_pos = authorization_content.index("&", start_pos)

        token = authorization_content[start_pos:] if end_pos < 0 else authorization_content[start_pos:end_pos]
        logging.info(f"Login succeed: {username}, {token}")
        return token

    def logout(self, token: str):
        if self.test_mode or token.startswith("bootstrap-"):
            return

        credential = {
            "func": "logout",
            "pds_handle": token,
        }
        data_url = urllib.parse.urlencode(credential)
        rsp = requests.post(self.pds_url, data_url)
        logging.info(f"Logout: {token}, {rsp.status_code}")
