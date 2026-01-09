from __future__ import annotations
from dataclasses import dataclass, field
from typing import List, Optional
from enum import Enum


# Helper Enums
class EnumDepositJobStage(Enum):
    START = "START"
    END = "END"


class EnumDepositJobState(Enum):
    RUNNING = "RUNNING"
    FINISHED = "FINISHED"


class EnumUserRole(Enum):
    ADMIN = "ADMIN"
    USER = "USER"


@dataclass
class EntityCommon:
    id: Optional[int] = None
    auditRst: bool = True
    auditMsg: str = "OK"


@dataclass
class GlobalSetting(EntityCommon):
    paused: bool = False
    pausedStartTime: Optional[str] = None
    pausedEndTime: Optional[str] = None
    delays: Optional[int] = None
    delayUnit: str = "S"

    @property
    def delayTimeUnitSeconds(self) -> int:
        mapping = {"S": 1, "M": 60, "H": 3600, "D": 86400}
        return mapping.get(self.delayUnit.upper(), 1)


@dataclass
class DepositAccount(EntityCommon):
    depositUserInstitute: Optional[str] = None
    depositUserName: Optional[str] = None
    depositUserPassword: Optional[str] = None

    def __str__(self):
        return f"{self.depositUserName}-{self.depositUserInstitute}:*****"


@dataclass
class FlowSetting(EntityCommon):
    enabled: bool = False
    depositAccountId: Optional[int] = None
    materialFlowId: Optional[str] = None
    materialFlowName: Optional[str] = None
    producerId: Optional[str] = None
    producerName: Optional[str] = None
    rootPath: Optional[str] = None
    streamLocation: Optional[str] = None
    injectionCompleteFileName: Optional[str] = None
    maxActiveDays: Optional[int] = None
    maxSaveDays: Optional[int] = None
    delays: Optional[int] = None
    delayUnit: Optional[str] = None
    weeklyMaxConcurrency: list[int] = field(default_factory=lambda: [0] * 7)
    actualContentDeleteOptions: Optional[str] = None
    backupEnabled: Optional[bool] = None
    actualContentBackupOptions: Optional[str] = None
    backupPath: Optional[str] = None
    backupSubFolders: Optional[str] = None


@dataclass
class DepositJob(EntityCommon):
    initialTime: Optional[int] = None
    latestTime: Optional[int] = None
    depositStartTime: Optional[int] = None
    depositEndTime: Optional[int] = None
    finalizedTime: Optional[int] = None
    finishedTime: Optional[int] = None
    injectionPath: Optional[str] = None
    injectionTitle: Optional[str] = None
    fileCount: int = 0
    fileSize: int = 0
    isSuccessful: bool = False
    sipId: Optional[str] = None
    sipModule: Optional[str] = None
    sipStage: Optional[str] = None
    sipStatus: Optional[str] = None
    stage: Optional[EnumDepositJobStage] = None
    state: Optional[EnumDepositJobState] = None
    depositSetId: Optional[str] = None
    resultMessage: Optional[str] = None
    appliedFlowSetting: Optional[FlowSetting] = None
    actualContentDeleted: bool = False
    backupCompleted: bool = False


@dataclass
class Whitelist(EntityCommon):
    username: Optional[str] = None
    role: Optional[EnumUserRole] = None


@dataclass
class EntityID:
    key: Optional[str] = None
    number: Optional[int] = None


@dataclass
class EntityStorageLocation:
    scanMode: Optional[str] = None
    rootPath: Optional[str] = None
    ftpServer: Optional[str] = None
    ftpPort: int = 21
    ftpUsername: Optional[str] = None
    ftpPassword: Optional[str] = None
    ftpProxyEnabled: bool = False
    ftpProxyHost: Optional[str] = None
    ftpProxyPort: int = 0
    ftpProxyUsername: Optional[str] = None
    ftpProxyPassword: Optional[str] = None
