import falcon
import orjson

from app.domain.dao_job import JobDao
from common.db.db_access_fixity import FixityDatabaseHandler


class JobResource:
    def __init__(self, db_fixity: FixityDatabaseHandler):
        self.job_dao = JobDao(db_fixity=db_fixity)

    def on_post(self, req: falcon.Request, rsp: falcon.Response):
        data = req.stream.read(req.content_length).decode()
        data_json = orjson.loads(data)

        rsp.media = self.job_dao.query_job(
            data_json["pagination"],
            data_json["search_conditions"],
            data_json["sort_by"],
        )
