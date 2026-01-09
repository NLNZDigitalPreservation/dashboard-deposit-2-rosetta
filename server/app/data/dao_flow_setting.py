from app.data.dao_abstract import DAOAbstract
from app.data.models import FlowSetting


class RepoFlowSetting(DAOAbstract):
    def __init__(self, args):
        super().__init__(args, "flow_settings", FlowSetting)
