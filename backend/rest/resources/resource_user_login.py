import uuid

import falcon
import orjson
from playhouse.shortcuts import model_to_dict

from app.auth.ldap_client import LDAPAuthentication
from app.auth.sessions import RoleType, SessionManager, UserInfo
from common.data.models import WhiteList

TEST_SESSION_ID = "241200811372143992420081372111"


class UserLoginResource:
    def __init__(self, args, session_manager: SessionManager):
        self.args = args
        self.session_manager = session_manager
        self.ldap_client = LDAPAuthentication(args=args)

    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        token = req.get_header("Authorization", default=None)
        if token is None:
            raise falcon.HTTPUnauthorized(
                title="Unauthorized", description="Missing Authorization header"
            )
        if not self.session_manager.is_valid(token):
            raise falcon.HTTPUnauthorized(
                title="Unauthorized", description="Invalid Authorization token"
            )

        user_info = self.session_manager.get_user_info(token)
        rsp.status = falcon.HTTP_OK
        rsp.media = user_info.to_dict()

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        auth_mode = self.args.user_auth_mode
        if auth_mode is None:
            auth_mode = "test"
        else:
            auth_mode = str(auth_mode).lower()

        # Parse the request body as JSON
        data = req.bounded_stream.read()
        if data is None or len(data) == 0:
            raise falcon.HTTPBadRequest(
                title="Bad Request", description="Missing request body"
            )
        data_json = orjson.loads(data.decode())

        # Initial the request user info
        user_info = UserInfo(
            user_name=data_json.get("user_name", "test_user"),
            email=data_json.get("email", self.args.admin_email),
            presentation_name=data_json.get("presentation_name", "Test User"),
            role=RoleType.ADMIN.value,
            token=f"{uuid.uuid4()}",
        )

        if auth_mode == "entra":
            pass
        elif auth_mode == "ldap":
            ret, user_info_or_err = self.ldap_client.authenticate(
                user_info, data_json.get("password", "")
            )
            if not ret:
                raise falcon.HTTPUnauthorized(
                    title="Unauthorized",
                    description=f"Failed to authorize user: {user_info_or_err}",
                )

            user_info = user_info_or_err
        else:
            # Taken as test mode
            user_info.token = TEST_SESSION_ID

        if not self.session_manager.is_valid(user_info.token):
            self.session_manager.remove_session(user_info.token)
            self.session_manager.add_session(user_info)

        user: WhiteList = WhiteList.get_or_none(
            WhiteList.account_name == user_info.user_name
        )
        if user is None:
            user = WhiteList.create(
                mail=user_info.email,
                account_name=user_info.user_name,
                presentation_name=user_info.presentation_name,
                role=user_info.role,
            )
        rsp.status = falcon.HTTP_OK
        rsp.media = user_info.to_dict()

    def on_delete(self, req: falcon.Request, rsp: falcon.Response):
        token = req.get_header("Authorization", default=None)
        if token is None:
            return
        self.session_manager.remove_session(token)
