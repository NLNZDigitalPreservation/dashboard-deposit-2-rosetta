import enum
import logging
from dataclasses import dataclass
from typing import Dict
import time


class RoleType(enum.Enum):
    BOOTSTRAP = "bootstrap"
    ADMIN = "admin"
    NORMAL = "normal"


@dataclass
class SessionInfo:
    modified: float
    username: str
    role: RoleType
    expire_interval: int

    def expired(self):
        return time.time() - self.modified > self.expire_interval

    def touch(self):
        self.modified = time.time()


class SessionManager:
    def __init__(self, expire_interval):
        self.expire_interval = expire_interval
        self.session_map: Dict[str:SessionInfo] = {}

    def add_session(self, token: str, username: str, role: RoleType):
        if token in self.session_map:
            raise RuntimeError(f"Session {token} already exists")

        keys = list(self.session_map.keys())
        for key in keys:
            s: SessionInfo = self.session_map.get(key)
            if s.expired():
                self.session_map.pop(key)
                logging.info(f"{s.username} {key} is expired")

        s = SessionInfo(
            modified=time.time(),
            username=username,
            role=role,
            expire_interval=self.expire_interval,
        )
        self.session_map[token] = s

    def remove_session(self, token):
        if token in self.session_map:
            self.session_map.pop(token)

    def get_username(self, token):
        if token not in self.session_map:
            raise RuntimeError(f"Invalid session: {token}")
        s: SessionInfo = self.session_map.get(token)
        if not s.expired():
            s.touch()
        return s.username

    def get_role(self, token):
        if token not in self.session_map:
            raise RuntimeError(f"Invalid session: {token}")
        s: SessionInfo = self.session_map.get(token)
        if not s.expired():
            s.touch()
        return s.role

    def is_valid(self, token):
        if token not in self.session_map:
            return False

        s: SessionInfo = self.session_map.get(token)
        if s.expired():
            return False

        s.touch()
        return True
