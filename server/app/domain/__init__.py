import falcon
from .models import *


def assert_empty(name, value):
    if value is not None:
        raise falcon.HTTPBadRequest(title="Bad request", description=f"The {name} should be null.")


def assert_not_empty(name, value):
    if value is None:
        raise falcon.HTTPBadRequest(title="Bad request", description=f"The {name} can not be null.")

    if isinstance(value, str) and value.strip() == "":
        raise falcon.HTTPBadRequest(title="Bad request", description=f"The {name} can not be empty.")
