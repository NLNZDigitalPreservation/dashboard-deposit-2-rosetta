from app.data.dao_abstract import DAOAbstract
from app.data.models import DepositAccount


class RepoDepositAccount(DAOAbstract):
    def __init__(self, args):
        super().__init__(args, "deposit_accounts", DepositAccount)
