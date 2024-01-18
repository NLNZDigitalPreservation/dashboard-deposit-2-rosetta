import pathlib
import glob
import json
import shutil


def load_file(file_path):
    with open(file_path, "r") as fp:
        json_data = json.load(fp)
        return json_data


stage_weight = {
    "INGEST": 0,
    "DEPOSIT": 1,
    "FINALIZE": 2,
    "FINISHED": 3,
}

state_weight = {
    "INITIALED": 0,
    "RUNNING": 4,
    "PAUSED": 1,
    "SUCCEED": 5,
    "FAILED": 3,
    "CANCELED": 2,
}


def job_sort_key(job):
    try:
        stage = stage_weight[job["stage"]]
        state = state_weight[job["state"]]
        id = int(job["id"])

        return stage * 1024 ** 4 + state * 1024 ** 2 - id
    except AttributeError as e:
        print(f'{job["id"]} {job["stage"]} {job["state"]}')


def main(root_path):
    groupped_jobs = {}
    files = glob.glob(str(pathlib.Path(root_path, "jobs", "*.json")), recursive=False)
    for file_path in files:
        job = load_file(file_path)
        subfolder = job["injectionPath"]
        if subfolder not in groupped_jobs:
            groupped_jobs[subfolder] = []
        groupped_jobs[subfolder].append(job)

    for subfolder in groupped_jobs:
        job_set = groupped_jobs[subfolder]
        if len(job_set) <= 1:
            continue

        job_set = sorted(job_set, key=job_sort_key, reverse=True)
        # print(job_set)
        for job in job_set[1:]:
            job_file_name = f"{job['id']}.json"
            print(f'{job_file_name} {job["id"]} {job["stage"]} {job["state"]}')
            shutil.move(
                str(pathlib.Path(root_path, "jobs", job_file_name)),
                str(pathlib.Path(root_path, "jobs-backup", job_file_name))
            )
        print('')


if __name__ == '__main__':
    main(root_path="/exlibris/dps/nlnz_tools/dashboard/running_data")

