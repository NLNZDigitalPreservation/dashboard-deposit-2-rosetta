from app.data.dao_abstract import DAOAbstract
from app.data.models import FlowSetting


class RepoFlowSetting(DAOAbstract):
    def __init__(self, args):
        super().__init__(args, "flow_settings", FlowSetting)

    def get_by_account_id(self, account_id):
        datasets = []
        for item in self.all_data():
            if item.depositAccountId == account_id:
                datasets.append(item)
        return datasets
