import falcon

from app.auth.sessions import SessionManager


class AuthorizationMiddleware:
    def __init__(self, session_manager: SessionManager):
        self.session_manager = session_manager

    def process_request(self, req: falcon.Request, rsp: falcon.Response):
        ret, ex = self.session_manager.validate(req, rsp)
        if not ret:
            raise ex
