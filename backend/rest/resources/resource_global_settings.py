import falcon
import orjson
from playhouse.shortcuts import model_to_dict

from common.data.dao_global_settings import GlobalSettingsDao


class GlobalSettingsResource:
    def __init__(self):
        pass

    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        obj = GlobalSettingsDao.get()
        rsp.status = falcon.HTTP_OK
        rsp.media = model_to_dict(obj)

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)
        GlobalSettingsDao.save(data_json)
        self.on_get(req=req, rsp=rsp)

    def on_delete(self, req: falcon.Request, rsp: falcon.Response):
        pass
