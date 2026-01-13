import logging
import falcon
from .models import *


def assert_empty(name, value):
    if value is not None:
        err = f"The {name} should be null."
        logging.error(err)
        raise falcon.HTTPBadRequest(title="Bad request", description=err)


def assert_not_empty(name, value):
    if value is None:
        err = f"The {name} can not be null."
        logging.error(err)
        raise falcon.HTTPBadRequest(title="Bad request", description=err)

    if isinstance(value, str) and value.strip() == "":
        err = f"The {name} can not be empty."
        logging.error(err)
        raise falcon.HTTPBadRequest(title="Bad request", description=err)
