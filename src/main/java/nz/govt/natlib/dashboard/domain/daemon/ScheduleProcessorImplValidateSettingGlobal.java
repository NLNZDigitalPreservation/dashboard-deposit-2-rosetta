package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.domain.service.GlobalSettingService;

public class ScheduleProcessorImplValidateSettingGlobal {
    private GlobalSettingService globalSettingService;

    public void handle() {
        globalSettingService.validateGlobalSetting();
    }

    public GlobalSettingService getGlobalSettingService() {
        return globalSettingService;
    }

    public void setGlobalSettingService(GlobalSettingService globalSettingService) {
        this.globalSettingService = globalSettingService;
    }
}
