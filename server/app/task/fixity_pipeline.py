import logging

from app.data_sync.data_sync_full_incremental import DataSyncFullIncremental
from app.data.models import FixityTask
from app.fixity.fixity_engine_bau import FixityEngineBAUFile, FixityEngineBAUMets
from app.fixity.fixity_engine_blob_event import FixityEngineBlobEvent
from common.db.db_access_fixity import FixityDatabaseHandler
from common.db.db_access_rosetta import RosettaDatabaseHandler
from common.metadata import FixityType


class FixityPipeline:
    def __init__(self, args, db_fixity, db_rosetta):
        self.args = args
        self.db_fixity: FixityDatabaseHandler = db_fixity
        self.db_rosetta: RosettaDatabaseHandler = db_rosetta

        self.data_sync_handler = DataSyncFullIncremental(args=args, db_fixity=db_fixity, db_rosetta=db_rosetta)
        self.bau_engine_file = FixityEngineBAUFile(args=args, db_fixity=db_fixity)
        self.bau_engine_mets = FixityEngineBAUMets(args=args, db_fixity=db_fixity)
        self.blob_event_engine = FixityEngineBlobEvent(args=args, db_fixity=db_fixity)

    def close(self):
        self.data_sync_handler.close()
        self.bau_engine_file.close()
        self.bau_engine_mets.close()
        self.blob_event_engine.close()

    @property
    def progress(self):
        try:
            progresses = [processor.progress for processor in self.processors]
            return progresses
        except Exception as ex:
            logging.error(f"{ex}")
            return []

    def _run(self, task: FixityTask):
        fixity_type = FixityType(task.fixity_type)
        if task.sync_data_flag:
            self.data_sync_handler.sync_data(fixity_task_type=fixity_type)

        if fixity_type == FixityType.BAU:
            self.bau_engine_file.fixity(task=task)
            self.bau_engine_mets.fixity(task=task)
        elif fixity_type == FixityType.BLOB_EVENT:
            self.blob_event_engine.fixity(task=task)
        else:
            logging.warning(f"Unsupported fixity type: {fixity_type}")
            return False
        return True

    def run(self, task: FixityTask):
        ret = False
        try:
            ret = self._run(task)
        except Exception as ex:
            logging.error(f"{ex}")
        finally:
            self.close()
            logging.info(f"The fixity pipeline is finished. Result={ret}!")

        return ret
