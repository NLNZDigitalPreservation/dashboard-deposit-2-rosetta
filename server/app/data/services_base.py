import falcon
from app.data import *


def assert_empty(name, value):
    if value is not None:
        raise falcon.HTTPBadRequest(title="Bad request", description=f"The {name} should be null.")


def assert_not_empty(name, value):
    if value is None:
        raise falcon.HTTPBadRequest(title="Bad request", description=f"The {name} can not be null.")

    if isinstance(value, str) and value.strip() == "":
        raise falcon.HTTPBadRequest(title="Bad request", description=f"The {name} can not be empty.")


class DataServicesAbstract:
    def __init__(self, args, repo: DataRepository):
        self.repo_deposit_account = repo.deposit_account
        self.repo_white_list = repo.white_list
        self.repo_flow_setting = repo.flow_setting
        self.repo_deposit_job = repo.deposit_job
        self.repo_deposit_job_history = repo.deposit_job_history
        self.repo_global_settings = repo.global_settings
