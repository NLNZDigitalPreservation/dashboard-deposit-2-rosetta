import json
import time
from typing import Optional

from peewee import (
    BigAutoField,
    BigIntegerField,
    BooleanField,
    CharField,
    DateTimeField,
    DoubleField,
    Field,
    IntegerField,
    Model,
    PostgresqlDatabase,
    Proxy,
    SmallIntegerField,
    TextField,
)
from playhouse.pool import PooledPsycopg3Database

# Do NOT import Psycopg3JSONField from peewee or postgres_ext


class Psycopg3JSONField(Field):
    """
    Handles JSONB for Psycopg3 by manually serializing to string.
    This fixes the 'cannot adapt type dict' error.
    """

    field_type = "JSONB"

    def db_value(self, value):
        if value is None:
            return None

        # Convert the Python dict/list into a JSON string.
        # This tells Psycopg3 exactly what to send to the database.
        return json.dumps(value)

    def python_value(self, value):
        if value is None:
            return None

        # If the value is a string (common), parse it into a dict/list.
        if isinstance(value, str):
            return json.loads(value)

        # If Psycopg3 already parsed it into a dict (depends on driver config),
        # just return it as is.
        return value


class DatabaseManager:
    def __init__(self):
        self.database: Optional[PostgresqlDatabase] = None
        self.database_proxy = Proxy()

    def initialize_models(self, args):
        self.database = PooledPsycopg3Database(
            args.db_name,
            user=args.dbms_username,
            password=args.dbms_password,
            host=args.dbms_hostname,
            port=args.dbms_port,
            max_connections=20,
        )
        self.database_proxy.initialize(self.database)

    def connect(self):
        # Use the proxy to connect to ensure models are ready
        return self.database_proxy.connect(reuse_if_open=True)

    def close(self):
        if not self.database_proxy.is_closed():
            self.database_proxy.close()


db_manager = DatabaseManager()


class BaseModel(Model):
    id = BigIntegerField(primary_key=True)
    audit_rst = BooleanField(default=True, null=False)
    audit_msg = TextField(default="OK", null=False)

    class Meta:
        database = db_manager.database_proxy
        legacy_table_names = False
        abstract = True


class GlobalSetting(BaseModel):
    paused = BooleanField(default=False, null=False)
    paused_start_time = TextField(null=False)
    paused_end_time = TextField(null=False)
    delays = BigIntegerField(null=False)
    delay_unit = CharField(max_length=8, null=False)

    class Meta:
        table_name = "global_setting"


class DepositAccount(BaseModel):
    deposit_user_institute = CharField(max_length=64, null=False)
    deposit_user_name = CharField(max_length=255, null=False)
    deposit_user_password = TextField(null=False)
    producers = Psycopg3JSONField(default=list, null=False)

    class Meta:
        table_name = "deposit_account"


class StorageLocation(BaseModel):
    scan_mode = CharField(max_length=8, null=True)
    root_path = TextField(null=True)
    ftp_server = TextField(null=True)
    ftp_port = IntegerField(default=0, null=False)
    ftp_username = TextField(null=True)
    ftp_password = TextField(null=True)
    ftp_proxy_enabled = BooleanField(default=False, null=False)
    ftp_proxy_host = TextField(null=True)
    ftp_proxy_port = IntegerField(default=0, null=False)
    ftp_proxy_username = TextField(null=True)
    ftp_proxy_password = TextField(null=True)

    class Meta:
        table_name = "storage_location"


class WhitelistSetting(BaseModel):
    white_user_name = CharField(max_length=255, null=False)
    white_user_role = CharField(max_length=16, null=False)

    class Meta:
        table_name = "whitelist_setting"
        indexes = ((("white_user_name",), True),)


class FlowSetting(BaseModel):
    enabled = BooleanField(default=False, null=False)
    deposit_account_id = BigIntegerField(null=False)
    material_flow_id = TextField(null=False)
    material_flow_name = TextField(null=True)
    producer_id = TextField(null=False)
    producer_name = TextField(null=True)
    root_path = TextField(null=False)
    stream_location = TextField(null=False)
    injection_complete_file_name = TextField(null=False)
    max_active_days = BigIntegerField(null=False)
    max_save_days = BigIntegerField(null=False)
    delays = BigIntegerField(null=True)
    delay_unit = CharField(max_length=8, null=True)
    weekly_max_concurrency = Psycopg3JSONField(
        default=lambda: [0, 0, 0, 0, 0, 0, 0], null=False
    )
    actual_content_delete_options = TextField(null=True)
    backup_enabled = BooleanField(default=False, null=False)
    actual_content_backup_options = TextField(null=False)
    backup_path = TextField(null=True)
    backup_sub_folders = TextField(null=True)

    class Meta:
        table_name = "flow_setting"
        indexes = (
            ((("material_flow_id",), True)),
            ((("root_path",), True)),
            ((("deposit_account_id",), False)),
        )


class DepositJob(BaseModel):
    initial_time = BigIntegerField(null=True)
    latest_time = BigIntegerField(null=True)
    deposit_start_time = BigIntegerField(null=True)
    deposit_end_time = BigIntegerField(null=True)
    finalized_time = BigIntegerField(null=True)
    finished_time = BigIntegerField(null=True)

    injection_path = TextField(null=True)
    injection_title = TextField(null=True)

    file_count = BigIntegerField(default=0, null=False)
    file_size = BigIntegerField(default=0, null=False)

    is_successful = BooleanField(default=False, null=False)

    sip_id = TextField(null=True)
    sip_module = TextField(null=True)
    sip_stage = TextField(null=True)
    sip_status = TextField(null=True)

    stage = CharField(max_length=16, null=False)
    state = CharField(max_length=16, null=False)

    deposit_set_id = TextField(null=True)
    result_message = TextField(null=True)

    applied_flow_setting = Psycopg3JSONField(null=False)

    actual_content_deleted = BooleanField(default=False, null=False)
    backup_completed = BooleanField(default=False, null=False)

    class Meta:
        table_name = "deposit_job"
        indexes = (
            ((("initial_time",), False)),
            ((("latest_time",), False)),
            ((("sip_id",), False)),
            ((("state",), False)),
        )
