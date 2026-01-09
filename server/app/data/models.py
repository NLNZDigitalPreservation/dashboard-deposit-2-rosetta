from __future__ import annotations
from dataclasses import dataclass, field
from typing import List, Optional
from enum import Enum


# Helper Enums (Assuming these exist based on your Java imports)
class EnumDepositJobStage(Enum):
    # Add actual stages here
    START = "START"
    END = "END"


class EnumDepositJobState(Enum):
    # Add actual states here
    RUNNING = "RUNNING"
    FINISHED = "FINISHED"


class EnumUserRole(Enum):
    ADMIN = "ADMIN"
    USER = "USER"


@dataclass
class EntityCommon:
    id: Optional[int] = None
    audit_rst: bool = True
    audit_msg: str = "OK"


@dataclass
class GlobalSetting(EntityCommon):
    paused: bool = False
    paused_start_time: Optional[str] = None
    paused_end_time: Optional[str] = None
    delays: Optional[int] = None
    delay_unit: str = "S"

    @property
    def delay_time_unit_seconds(self) -> int:
        """
        Python doesn't have a built-in TimeUnit like Java.
        This returns the multiplier for seconds.
        """
        mapping = {"S": 1, "M": 60, "H": 3600, "D": 86400}
        return mapping.get(self.delay_unit.upper(), 1)


@dataclass
class DepositAccount(EntityCommon):
    deposit_user_institute: Optional[str] = None
    deposit_user_name: Optional[str] = None
    deposit_user_password: Optional[str] = None
    # Assuming DtoProducersRsp.Producer is a dictionary or another class
    producers: list = field(default_factory=list)

    def __str__(self):
        return f"{self.deposit_user_name}-{self.deposit_user_institute}:*****"


@dataclass
class FlowSetting(EntityCommon):
    enabled: bool = False
    deposit_account_id: Optional[int] = None
    material_flow_id: Optional[str] = None
    material_flow_name: Optional[str] = None
    producer_id: Optional[str] = None
    producer_name: Optional[str] = None
    root_path: Optional[str] = None
    stream_location: Optional[str] = None
    injection_complete_file_name: Optional[str] = None
    max_active_days: Optional[int] = None
    max_save_days: Optional[int] = None
    delays: Optional[int] = None
    delay_unit: Optional[str] = None
    weekly_max_concurrency: list[int] = field(default_factory=lambda: [0] * 7)
    actual_content_delete_options: Optional[str] = None
    backup_enabled: Optional[bool] = None
    actual_content_backup_options: Optional[str] = None
    backup_path: Optional[str] = None
    backup_sub_folders: Optional[str] = None


@dataclass
class DepositJob(EntityCommon):
    initial_time: Optional[int] = None
    latest_time: Optional[int] = None
    deposit_start_time: Optional[int] = None
    deposit_end_time: Optional[int] = None
    finalized_time: Optional[int] = None
    finished_time: Optional[int] = None
    injection_path: Optional[str] = None
    injection_title: Optional[str] = None
    file_count: int = 0
    file_size: int = 0
    is_successful: bool = False
    sip_id: Optional[str] = None
    sip_module: Optional[str] = None
    sip_stage: Optional[str] = None
    sip_status: Optional[str] = None
    stage: Optional[EnumDepositJobStage] = None
    state: Optional[EnumDepositJobState] = None
    deposit_set_id: Optional[str] = None
    result_message: Optional[str] = None
    applied_flow_setting: Optional[FlowSetting] = None
    actual_content_deleted: bool = False
    backup_completed: bool = False


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
    scan_mode: Optional[str] = None
    root_path: Optional[str] = None
    ftp_server: Optional[str] = None
    ftp_port: int = 21
    ftp_username: Optional[str] = None
    ftp_password: Optional[str] = None
    ftp_proxy_enabled: bool = False
    ftp_proxy_host: Optional[str] = None
    ftp_proxy_port: int = 0
    ftp_proxy_username: Optional[str] = None
    ftp_proxy_password: Optional[str] = None
