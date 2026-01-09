import falcon


def assert_empty(key, data_json):
    if data_json is None:
        raise falcon.HTTPBadRequest(title="Bad request", description="The input request body is null.")

    if data_json.get(key) is not None:
        raise falcon.HTTPBadRequest(title="Bad request", description=f"The {key} should be null.")


def assert_not_empty(key, data_json):
    if data_json is None:
        raise falcon.HTTPBadRequest(title="Bad request", description="The input request body is null.")

    if data_json.get(key) is None:
        raise falcon.HTTPBadRequest(title="Bad request", description=f"The {key} can not be null.")
