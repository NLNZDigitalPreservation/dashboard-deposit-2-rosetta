import falcon
from orjson import orjson
import uuid
from app.auth.ldap_client import LDAPAuthentication
from app.auth.sessions import RoleType, SessionManager


class UserAuthResource:
    def __init__(self, session_manager: SessionManager, auth_client):
        self.session_manager = session_manager
        self.auth_client: LDAPAuthentication = auth_client

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        token = req.get_header("Authorization")
        if token is not None and self.session_manager.is_valid(token):
            rsp.status = falcon.HTTP_OK
            rsp.media = token
            return

        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        username = data_json["username"]
        password = data_json["password"]

        ret, err = self.auth_client.authenticate(username, password)
        if not ret:
            raise falcon.HTTPUnauthorized(
                title="Unauthorized",
                description=f"Failed to authorize user: {username} {err}",
            )

        token = f"{uuid.uuid4()}"

        self.session_manager.add_session(token=token, username=username, role=RoleType.ADMIN)

        rsp.status = falcon.HTTP_OK
        rsp.media = token

    def on_delete(self, req: falcon.Request, rsp: falcon.Response, token: str):
        if token is None:
            return

        self.session_manager.remove_session(token)
