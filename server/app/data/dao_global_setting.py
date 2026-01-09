from app.data.dao_abstract import DAOAbstract
from app.data.models import GlobalSetting


class RepoGlobalSetting(DAOAbstract):
    G_ID = 1

    def __init__(self, args):
        super().__init__(args, "global_settings", GlobalSetting)

    @property
    def global_setting(self) -> GlobalSetting:
        with self.lock:
            global_setting = self.get(RepoGlobalSetting.G_ID)
            if global_setting is None:
                global_setting = GlobalSetting(id=RepoGlobalSetting.G_ID)
                self.save(global_setting)
        return global_setting
