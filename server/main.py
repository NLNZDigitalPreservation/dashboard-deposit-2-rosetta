import os
import argparse

import falcon

from app.domain.models import db_manager
from app.auth.ldap_client import LDAPAuthentication
from app.auth.sessions import SessionManager
from app.rest import server
from app.rest.middleware.authorization import AuthorizationMiddleware
from app.task.task_manager import TaskManager
from common.shared import config, exiting
from common.utils import log_utils


def running_in_container():
    # Docker
    if os.path.exists("/.dockerenv"):
        return True
    # Podman
    if os.path.exists("/run/.containerenv"):
        return True
    # Check cgroups for hints
    try:
        with open("/proc/1/cgroup", "rt") as f:
            for line in f:
                if any(x in line for x in ("docker", "kubepods", "podman", "libpod")):
                    return True
    except FileNotFoundError:
        pass
    return False


def _parse_args() -> argparse.Namespace:
    parser = config.Parser(description="This is the deposit dashboard")

    parser.add_log_level()
    parser.add_ldap_arguments()
    parser.add_app_main_arguments(api_port=1901)

    args = parser.parse_known_args()
    return args[0]


def main():
    args = _parse_args()

    log_utils.init(args.log_level)

    auth_client = LDAPAuthentication(args=args)
    session_manager = SessionManager(expire_interval=args.expire_interval)
    auth_middleware = AuthorizationMiddleware(session_manager=session_manager)

    # Filter the authentication
    app = falcon.App(middleware=[auth_middleware])
    app_auth = falcon.App()
    app_mock = falcon.App()
    server.add_resources(app=app, args=args, session_manager=session_manager, task_manager=task_manager, db_fixity=db_fixity)
    server.add_auth_resources(app=app_auth, auth_client=auth_client, session_manager=session_manager)

    server.start_rest_server(app=app, app_auth=app_auth, app_mock=app_mock, port=3000)
    exiting.add_before_exit_callback(server.close_rest_server)
    exiting.wait_for_exit()


if __name__ == "__main__":
    if not running_in_container():
        from dotenv import load_dotenv

        load_dotenv()

    main()
