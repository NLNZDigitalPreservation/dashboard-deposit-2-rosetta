package nz.govt.natlib.dashboard.ui.command;

import nz.govt.natlib.dashboard.domain.entity.EntityStorageLocation;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;

public class FlowSettingCommand {
    private EntityFlowSetting flowSetting;
    private EntityStorageLocation injectionEndPoint;
    private EntityStorageLocation backupEndPoint;

    public EntityFlowSetting getFlowSetting() {
        return flowSetting;
    }

    public void setFlowSetting(EntityFlowSetting flowSetting) {
        this.flowSetting = flowSetting;
    }

    public EntityStorageLocation getInjectionEndPoint() {
        return injectionEndPoint;
    }

    public void setInjectionEndPoint(EntityStorageLocation injectionEndPoint) {
        this.injectionEndPoint = injectionEndPoint;
    }

    public EntityStorageLocation getBackupEndPoint() {
        return backupEndPoint;
    }

    public void setBackupEndPoint(EntityStorageLocation backupEndPoint) {
        this.backupEndPoint = backupEndPoint;
    }
}
