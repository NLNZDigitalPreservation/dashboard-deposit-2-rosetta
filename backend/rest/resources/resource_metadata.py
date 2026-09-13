import falcon

from app.auth.sessions import SessionManager


def enum_to_dict(enum_cls):
    # return {member.name: member.value for member in enum_cls}
    return [{"name": member.name, "code": member.value} for member in enum_cls]


class FixityMetadataResource:
    def __init__(self, args, session_manager: SessionManager):
        self.args = args
        self.session_manager = session_manager

    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        # ret, ex = self.session_manager.validate(req, rsp)
        # authentication_description = None if ex is None else str(ex)

        options = {
            "version": self.args.version,
            "client_id": self.args.user_auth_entra_client_id,
            "tenant_id": self.args.user_auth_entra_tenant_id,
        }

        rsp.status = falcon.HTTP_OK
        rsp.media = options
