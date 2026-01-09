import glob
import pathlib
import time
import requests

from app.worker.azure_common import OrchestratorTransaction, SessionStatus, query_status
from common.metadata import FixityResultCode

BATCH_API_FILE = "api/FixityBatchFile"
BATCH_API_METS = "api/FixityBatchMets"


def run_executor(env_args, data_resources, worker_api_url):
    files = glob.glob(f"{data_resources}/**/*.*", recursive=True)

    entities = []

    for file in files:
        path = pathlib.Path(file)
        if not path.is_file():
            continue

        entity = {"oid": 1, "index_location": file}

        if (path.name.endswith("ie.xml") and worker_api_url == BATCH_API_METS) or (not path.name.endswith("ie.xml") and worker_api_url == BATCH_API_FILE):
            entities.append(entity)

    rsp = requests.post(f"{env_args.fixity_worker_url}/{worker_api_url}", json=entities)
    assert rsp.ok

    rsp_json = rsp.json()
    transaction = OrchestratorTransaction(**rsp_json)

    time_used = 0
    timeslot_seconds = 1
    while time_used < 60:
        ret, output = query_status(transaction)
        if ret == SessionStatus.success:
            break

        time_used += timeslot_seconds
        time.sleep(timeslot_seconds)

    return ret, output


def test_batch_file_api(env_args, data_resources):
    ret, output = run_executor(env_args, data_resources, BATCH_API_FILE)
    assert ret == SessionStatus.success, f"Batch File API failed: {ret}, {output}"
    assert output is not None
    assert len(output) > 0

    for result in output:
        assert result["state"] == FixityResultCode.SUCCESS, f"File fixity check failed: {result}"
        assert result["checksum"]


def test_batch_mets_api(env_args, data_resources):
    ret, output = run_executor(env_args, data_resources, BATCH_API_METS)
    assert ret == SessionStatus.success, f"Batch Mets API failed: {ret}, {output}"
    assert output is not None
    assert len(output) > 0

    for result in output:
        assert result["state"] == FixityResultCode.SUCCESS, f"File fixity check failed: {result}"
        assert result["coll"]
