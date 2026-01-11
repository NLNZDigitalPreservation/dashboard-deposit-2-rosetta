import enum
from dataclasses import dataclass, asdict
from typing import Optional


class EnumDepositJobStage(enum.Enum):
    INGEST = "ingest"
    DEPOSIT = "deposit"
    FINALIZE = "finalize"
    FINISHED = "finished"


class EnumDepositJobState(enum.Enum):
    INITIALED = "initialized"
    RUNNING = "running"  # Submit and accept by Rosetta
    PAUSED = "paused"  # Paused, but can be retried or resumed
    SUCCEEDED = "succeeded"  # Successfully finished the stage
    FAILED = "failed"  # Completely failed and can not be retried
    CANCELED = "canceled"  # Stopped manually


class EnumActualContentDeletionOptions(enum.Enum):
    NOT_DELETE = "notDelete"
    DELETE_EXCEED_MAX_STORAGE_DAYS = "deleteExceedMaxStorageDays"
    DELETE_INSTANTLY = "deleteInstantly"


class EnumStorageMode(enum.Enum):
    NFS = "nfs"
    FTP = "ftp"
    SFTP = "sftp"


@dataclass
class SipStatusInfo:
    link: Optional[str] = None
    id: Optional[str] = None
    externalId: Optional[str] = None
    externalSystem: Optional[str] = None
    module: str = "HUB"
    stage: Optional[str] = None
    status: Optional[str] = None
    numberOfIEs: Optional[str] = None
    iePids: Optional[str] = None

    @classmethod
    def from_dict(cls, data: dict) -> "SipStatusInfo":
        return cls(**data)

    def to_dict(self) -> dict:
        return asdict(self)
