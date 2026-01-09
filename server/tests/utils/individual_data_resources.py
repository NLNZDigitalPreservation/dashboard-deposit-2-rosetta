import glob
import pathlib


class IndividualDataResources:
    def __init__(self, data_resources):
        self._file_location = None
        self._ie_location = None

        files = glob.glob(f"{data_resources}/**/*.*", recursive=True)
        for file in files:
            path = pathlib.Path(file)
            if not path.is_file():
                continue

            file_name = path.name
            if file_name == "ie.xml":
                self._ie_location = file
            else:
                self._file_location = file

            if self._ie_location is not None and self._file_location is not None:
                break

    @property
    def file_location(self):
        return self._file_location

    @property
    def ie_location(self):
        return self._ie_location
