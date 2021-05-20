package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.service.FlowSettingService;

public class ScheduleProcessorImplValidateSettingFlow extends ScheduleProcessor {
    private FlowSettingService flowSettingService;

    @Override
    public void handle(EntityFlowSetting flowSetting) throws Exception {
        try {
            flowSettingService.validateFlowSetting(flowSetting);
            flowSetting.setAuditRst(true);
            flowSetting.setAuditMsg("OK");
        } catch (Exception e) {
            flowSetting.setAuditRst(false);
            flowSetting.setAuditMsg(e.getMessage());
            log.error("Failed to validate flow setting", e);
        }

        repoFlowSetting.save(flowSetting);
    }

    public FlowSettingService getFlowSettingService() {
        return flowSettingService;
    }

    public void setFlowSettingService(FlowSettingService flowSettingService) {
        this.flowSettingService = flowSettingService;
    }
}