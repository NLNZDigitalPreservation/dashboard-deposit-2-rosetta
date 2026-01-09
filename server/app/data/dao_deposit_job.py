from app.data.dao_abstract import DAOAbstract
from app.data.models import FlowSetting, DepositJob


class RepoDepositJob(DAOAbstract):
    def __init__(self, args):
        super().__init__(args, "deposit_jobs", DepositJob)

    def get_by_flow(self, flow_id: int):
        all_data = self.all_data()
        for item in all_data:
            job = DepositJob(**item)
            if job.applied_flow_setting is None:
                continue
            if job.applied_flow_setting.id == flow_id:
                yield job

    def get_by_flow_and_title(self, flow_id: str, title: str):
        for job in self.get_by_flow(flow_id):
            if job.injection_title == title:
                yield job

    def get_by_initialized_time(self, start_time: int, end_time: int):
        all_data = self.all_data()
        for item in all_data:
            job = DepositJob(**item)
            if job.initial_time is None:
                continue
            if start_time <= job.initial_time <= end_time:
                yield job

    def get_by_latest_time(self, start_time: int, end_time: int):
        all_data = self.all_data()
        for item in all_data:
            job = DepositJob(**item)
            if job.latest_time is None:
                continue
            if start_time <= job.latest_time <= end_time:
                yield job
