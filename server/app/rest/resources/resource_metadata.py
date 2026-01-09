import falcon

from common.metadata import FixityResultOptions, FixityType, TaskState, TaskPeriodicType


def enum_to_dict(enum_cls):
    # return {member.name: member.value for member in enum_cls}
    return [{"name": member.name, "code": member.value} for member in enum_cls]


class FixityMetadataResource:
    def __init__(self, args):
        self.args = args

    def on_get(self, req: falcon.Request, rsp: falcon.Response):
        if self.args.enable_blob_storage_event:
            available_fixity_type_options = [{"name": item.name, "code": item.value} for item in FixityType if item in FixityType.patch_types()]
        else:
            available_fixity_type_options = [{"name": item.name, "code": item.value} for item in FixityType]

        fixity_type_options = enum_to_dict(FixityType)
        periodic_type_options = enum_to_dict(TaskPeriodicType)
        task_state_options = enum_to_dict(TaskState)

        options = {
            "fixity_type_options": fixity_type_options,
            "fixity_result_options": FixityResultOptions,
            "periodic_type_options": periodic_type_options,
            "task_state_options": task_state_options,
            "available_fixity_type_options": available_fixity_type_options,
            "enable_blob_storage_event": self.args.enable_blob_storage_event,
        }

        rsp.status = falcon.HTTP_OK
        rsp.media = options
