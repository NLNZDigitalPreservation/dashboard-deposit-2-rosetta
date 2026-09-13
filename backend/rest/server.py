import logging
import os
from pathlib import Path

import cherrypy
import falcon

_server_running = False


def _resolve_web_dir() -> Path:
    current_dir = Path(os.path.dirname(os.path.abspath(__file__)))
    candidates = [
        current_dir.parent.joinpath("frontend", "dist"),
        current_dir.parent.parent.joinpath("frontend", "dist"),
    ]
    for candidate in candidates:
        if candidate.exists():
            return candidate

    # Keep previous behavior as a fallback for compatibility.
    return current_dir.parent.parent.parent.joinpath("frontend", "dist")


class HealthCheckResource:
    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        rsp.status = falcon.HTTP_OK
        rsp.media = {
            "status": "healthy",
            "service": "Deposit Dashboard",
        }


def _get_health_check_app():
    app = falcon.App()
    app.add_route("/", HealthCheckResource())
    return app


def start_rest_server(app: falcon.App, port: int, thread_pool_size: int = 20):
    global _server_running
    if _server_running:
        raise RuntimeError("Only one rest server may be running at once")

    cherrypy.tree.graft(_get_health_check_app(), "/depdash/health")
    cherrypy.tree.graft(app, "/depdash/rest")

    # Serve static files via CherryPy
    web_dir = _resolve_web_dir()
    logging.info(f"web dir={web_dir}")

    conf = {
        "/depdash": {
            "tools.staticdir.on": True,
            "tools.staticdir.dir": str(web_dir),
            "tools.staticdir.section": "/depdash",
            "tools.staticdir.index": "index.html",
        }
    }  # folder "dist", served at /depdash/*

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
    _server_running = True


def close_rest_server():
    global _server_running
    if _server_running:
        cherrypy.server.stop()
    _server_running = False
