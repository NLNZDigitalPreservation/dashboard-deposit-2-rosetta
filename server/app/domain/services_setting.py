import os
from datetime import datetime
import threading

import falcon
from app.auth.sessions import RoleType, SessionManager
from app.domain import *

GLOBAL_SINGLE_ID = 1


class ServicesSetting:
    def __init__(self, args, session_manager):
        self.args = args
        self.session_manager: SessionManager = session_manager
        self.lock = threading.Lock()

    def get_global_setting(self) -> GlobalSetting:
        with self.lock:
            instance = GlobalSetting.get_or_none(
                GlobalSetting.id == GLOBAL_SINGLE_ID
            )  # Ensure singleton with ID=1
            if instance is None:
                instance = GlobalSetting.create(id=GLOBAL_SINGLE_ID, delays=60)
            return instance

    def save_global_setting(self, data_json) -> GlobalSetting:
        assert_not_empty("GlobalSetting", data_json)
        global_setting = GlobalSetting(**data_json)

        if global_setting.id is not None and global_setting.id != GLOBAL_SINGLE_ID:
            raise falcon.HTTPBadRequest(
                title="Bad Request", description="Invalid GlobalSetting ID"
            )

        if global_setting.paused:
            assert_not_empty("PausedStartTime", global_setting.pausedStartTime)
            assert_not_empty("PausedEndTime", global_setting.pausedEndTime)

            now = datetime.now()
            try:
                ldt_paused_start = datetime.fromtimestamp(
                    global_setting.pausedStartTime
                )
                ldt_paused_end = datetime.fromtimestamp(global_setting.pausedEndTime)
            except ValueError as e:
                raise falcon.HTTPBadRequest(
                    title="Bad Request", description=f"Invalid date format: {str(e)}"
                )

            if ldt_paused_end < now:
                raise falcon.HTTPBadRequest(
                    title="Bad Request", description=f"The end time must after now"
                )

            if ldt_paused_end < ldt_paused_start:
                raise falcon.HTTPBadRequest(
                    title="Bad Request",
                    description=f"The end time must after start time",
                )

        # Validation for Delays and DelayUnit (Equivalent to DashboardHelper.assertNotNull)
        assert_not_empty("Delays", global_setting.delays)
        assert_not_empty("DelayUnit", global_setting.delayUnit)

        with self.lock:
            global_setting.id = GLOBAL_SINGLE_ID  # Ensure singleton ID
            global_setting.save()
            return global_setting

    def _assert_privilege(self, token: str, user: Whitelist):
        role = self.session_manager.get_role(token)
        if role not in [RoleType.BOOTSTRAP, RoleType.ADMIN]:
            raise falcon.HTTPForbidden(
                title="NoPrivilege",
                description="You have no privilege to edit the white list",
            )

        if user.username == "bootstrap":
            raise falcon.HTTPForbidden(
                title="Forbidden",
                description="The bootstrap user can not be changed",
            )
        cur_login_name = self.session_manager.get_username(token)
        if cur_login_name == user.username:
            raise falcon.HTTPForbidden(
                title="Forbidden", description="You can not edit your self"
            )

    def save_white_list(self, token: str, data_json, is_insert: bool) -> Whitelist:
        assert_not_empty("Whitelist User", data_json)
        user = Whitelist(**data_json)
        if is_insert:
            assert_empty("ID", user.id)
        else:
            assert_not_empty("ID", user.id)
        assert_not_empty("username", user.username)
        assert_not_empty("role", user.role)

        existing_user = Whitelist.get_or_none(Whitelist.username == user.username)
        if is_insert and existing_user is not None:
            raise falcon.HTTPBadRequest(
                title="Bad Request", description=f"The user {user.username} does exist"
            )
        elif (
            not is_insert and existing_user is not None and existing_user.id != user.id
        ):
            raise falcon.HTTPBadRequest(
                title="Bad Request",
                description=f"The white list user {user.username} already exists",
            )
        self._assert_privilege(token=token, user=user)
        user.save(force_insert=is_insert)
        return user

    def delete_white_list(self, token: str, user_id: int) -> Whitelist:
        user = Whitelist.get_or_none(Whitelist.id == user_id)
        if user is None:
            raise falcon.HTTPNotFound(
                title="Not Found", description=f"The user {user_id} does not exist"
            )
        self._assert_privilege(token=token, user=user)
        Whitelist.delete().where(Whitelist.id == user_id).execute()
        return user

    def save_deposit_account(self, data_json, is_insert: bool) -> DepositAccount:
        assert_not_empty("DepositAccount", data_json)
        deposit_account = DepositAccount(**data_json)
        if is_insert:
            assert_empty("ID", deposit_account.id)
        else:
            assert_not_empty("ID", deposit_account.id)
        assert_not_empty("DepositUserInstitute", deposit_account.depositUserInstitute)
        assert_not_empty("DepositUserName", deposit_account.depositUserName)
        assert_not_empty("DepositUserPassword", deposit_account.depositUserPassword)

        existing_account = DepositAccount.get_or_none(
            DepositAccount.depositUserName == deposit_account.depositUserName
        )
        if is_insert and existing_account is not None:
            raise falcon.HTTPBadRequest(
                title="Bad Request",
                description=f"The account for institute {deposit_account.depositUserInstitute} already exists",
            )
        elif (
            not is_insert
            and existing_account is not None
            and existing_account.id != deposit_account.id
        ):
            raise falcon.HTTPBadRequest(
                title="Bad Request",
                description=f"The account for institute {deposit_account.depositUserInstitute} already exists",
            )
        deposit_account.save(force_insert=is_insert)
        return deposit_account

    def delete_deposit_account(self, deposit_account_id: int) -> DepositAccount:
        deposit_account = DepositAccount.get_or_none(
            DepositAccount.id == deposit_account_id
        )
        if deposit_account is None:
            raise falcon.HTTPNotFound(
                title="Not Found",
                description=f"Not able to find deposit account: {deposit_account_id}",
            )

        existing_flow_settings = FlowSetting.select().where(
            FlowSetting.depositAccountId == deposit_account.id
        )
        if existing_flow_settings.count() > 0:
            raise falcon.HTTPBadRequest(
                title="Bad Request",
                description=f"The deposit account id={deposit_account.id} is still used by flow settings, please remove them first",
            )
        DepositAccount.delete().where(DepositAccount.id == deposit_account.id).execute()
        return deposit_account

    def save_flow_setting(self, data_json, is_insert: bool) -> FlowSetting:
        assert_not_empty("FlowSetting", data_json)
        flow_setting = FlowSetting(**data_json)
        if is_insert:
            assert_empty("ID", flow_setting.id)
        else:
            assert_not_empty("ID", flow_setting.id)
        assert_not_empty("Enabled", flow_setting.enabled)
        assert_not_empty("ProducerId", flow_setting.producerId)
        assert_not_empty("RootPath", flow_setting.rootPath)
        assert_not_empty("MaterialFlowId", flow_setting.materialFlowId)
        assert_not_empty("Stream Location", flow_setting.streamLocation)
        assert_not_empty(
            "Ingestion Completed File Name", flow_setting.injectionCompleteFileName
        )
        assert_not_empty("MaxActiveDays", flow_setting.maxActiveDays)
        assert_not_empty("MaxStorageDays", flow_setting.maxSaveDays)
        assert_not_empty(
            "ActualContentBackupOptions", flow_setting.actualContentBackupOptions
        )

        if not os.path.isdir(str(flow_setting.rootPath)):
            raise falcon.HTTPBadRequest(
                title="Bad Request",
                description="Invalid RootPath: the Root Path does not exist or is not a directory.",
            )

        deposit_account = DepositAccount.get_or_none(
            DepositAccount.id == flow_setting.depositAccountId
        )
        if deposit_account is None:
            raise falcon.HTTPBadRequest(
                title="Bad Request",
                description=f"The Deposit Account does not exist, depositAccountId: {flow_setting.depositAccountId}",
            )

        # External Web Service Validation (Rosetta)
        try:
            if not self.rosetta_web_service.is_valid_producer(
                deposit_account, flow_setting.producerId
            ):
                raise falcon.HTTPBadRequest(
                    title="Bad Request", description="Invalid producerId"
                )

            if not self.rosetta_web_service.is_valid_material_flow(
                deposit_account, flow_setting.producerId, flow_setting.materialFlowId
            ):
                raise falcon.HTTPBadRequest(
                    title="Bad Request", description="Invalid materialFlowId"
                )
        except Exception as e:
            # Mapping WebServiceException to Falcon
            raise falcon.HTTPInternalServerError(
                title="Web Service Error", description=str(e)
            )

        # Uniqueness/Duplication Logic
        # Note: For performance, it is better to use specific repo filter methods if available
        flow_settings = FlowSetting.select()
        for existing_flow in flow_settings:
            # Skip checking against itself if it's an update
            if not is_insert and existing_flow.id == flow_setting.id:
                continue

            if existing_flow.rootPath == flow_setting.rootPath:
                raise falcon.HTTPBadRequest(
                    title="Bad Request", description="Duplicate RootPath"
                )

            if existing_flow.materialFlowId == flow_setting.materialFlowId:
                raise falcon.HTTPBadRequest(
                    title="Bad Request", description="Duplicate MaterialFlowId"
                )

        # Conditional Backup Validation
        if str(flow_setting.actualContentBackupOptions).lower() != "notbackup":
            assert_not_empty("Backup Path", flow_setting.backupPath)
            assert_not_empty("Backup Sub Folders", flow_setting.backupSubFolders)

        # Save and Return
        flow_setting.save(force_insert=is_insert)
        return flow_setting

    def delete_flow_setting(self, flow_setting_id: int) -> FlowSetting:
        # 1. Verify existence
        flow_setting = FlowSetting.get_or_none(FlowSetting.id == flow_setting_id)
        if flow_setting is None:
            raise falcon.HTTPNotFound(
                title="Not Found",
                description=f"Not able to find material flow: {flow_setting_id}",
            )

        # 2. Check for dependencies (Referential Integrity)
        # Checking if any deposit jobs are linked to this flow
        jobs = self.deposit_job.get_by_flow_id(flow_setting.id)
        if jobs:  # In Python, an empty list evaluates to False
            raise falcon.HTTPConflict(
                title="Conflict",
                description="The flow is referenced by deposit jobs and cannot be deleted.",
            )

        # 3. Perform deletion
        self.repo_flow_setting.delete_by_id(flow_setting.id)

        return flow_setting
