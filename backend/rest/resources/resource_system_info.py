from typing import Optional

import falcon


class SystemInfoResource:
    def __init__(self, args):
        self.args = args

    def on_get(
        self, req: falcon.Request, rsp: falcon.Response, oid: Optional[int] = None
    ):

        rsp.status = falcon.HTTP_OK
        rsp.media = {
            "version": self.args.version,
            "authMode": self.args.user_auth_mode,
            "entraClientId": self.args.user_auth_entra_client_id,
            "entraTenantId": self.args.user_auth_entra_tenant_id,
        }
