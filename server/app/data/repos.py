from app.data import *


class DataRepository:
    def __init__(self, args):
        self.deposit_account = RepoDepositAccount(args=args)
        self.white_list = RepoWhiteList(args=args)
        self.flow_setting = RepoFlowSetting(args=args)
        self.deposit_job = RepoDepositJob(args=args)
        self.deposit_job_history = RepoDepositJobHistory(args=args)
        self.global_settings = RepoGlobalSetting(args=args)
