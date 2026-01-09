from app.data.dao_abstract import DAOAbstract
from app.data.models import DepositJob


class RepoDepositJobHistory(DAOAbstract):
    def __init__(self, args):
        super().__init__(args, "deposit_job_history", DepositJob)
