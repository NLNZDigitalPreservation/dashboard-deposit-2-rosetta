import falcon


def assert_empty(key, data_json):
    if data_json is None:
        raise falcon.HTTPBadRequest(title="Bad request", description="The input request body is null.")

    if key in data_json and data_json[key] is not None:
        raise falcon.HTTPBadRequest(title="Bad request", description=f"The {key} should be null.")


def assert_not_empty(key, data_json):
    if data_json is None:
        raise falcon.HTTPBadRequest(title="Bad request", description="The input request body is null.")

    if key not in data_json or data_json[key] is None:
        raise falcon.HTTPBadRequest(title="Bad request", description=f"The {key} can not be null.")
