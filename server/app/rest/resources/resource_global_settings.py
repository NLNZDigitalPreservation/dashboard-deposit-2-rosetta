import falcon
import orjson
from playhouse.shortcuts import model_to_dict

from app.domain.services_setting import ServicesSetting


class GlobalSettingsResource:
    def __init__(self, service):
        self.service: ServicesSetting = service

    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        rsp.status = falcon.HTTP_OK
        rsp.media = model_to_dict(self.service.get_global_setting())

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)
        self.service.save_global_setting(data_json=data_json)
        self.on_get(req=req, rsp=rsp)

    def on_put(self, req: falcon.Request, rsp: falcon.Response):
        self.on_post(req=req, rsp=rsp)

    def on_delete(self, req: falcon.Request, rsp: falcon.Response):
        raise falcon.HTTPMethodNotAllowed(
            description="Not able to delete global settings"
        )
