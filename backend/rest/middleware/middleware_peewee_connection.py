from common.data.models import db_manager


class PeeweeConnectionMiddleware:
    def process_request(self, req, resp):
        # Open connection before anything else happens
        db_manager.connect()

    def process_response(self, req, resp, resource, req_succeeded):
        # Close connection after everything is done
        db_manager.close()
