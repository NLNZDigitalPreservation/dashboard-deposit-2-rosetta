from app.data.dao_abstract import DAOAbstract
from app.data.models import DepositAccount


class RepoDepositAccount(DAOAbstract):
    def __init__(self, args):
        super().__init__(args, "deposit_accounts", DepositAccount)

    def get_by_institute_name(self, institute_name):
        for item in self.all_data():
            if item.depositUserInstitute == institute_name:
                return item
        return None
