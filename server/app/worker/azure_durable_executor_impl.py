from app.worker.azure_durable_executor_abstract import AzureDurableExecutorAbstract


class AzureDurableExecutorEntity(AzureDurableExecutorAbstract):
    def __init__(self, args):
        super().__init__(args, "api/FixitySingleEntity")

    def gen_result_entity(self, entity, state, desc):
        retFile = {
            "id": entity["id"],
            "index_location": entity["index_location"],
            "state": state,
            "desc": desc,
        }
        retMets = {
            "id": entity["id"],
            "state": state,
            "desc": desc,
        }
        return {"retFile": retFile, "retMets": retMets}


class AzureDurableExecutorFile(AzureDurableExecutorAbstract):
    def __init__(self, args):
        super().__init__(args, "api/FixitySingleFile")

    def gen_result_entity(self, entity, state, desc):
        retFile = {
            "id": entity["id"],
            "index_location": entity["index_location"],
            "state": state,
            "desc": desc,
        }
        return retFile


class AzureDurableExecutorMets(AzureDurableExecutorAbstract):
    def __init__(self, args):
        super().__init__(args, "api/FixitySingleMets")

    def gen_result_entity(self, entity, state, desc):
        retMets = {
            "id": entity["id"],
            "state": state,
            "desc": desc,
        }
        return retMets
