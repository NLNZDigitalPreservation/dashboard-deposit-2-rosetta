import logging
from datetime import datetime, timedelta
from typing import Optional

from app.data.models import GlobalSetting


# Assuming these exist based on your Java snippet
class RestResponseCommand:
    RSP_INVALID_INPUT_PARAMETERS = " (Invalid Input Parameters)"


class GlobalSettingService:
    UNIQUE_GLOBAL_SETTING_ID = 0
    # The format used in the Java substring(0, 16) is "YYYY-MM-DDTHH:MM"
    ISO_FORMAT_SHORT = "%Y-%m-%dT%H:%M"

    def __init__(self, repo_global_setting):
        self.logger = logging.getLogger(__name__)
        self.repo_global_setting = repo_global_setting

    def get_global_setting(self) -> GlobalSetting:
        global_setting: Optional[GlobalSetting] = self.repo_global_setting.get_global_setting()
        now = datetime.now()

        if global_setting is None:
            # Create default setting if none exists
            global_setting = GlobalSetting()
            global_setting.id = self.UNIQUE_GLOBAL_SETTING_ID
            global_setting.pausedStartTime = now.strftime(self.ISO_FORMAT_SHORT)

            paused_end_time = now + timedelta(days=1)
            global_setting.pausedEndTime = paused_end_time.strftime(self.ISO_FORMAT_SHORT)
            global_setting.delayUnit = "S"
            global_setting.delays = 60
        else:
            # Validate and parse pausedStartTime
            try:
                # Python's fromisoformat handles the YYYY-MM-DDTHH:MM format
                datetime.fromisoformat(global_setting.pausedStartTime)
            except (ValueError, TypeError):
                self.logger.warning(f"Failed to parse pausedStartTime: {global_setting.pausedStartTime}")
                global_setting.pausedStartTime = now.strftime(self.ISO_FORMAT_SHORT)

            # Validate and parse pausedEndTime
            ldt_paused_end_time: datetime
            try:
                ldt_paused_end_time = datetime.fromisoformat(global_setting.pausedEndTime)
            except (ValueError, TypeError):
                self.logger.warning(f"Failed to parse pausedEndTime: {global_setting.pausedEndTime}")
                ldt_paused_end_time = now + timedelta(days=1)
                global_setting.pausedEndTime = ldt_paused_end_time.strftime(self.ISO_FORMAT_SHORT)

            # Auto-unpause if the end time has already passed
            if ldt_paused_end_time < now:
                global_setting.paused = False

        return global_setting

    def save_global_setting(self, global_setting: GlobalSetting) -> GlobalSetting:
        if global_setting.paused:
            if not global_setting.pausedStartTime:
                raise ValueError(f"The start time is empty: {RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS}")
            if not global_setting.pausedEndTime:
                raise ValueError(f"The end time is empty: {RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS}")

            now = datetime.now()
            try:
                ldt_paused_start = datetime.fromisoformat(global_setting.pausedStartTime)
                ldt_paused_end = datetime.fromisoformat(global_setting.pausedEndTime)
            except ValueError as e:
                raise ValueError(f"Invalid date format: {str(e)} {RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS}")

            if ldt_paused_end < now:
                raise ValueError(f"The end time must after now: {RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS}")

            if ldt_paused_end < ldt_paused_start:
                raise ValueError(f"The end time must after start time: {RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS}")

        # Validation for Delays and DelayUnit (Equivalent to DashboardHelper.assertNotNull)
        if global_setting.delays is None:
            raise ValueError(f"Delays cannot be null {RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS}")
        if global_setting.delayUnit is None:
            raise ValueError(f"DelayUnit cannot be null {RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS}")

        # Ensure we are always updating the same singleton record
        global_setting.id = self.UNIQUE_GLOBAL_SETTING_ID
        self.repo_global_setting.save(global_setting)

        return global_setting
