import threading
from collections import deque
from dataclasses import dataclass
from datetime import datetime as dt


class ProgressManager:
    def __init__(self):
        self.lock = threading.Lock()
        self.progressBars = []
        self.messages = deque(maxlen=30)

    def addItem(self, item):
        with self.lock:
            self.progressBars.append(item)

    def reset(self):
        with self.lock:
            self.progressBars = []
            self.messages.clear()

    def push_message(self, msg):
        with self.lock:
            self.messages.append(msg)

    def latest_progress(self):
        with self.lock:
            if len(self.progressBars) > 0:
                latest_item = self.progressBars[-1]
                return latest_item.to_dict()
            else:
                return None

    def all_progresses(self):
        with self.lock:
            total_progress = [p.to_dict() for p in self.progressBars]
        return total_progress

    def get_messages(self):
        with self.lock:
            messages = list(self.messages)
        return messages

    def get_progress_bar(self, title):
        with self.lock:
            for p in self.progressBars:
                if p.title == title:
                    return p
            return None


progress_manager = ProgressManager()


class ProgressBar:
    _tot_progresses = []
    _errors = deque(maxlen=1024)

    def __init__(self, title="Anonymous"):
        self.original_title = title
        self.title = title
        self.start_time = dt.now()
        self.end_time = dt.now()
        self.total_records = 0
        self.processed_records = 0
        self.successful_records = 0
        self.failed_records = 0
        progress_manager.addItem(self)

    def reset(self):
        self.start_time = dt.now()
        self.end_time = dt.now()
        self.total_records = 0
        self.processed_records = 0
        self.successful_records = 0
        self.failed_records = 0

    def set_title(self, title: str):
        self.title = title

    def set_total_records(self, total_records):
        self.total_records = total_records

    def acc_failure(self, count=1):
        self.end_time = dt.now()
        if not count:
            return
        self.processed_records += count
        self.failed_records += count

    def acc_success(self, count=1):
        self.end_time = dt.now()
        if not count:
            return
        self.processed_records += count
        self.successful_records += count

    def to_dict(self):
        result = {
            "title": self.title,
            "total_records": self.total_records,
            "processed_records": self.processed_records,
            "successful_records": self.successful_records,
            "failed_records": self.failed_records,
            "start_time": self.start_time.strftime("%Y-%m-%d %H:%M:%S"),
            "end_time": self.end_time.strftime("%Y-%m-%d %H:%M:%S"),
            "duration": f"{self.end_time - self.start_time}",
        }
        return result

    def to_string(self):
        result = (
            f"{self.title}\n"
            f"Total records: {self.total_records}\n"
            f"Processed records: {self.processed_records}\n"
            f"Successful records: {self.successful_records}\n"
            f"Failed records: {self.failed_records}\n"
            f"Start time: {self.start_time.strftime("%Y-%m-%d %H:%M:%S")}\n"
            f"End time: {self.end_time.strftime("%Y-%m-%d %H:%M:%S")}\n"
            f"Duration: {self.end_time - self.start_time}"
        )
        return result

    def push_message(self, error):
        progress_manager.push_message(error)

    def latest_progress(self):
        return progress_manager.latest_progress()

    def all_progresses(self):
        return progress_manager.all_progresses()

    def get_errors(self):
        return progress_manager.get_messages()
