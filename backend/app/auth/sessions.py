import enum
import logging
import time
from dataclasses import dataclass, asdict
from os import path
from typing import Dict, Optional

import falcon
from playhouse.shortcuts import model_to_dict

from common.data.models import WhiteList


class RoleType(enum.Enum):
    BOOTSTRAP = "bootstrap"
    ADMIN = "admin"
    NORMAL = "normal"


@dataclass
class UserInfo:
    user_name: str
    email: str
    presentation_name: str
    role: str
    token: Optional[str] = None

    def to_dict(self):
        return asdict(self)


@dataclass
class SessionInfo:
    modified: float
    user_info: UserInfo
    expire_interval: int

    def expired(self):
        return time.time() - self.modified > self.expire_interval

    def touch(self):
        self.modified = time.time()


class SessionManager:
    def __init__(self, args):
        self.args = args
        self.expire_interval = args.expire_interval
        self.session_map: Dict[str, SessionInfo] = {}

    def add_session(self, user_info: UserInfo):
        token = user_info.token
        if token in self.session_map:
            raise RuntimeError(f"Session {token} already exists")

        keys = list(self.session_map.keys())
        for key in keys:
            s: SessionInfo = self.session_map.get(key)
            if s.expired():
                self.session_map.pop(key)
                logging.info(f"{s.user_info.user_name} {key} is expired")

        s = SessionInfo(
            modified=time.time(),
            user_info=user_info,
            expire_interval=self.expire_interval,
        )
        self.session_map[token] = s

    def remove_session(self, token):
        if token in self.session_map:
            self.session_map.pop(token)

    def get_user_info(self, token) -> UserInfo:
        if token not in self.session_map:
            raise RuntimeError(f"Invalid session: {token}")
        s: SessionInfo = self.session_map.get(token)
        if not s.expired():
            s.touch()
        return s.user_info

    def get_role(self, token) -> RoleType:
        if token not in self.session_map:
            raise RuntimeError(f"Invalid session: {token}")
        s: SessionInfo = self.session_map.get(token)
        if not s.expired():
            s.touch()
        return RoleType(s.user_info.role)

    def is_valid(self, token):
        if token not in self.session_map:
            return False

        s: SessionInfo = self.session_map.get(token)
        if s.expired():
            return False

        s.touch()
        return True

    def validate(self, req: falcon.Request, rsp: falcon.Response):
        path = req.path
        if path in (
            "/api/metadata",
            "/api/pipeline",
            "/api/pipeline-messages",
            "/api/pipeline-progress",
            "/api/system-info",
            "/auth/login",
        ):
            return True, None

        token = req.get_header("Authorization")
        if token is None or len(token) == 0:
            ex = falcon.HTTPUnauthorized(
                title="Unauthorized", description="Authorization header is missing"
            )
            return False, ex

        if not self.is_valid(token):
            ex = falcon.HTTPUnauthorized(
                title="Unauthorized", description="Authorization session is expired"
            )
            return False, ex

        role = self.get_role(token)
        if role is None:
            ex = falcon.HTTPUnauthorized(
                title="Unauthorized", description=f"Unable to get role: {token}"
            )
            return False, ex

        # if (
        #     role == RoleType.BOOTSTRAP
        #     and not path.startswith("/api/white-list")
        #     and req.method.upper() != "GET"
        # ):
        #     ex = falcon.HTTPForbidden(
        #         title="NoPrivilege",
        #         description=f"Bootstrap user can only operate the white-list and read the data",
        #     )
        #     return False, ex

        # if role == RoleType.NORMAL and req.method.upper() != "GET":
        #     ex = falcon.HTTPForbidden(
        #         title="NoPrivilege", description=f"Normal user can only read the data"
        #     )
        #     return False, ex

        return True, None
