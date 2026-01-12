import os
from pathlib import Path

import cherrypy
import falcon

from app.auth.ldap_client import LDAPAuthentication
from app.auth.sessions import SessionManager
from app.rest import resources
from app.rest.resources.resource_blob_storage import BlobStorageResource

_server_running = False


def start_rest_server(
    app: falcon.App,
    app_auth: falcon.App,
    app_mock: falcon.App,
    port: int,
    thread_pool_size: int = 20,
):
    global _server_running
    if _server_running:
        raise RuntimeError("Only one rest server may be running at once")

    cherrypy.tree.graft(app, "/api")
    cherrypy.tree.graft(app_auth, "/auth")
    cherrypy.tree.graft(app_mock, "/mock")

    # Serve static files via CherryPy
    current_dir = Path(os.path.dirname(os.path.abspath(__file__)))
    web_dir = current_dir.parent.parent.parent.joinpath("frontend").joinpath("dist")

    conf = {
        "/": {
            "tools.staticdir.on": True,
            "tools.staticdir.dir": str(web_dir),
            "tools.staticdir.index": "index.html",
        }
    }  # folder "dist"

    def error_page_404(status, message, traceback, version):
        index_file = os.path.join(web_dir, "index.html")
        if os.path.exists(index_file):
            with open(index_file, "r", encoding="utf-8") as f:
                return f.read()
        return "404 Not Found"

    cherrypy.config.update({"error_page.404": error_page_404})

    cherrypy.tree.mount(None, "/", config=conf)

    cherrypy.server.socket_port = port
    cherrypy.server.socket_host = "0.0.0.0"
    cherrypy.server.thread_pool = thread_pool_size
    cherrypy.server.max_request_body_size = None
    cherrypy.config.update({"engine.autoreload.on": False})
    cherrypy.server.start()


def close_rest_server():
    global _server_running
    if _server_running:
        cherrypy.server.stop()
    _server_running = False


def add_auth_resources(
    app: falcon.App,
    auth_client: LDAPAuthentication,
    session_manager: SessionManager,
):
    res_user_auth = resources.UserAuthResource(
        session_manager=session_manager, auth_client=auth_client
    )
    app.add_route("/login", res_user_auth)
    app.add_route("/logout/{token}", res_user_auth)


def add_resources(
    app: falcon.App,
    args,
    session_manager: SessionManager,
    task_manager: TaskManager,
    db_fixity: FixityDatabaseHandler,
):

    res_global_settings = resources.GlobalSettingsResource(db_fixity=db_fixity)
    res_white_list = resources.WhiteListResource(
        session_manager=session_manager, db_fixity=db_fixity
    )
    res_task = resources.TaskResource(
        session_manager=session_manager, task_manager=task_manager
    )

    res_job = resources.JobResource(db_fixity=db_fixity)
    res_fixity_metadata = resources.FixityMetadataResource(args=args)
    res_fixity_report = resources.FixityReportResource(db_fixity=db_fixity)
    res_webhook = resources.WebhookResource()

    app.add_route("/global-settings", res_global_settings)
    app.add_route("/white-list", res_white_list)
    app.add_route("/job", res_job)
    app.add_route("/task", res_task)
    app.add_route("/task/{oid:int}", res_task)
    app.add_route("/metadata", res_fixity_metadata)
    app.add_route("/report", res_fixity_report)
    app.add_route("/webhook", res_webhook)


def add_mock_resources(app: falcon.App, blob_storage_access: BlobStorageAccess):
    res_blob_storage = BlobStorageResource(blob_storage_access)
    app.add_route("/blob-storage", res_blob_storage)
    app.add_route("/blob-storage/{blob_name}", res_blob_storage)
