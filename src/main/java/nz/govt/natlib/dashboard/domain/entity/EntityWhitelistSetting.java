package nz.govt.natlib.dashboard.domain.entity;

import nz.govt.natlib.dashboard.common.metadata.EnumUserRole;

public class EntityWhitelistSetting extends EntityCommon{
    private String userName;
    private EnumUserRole role;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public EnumUserRole getRole() {
        return role;
    }

    public void setRole(EnumUserRole role) {
        this.role = role;
    }
}
