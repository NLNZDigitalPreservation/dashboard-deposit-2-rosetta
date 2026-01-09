import falcon
import orjson

from app.auth.sessions import SessionManager
from app.data import RepoGlobalSetting
from app.data.services_settings import DataServices
from common.utils import helper
from common.utils.dataclass_utils import dataclass_as_dict


class GlobalSettingsResource:
    def __init__(
        self,
        session_manager: SessionManager,
        repo: DataServices,
    ):
        self.session_manager = session_manager
        self.repo = repo

    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        rsp.status = falcon.HTTP_OK
        rsp.media = dataclass_as_dict(self.repo.global_settings.global_setting)

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)
        helper.assert_not_empty("id", data_json)
        self.repo.global_settings.save(data_json)
        self.on_get(req=req, rsp=rsp)

    def on_delete(self, req: falcon.Request, rsp: falcon.Response):
        pass
