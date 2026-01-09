from app.data.dao_deposit_account import RepoDepositAccount
from app.data.dao_white_list import RepoWhiteList
from app.data.dao_flow_setting import RepoFlowSetting
from app.data.dao_deposit_job import RepoDepositJob
from app.data.dao_deposit_job_history import RepoDepositJobHistory
from app.data.dao_global_setting import RepoGlobalSetting


class DataRepository:
    def __init__(self, args):
        self.deposit_account = RepoDepositAccount(args=args)
        self.white_list = RepoWhiteList(args=args)
        self.flow_setting = RepoFlowSetting(args=args)
        self.deposit_job = RepoDepositJob(args=args)
        self.deposit_job_history = RepoDepositJobHistory(args=args)
        self.global_settings = RepoGlobalSetting(args=args)
