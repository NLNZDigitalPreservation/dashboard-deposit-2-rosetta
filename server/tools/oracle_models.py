from typing import Optional
from peewee import Database, Model, Proxy, BigAutoField, BigIntegerField, SmallIntegerField, CharField, DateTimeField, TextField

from common.db.db_access_rosetta import RosettaDatabaseHandler


class OracleDatabase(Database):
    param_style = "qmark"  # Oracle uses :1 or :name for bind variables

    def __init__(self, args, **kwargs):
        # connection: an existing oracledb.Connection
        self.db_handler = RosettaDatabaseHandler(args)
        self._connection = None
        super().__init__("db_fixity", **kwargs)

    def _connect(self):
        if self._connection is None:
            self._connection = self.db_handler.create_connection()
        return self._connection

    def execute_sql(self, sql, params=None, commit=True):
        if params is None:
            params = ()

        # --- THE FIX ---
        # Oracle driver strictly requires :1, :2, :3 for positional args.
        # Peewee usually generates '?' or '%s'. We replace them dynamically.
        if params and "?" in sql:
            # Replace each '?' with :1, :2, :3... sequentially
            param_count = len(params)
            for i in range(1, param_count + 1):
                sql = sql.replace("?", f":{i}", 1)
        elif params and "%s" in sql:
            # Just in case it generated %s
            param_count = len(params)
            for i in range(1, param_count + 1):
                sql = sql.replace("%s", f":{i}", 1)

        # Connect and Execute
        cursor = self._connect().cursor()
        try:
            cursor.execute(sql, params)
            if commit:
                self._connect().commit()
            return cursor
        except Exception:
            if commit:
                self._connect().rollback()
            raise


class OracleDatabaseManager:
    def __init__(self):
        self.database: Optional[OracleDatabase] = None
        self.database_proxy = Proxy()

    def initialize_models(self, args):
        self.database = OracleDatabase(args=args)
        self.database_proxy.initialize(self.database)
        self.database.connect(reuse_if_open=True)

    def close(self):
        self.database.close()


oracle_db_manager = OracleDatabaseManager()


class OracleBaseModel(Model):
    class Meta:
        database = oracle_db_manager.database_proxy
        legacy_tables_names = False


class RosettaPermanentIndex(OracleBaseModel):
    id = BigAutoField(primary_key=True)

    file_size = BigIntegerField(null=True)
    version = BigIntegerField(null=True)
    status = BigIntegerField(null=True)

    stored_entity_id = CharField(max_length=255, null=True)
    check_sum_type = CharField(max_length=255, null=True)

    storage_id = BigIntegerField(null=True)
    update_date = DateTimeField(null=True)

    storage_entity_type = CharField(max_length=8, null=True)
    index_location = CharField(max_length=255, null=True)

    check_sum = CharField(max_length=255, null=True)
    update_check_sum = SmallIntegerField(null=True)

    phys_check_sum = CharField(max_length=255, null=True)
    phys_check_sum_type = CharField(max_length=255, null=True)

    xsd_versions = CharField(max_length=50, null=True)
    created_by = CharField(max_length=255, null=True)

    title = CharField(max_length=4000, null=True)  # VARCHAR(4000)

    class Meta:
        # table_name = "V2PN_PER00.permanent_index"
        table_name = "PERMANENT_INDEX"
        schema = "V2PN_PER00"
        table_alias = None  # 🔑 IMPORTANT


class RosettaStorageParameter(OracleBaseModel):
    # Defined as BIGINT NOT NULL in SQL (not serial), so we use BigIntegerField
    # id = BigIntegerField(primary_key=True)
    id = BigAutoField(primary_key=True)

    value = CharField(max_length=4000, null=True)
    key = CharField(max_length=50, null=True)
    storage_id = BigIntegerField(null=True)

    class Meta:
        table_name = "storage_parameter"
        schema = "V2PN_SHR00"
        table_alias = None  # 🔑 IMPORTANT
