package nz.govt.natlib.dashboard.domain.entity;

public class EntityDepositAccountSetting extends EntityCommon {
    private String depositUserInstitute;
    private String depositUserName;
    private String depositUserPassword;

    public String getDepositUserInstitute() {
        return depositUserInstitute;
    }

    public void setDepositUserInstitute(String depositUserInstitute) {
        this.depositUserInstitute = depositUserInstitute;
    }

    public String getDepositUserName() {
        return depositUserName;
    }

    public void setDepositUserName(String depositUserName) {
        this.depositUserName = depositUserName;
    }

    public String getDepositUserPassword() {
        return depositUserPassword;
    }

    public void setDepositUserPassword(String depositUserPassword) {
        this.depositUserPassword = depositUserPassword;
    }

    public String toString() {
        return String.format("%s-%s:*****", depositUserName, depositUserInstitute);
    }
}
