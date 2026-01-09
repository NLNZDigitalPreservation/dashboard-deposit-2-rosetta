from app.data.dao_abstract import DAOAbstract
from app.data.models import Whitelist


class RepoWhiteList(DAOAbstract):
    def __init__(self, args):
        super().__init__(args, "white_list", Whitelist)

    def get_by_username(self, username):
        for user in self.all_data():
            if user.username == username:
                return user
        return None
