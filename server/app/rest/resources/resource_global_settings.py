from dataclasses import asdict

import falcon
import orjson

from app.data.dao_global_setting import GlobalSettingsDao
from common.db.db_access_fixity import FixityDatabaseHandler


class GlobalSettingsResource:
    def __init__(self, db_fixity: FixityDatabaseHandler):
        self.db_fixity = db_fixity

    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        obj = GlobalSettingsDao.get()
        rsp.status = falcon.HTTP_OK
        rsp.media = asdict(obj)

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)
        GlobalSettingsDao.save(data_json)
        self.on_get(req=req, rsp=rsp)

    def on_delete(self, req: falcon.Request, rsp: falcon.Response):
        pass
