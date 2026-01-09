import os
from datetime import datetime

import falcon
from app.data import *


class DataServicesSettings(DataServicesAbstract):
    def __init__(self, args, repo: DataRepository):
        super().__init__(args, repo)

    def save_global_setting(self, global_setting: GlobalSetting) -> GlobalSetting:
        if global_setting.paused:
            assert_not_empty("PausedStartTime", global_setting.pausedStartTime)
            assert_not_empty("PausedEndTime", global_setting.pausedEndTime)

            now = datetime.now()
            try:
                ldt_paused_start = datetime.fromisoformat(global_setting.pausedStartTime)
                ldt_paused_end = datetime.fromisoformat(global_setting.pausedEndTime)
            except ValueError as e:
                raise ValueError(f"Invalid date format: {str(e)}")

            if ldt_paused_end < now:
                raise ValueError(f"The end time must after now")

            if ldt_paused_end < ldt_paused_start:
                raise ValueError(f"The end time must after start time")

        # Validation for Delays and DelayUnit (Equivalent to DashboardHelper.assertNotNull)
        assert_not_empty("Delays", global_setting.delays)
        assert_not_empty("DelayUnit", global_setting.delayUnit)

        # Ensure we are always updating the same singleton record
        global_setting = self.repo_global_settings.save(global_setting)

        return global_setting

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

        existing_account = self.repo_deposit_account.get_by_institute_name(deposit_account.depositUserInstitute)
        if is_insert and existing_account is not None:
            raise falcon.HTTPBadRequest(
                title="Bad Request",
                description=f"The account for institute {deposit_account.depositUserInstitute} already exists",
            )
        elif not is_insert and existing_account is not None and existing_account.id != deposit_account.id:
            raise falcon.HTTPBadRequest(
                title="Bad Request",
                description=f"The account for institute {deposit_account.depositUserInstitute} already exists",
            )

        self.repo_deposit_account.save(data_json)
        self.repo_deposit_account.save(deposit_account)
        return deposit_account

    def delete_deposit_account(self, data_json):
        assert_not_empty("DepositAccount", data_json)
        deposit_account = DepositAccount(**data_json)
        assert_not_empty("ID", deposit_account.id)

        existing_flow_settings = self.repo_flow_setting.get_by_account_id(deposit_account.id)
        if existing_flow_settings:
            raise falcon.HTTPBadRequest(
                title="Bad Request",
                description=f"The deposit account id={deposit_account.id} is still used by flow settings, please remove them first",
            )

        self.repo_deposit_account.delete(deposit_account.id)

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
        assert_not_empty("Ingestion Completed File Name", flow_setting.injectionCompleteFileName)
        assert_not_empty("MaxActiveDays", flow_setting.maxActiveDays)
        assert_not_empty("MaxStorageDays", flow_setting.maxSaveDays)
        assert_not_empty("ActualContentBackupOptions", flow_setting.actualContentBackupOptions)

        if not os.path.isdir(str(flow_setting.rootPath)):
            raise falcon.HTTPBadRequest(title="Bad Request", description="Invalid RootPath: the Root Path does not exist or is not a directory.")

        deposit_account = self.repo_deposit_account.get_by_id(flow_setting.depositAccountId)
        if deposit_account is None:
            raise falcon.HTTPBadRequest(title="Bad Request", description=f"The Deposit Account does not exist, depositAccountId: {flow_setting.depositAccountId}")

        # External Web Service Validation (Rosetta)
        try:
            if not self.rosetta_web_service.is_valid_producer(deposit_account, flow_setting.producerId):
                raise falcon.HTTPBadRequest(title="Bad Request", description="Invalid producerId")

            if not self.rosetta_web_service.is_valid_material_flow(deposit_account, flow_setting.producerId, flow_setting.materialFlowId):
                raise falcon.HTTPBadRequest(title="Bad Request", description="Invalid materialFlowId")
        except Exception as e:
            # Mapping WebServiceException to Falcon
            raise falcon.HTTPInternalServerError(title="Web Service Error", description=str(e))

        # Uniqueness/Duplication Logic
        # Note: For performance, it is better to use specific repo filter methods if available
        flow_settings: List[FlowSetting] = self.repo_flow_setting.all_data()
        for existing_flow in flow_settings:
            # Skip checking against itself if it's an update
            if not is_insert and existing_flow.id == flow_setting.id:
                continue

            if existing_flow.rootPath == flow_setting.rootPath:
                raise falcon.HTTPBadRequest(title="Bad Request", description="Duplicate RootPath")

            if existing_flow.materialFlowId == flow_setting.materialFlowId:
                raise falcon.HTTPBadRequest(title="Bad Request", description="Duplicate MaterialFlowId")

        # Conditional Backup Validation
        if str(flow_setting.actualContentBackupOptions).lower() != "notbackup":
            assert_not_empty("Backup Path", flow_setting.backupPath)
            assert_not_empty("Backup Sub Folders", flow_setting.backupSubFolders)

        # Save and Return
        flow_setting = self.repo_flow_setting.save(flow_setting)
        return flow_setting

    def delete_flow_setting(self, flow_id: int) -> FlowSetting:
        # 1. Verify existence
        flow_setting = self.repo_flow_setting.get_by_id(flow_id)
        if flow_setting is None:
            raise falcon.HTTPNotFound(title="Not Found", description=f"Not able to find material flow: {flow_id}")

        # 2. Check for dependencies (Referential Integrity)
        # Checking if any deposit jobs are linked to this flow
        jobs = self.deposit_job.get_by_flow_id(flow_id)
        if jobs:  # In Python, an empty list evaluates to False
            raise falcon.HTTPConflict(title="Conflict", description="The flow is referenced by deposit jobs and cannot be deleted.")

        # 3. Perform deletion
        self.repo_flow_setting.delete_by_id(flow_id)

        return flow_setting
