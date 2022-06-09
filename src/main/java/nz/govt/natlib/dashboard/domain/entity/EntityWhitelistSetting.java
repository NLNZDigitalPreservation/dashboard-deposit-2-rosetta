package nz.govt.natlib.dashboard.domain.entity;

import nz.govt.natlib.dashboard.common.metadata.EnumUserRole;

public class EntityWhitelistSetting extends EntityCommon {
    private String whiteUserName;
    private EnumUserRole whiteUserRole;

    public String getWhiteUserName() {
        return whiteUserName;
    }

    public void setWhiteUserName(String whiteUserName) {
        this.whiteUserName = whiteUserName;
    }

    public String getWhiteUserRole() {
        return whiteUserRole.name();
    }

    public void setWhiteUserRole(String whiteUserRole) {
        this.whiteUserRole = EnumUserRole.valueOf(whiteUserRole);
    }
}
