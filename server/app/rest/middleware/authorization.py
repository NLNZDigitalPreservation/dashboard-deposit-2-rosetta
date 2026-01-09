import falcon

from app.auth.sessions import RoleType, SessionManager


class AuthorizationMiddleware:
    def __init__(self, session_manager: SessionManager):
        self.session_manager = session_manager

    def process_request(self, req: falcon.Request, resp: falcon.Response):
        path = req.path
        if path.startswith("/auth") or path.startswith("/mock"):
            return

        token = req.get_header("Authorization")
        if token is None or len(token) == 0:
            raise falcon.HTTPUnauthorized(title="Unauthorized", description="Authorization header is missing")

        if not self.session_manager.is_valid(token):
            raise falcon.HTTPUnauthorized(title="Unauthorized", description="Authorization session is expired")

        role = self.session_manager.get_role(token)
        if role is None:
            raise falcon.HTTPUnauthorized(title="Unauthorized", description=f"Unable to get role: {token}")

        if role == RoleType.BOOTSTRAP and not path.startswith("/api/white-list") and req.method.upper() != "GET":
            raise falcon.HTTPForbidden(
                title="NoPrivilege",
                description=f"Bootstrap user can only operate the white-list and read the data",
            )

        if role == RoleType.NORMAL and req.method.upper() != "GET":
            raise falcon.HTTPForbidden(title="NoPrivilege", description=f"Normal user can only read the data")
