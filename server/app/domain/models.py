import os
from peewee import *
from playhouse.sqlite_ext import JSONField
from enum import Enum

# Define the database proxy
db = SqliteDatabase(None)


class BaseModel(Model):
    id = BigAutoField(primary_key=True)
    auditRst = BooleanField(default=True)
    auditMsg = CharField(default="OK", null=True)

    class Meta:
        database = db
        legacy_table_names = False


# --- Models ---
class GlobalSetting(BaseModel):
    paused = BooleanField(default=False)
    pausedStartTime = CharField(null=True)
    pausedEndTime = CharField(null=True)
    delays = IntegerField(null=True)
    delayUnit = CharField(default="S")

    @property
    def delayTimeUnitSeconds(self) -> int:
        mapping = {"S": 1, "M": 60, "H": 3600, "D": 86400}
        return mapping.get(str(self.delayUnit).upper(), 1)


class DepositAccount(BaseModel):
    depositUserInstitute = CharField(null=True)
    depositUserName = CharField(null=True)
    depositUserPassword = CharField(null=True)

    def __str__(self):
        return f"{self.depositUserName}-{self.depositUserInstitute}:*****"


class FlowSetting(BaseModel):
    enabled = BooleanField(default=True)
    # Using 'depositAccountId' as the name to match your dataclass exactly
    depositAccountId = BigIntegerField(null=True)
    materialFlowId = CharField(null=True)
    materialFlowName = CharField(null=True)
    producerId = CharField(null=True)
    producerName = CharField(null=True)
    rootPath = CharField(null=True)
    streamLocation = CharField(null=True)
    injectionCompleteFileName = CharField(null=True)
    maxActiveDays = IntegerField(null=True)
    maxSaveDays = IntegerField(null=True)
    delays = IntegerField(null=True)
    delayUnit = CharField(null=True)
    weeklyMaxConcurrency = JSONField(default=lambda: [0] * 7)
    actualContentDeleteOptions = CharField(null=True)
    backupEnabled = BooleanField(null=True)
    actualContentBackupOptions = CharField(null=True)
    backupPath = CharField(null=True)
    backupSubFolders = CharField(null=True)


class DepositJob(BaseModel):
    initialTime = BigIntegerField(null=True)
    latestTime = BigIntegerField(null=True)
    depositStartTime = BigIntegerField(null=True)
    depositEndTime = BigIntegerField(null=True)
    finalizedTime = BigIntegerField(null=True)
    finishedTime = BigIntegerField(null=True)
    injectionPath = CharField(null=True)
    injectionTitle = CharField(null=True)
    fileCount = IntegerField(default=0)
    fileSize = BigIntegerField(default=0)
    isSuccessful = BooleanField(default=False)
    sipId = CharField(null=True)
    sipModule = CharField(null=True)
    sipStage = CharField(null=True)
    sipStatus = CharField(null=True)
    stage = CharField(null=True)
    state = CharField(null=True)
    depositSetId = CharField(null=True)
    resultMessage = TextField(null=True)
    # If you want to store the ID directly like the dataclass:
    appliedFlowSettingId = BigIntegerField(null=True)
    actualContentDeleted = BooleanField(default=False)
    backupCompleted = BooleanField(default=False)


class Whitelist(BaseModel):
    username = CharField(null=True)
    role = CharField(null=True)


class EntityID(BaseModel):
    key = CharField(primary_key=True)
    number = IntegerField(null=True)


class EntityStorageLocation(BaseModel):
    scanMode = CharField(null=True)
    rootPath = CharField(null=True)
    ftpServer = CharField(null=True)
    ftpPort = IntegerField(default=21)
    ftpUsername = CharField(null=True)
    ftpPassword = CharField(null=True)
    ftpProxyEnabled = BooleanField(default=False)
    ftpProxyHost = CharField(null=True)
    ftpProxyPort = IntegerField(default=0)
    ftpProxyUsername = CharField(null=True)
    ftpProxyPassword = CharField(null=True)


def init_db(persistent_storage):
    db_path = os.path.join(persistent_storage, "dashboard.db")
    db.init(db_path)
    db.connect()
    db.create_tables(
        [
            GlobalSetting,
            DepositAccount,
            FlowSetting,
            DepositJob,
            Whitelist,
            EntityID,
            EntityStorageLocation,
        ]
    )
    return db


def close_db():
    db.close()
