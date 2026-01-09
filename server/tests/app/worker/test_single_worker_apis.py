import glob
import pathlib
from app.worker import azure_durable_executor_impl as impl
from app.worker.azure_durable_executor_abstract import AzureDurableExecutorAbstract


class DurableFuncCallback:
    def __init__(self):
        self.succeed_dataset = None
        self.failed_dataset = None

    def persist_call_back(self, succeed_dataset, failed_dataset):
        print(f"Persisting Entity Results: {len(succeed_dataset)} succeed, {len(failed_dataset)} failed")
        for output in succeed_dataset:
            print(f"Succeed: {output}")

        for output in failed_dataset:
            print(f"Failed: {output}")

        self.succeed_dataset = succeed_dataset
        self.failed_dataset = failed_dataset

    def assert_result(self):
        assert self.succeed_dataset is not None, "Succeed dataset is None"
        assert self.failed_dataset is not None, "Failed dataset is None"
        assert len(self.succeed_dataset) > 0, "Not all dataset succeeded"
        assert len(self.failed_dataset) == 0, "Failed dataset is negative"


def run_executor(data_resources, worker: AzureDurableExecutorAbstract, only_ie=False):
    files = glob.glob(f"{data_resources}/**/*.*", recursive=True)

    for file in files:
        path = pathlib.Path(file)
        if not path.is_file():
            continue

        if path.name.endswith("ie.xml"):
            storage_entity_type = "IE"
        else:
            if only_ie:
                continue
            storage_entity_type = "FILE"

        entity = {
            "oid": 1,
            "index_location": file,
            "fixityFile": False if only_ie else True,
            "fixityMets": storage_entity_type == "IE",
        }

        output = worker.submit_job(entity)
        assert output, "Failed to submit job"
        break

    worker.close_after_finished


def test_executor_entity(env_args, data_resources):
    worker = impl.AzureDurableExecutorEntity(args=env_args)
    run_executor(data_resources, worker)


def test_executor_file(env_args, data_resources):
    worker = impl.AzureDurableExecutorFile(args=env_args)
    run_executor(data_resources, worker)


def test_executor_mets(env_args, data_resources):
    worker = impl.AzureDurableExecutorMets(args=env_args)
    run_executor(data_resources, worker, only_ie=True)
