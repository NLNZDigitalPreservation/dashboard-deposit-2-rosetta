import falcon

from common.db.db_access_fixity import FixityDatabaseHandler


class FixityReportResource:
    def __init__(self, db_fixity: FixityDatabaseHandler):
        self.db_fixity = db_fixity

    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        dataset = []
        rsp.status = falcon.HTTP_OK
        rsp.media = dataset
