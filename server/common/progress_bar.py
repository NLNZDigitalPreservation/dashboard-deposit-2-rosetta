import threading
from dataclasses import dataclass
from datetime import datetime as dt
from collections import deque

PROGRESS_LOCK = threading.Lock()


class ProgressBar:
    _tot_progresses = []
    _errors = deque(maxlen=1024)

    def __init__(self, title="Anonymous"):
        self.title = title
        self.start_time = dt.now()
        self.end_time = dt.now()
        self.total_records = 0
        self.processed_records = 0
        self.successful_records = 0
        self.failed_records = 0
        with PROGRESS_LOCK:
            ProgressBar._tot_progresses.append(self)

    @staticmethod
    def reset():
        ProgressBar._tot_progresses = []
        ProgressBar._errors.clear()

    def initialize(self, title: str):
        self.title = title

    def set_total_records(self, total_records):
        self.total_records = total_records

    def acc_failure(self, count):
        self.end_time = dt.now()
        if not count:
            return
        self.processed_records += count
        self.failed_records += count

    def acc_success(self, count):
        self.end_time = dt.now()
        if not count:
            return
        self.processed_records += count
        self.successful_records += count

    def to_json(self):
        result = {
            "title": self.title,
            "total_records": self.total_records,
            "processed_records": self.processed_records,
            "successful_records": self.successful_records,
            "failed_records": self.failed_records,
            "start_time": f"{self.start_time}",
            "end_time": f"{self.end_time}",
            "duration": f"{self.end_time - self.start_time}",
        }
        return result

    @staticmethod
    def push_error(error):
        with PROGRESS_LOCK:
            ProgressBar._errors.append(error)

    @property
    @staticmethod
    def latest_progress():
        with PROGRESS_LOCK:
            if len(ProgressBar._tot_progresses) > 0:
                latest_item = ProgressBar._tot_progresses[-1]
                return latest_item.to_json()
            else:
                return None

    @property
    @staticmethod
    def all_progresses():
        with PROGRESS_LOCK:
            total_progress = [p.to_json() for p in ProgressBar._tot_progresses]
        return total_progress

    @staticmethod
    def get_errors():
        with PROGRESS_LOCK:
            errors = list(ProgressBar._errors)
        return errors
