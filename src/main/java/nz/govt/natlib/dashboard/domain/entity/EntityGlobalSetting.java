package nz.govt.natlib.dashboard.domain.entity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class EntityGlobalSetting {
    @PrimaryKey
    private Long id = 0L;

    private String depositUserInstitute;
    private String depositUserName;
    private String depositUserPassword;
    private String depositUserPasswordConfirm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getDepositUserPasswordConfirm() {
        return depositUserPasswordConfirm;
    }

    public void setDepositUserPasswordConfirm(String depositUserPasswordConfirm) {
        this.depositUserPasswordConfirm = depositUserPasswordConfirm;
    }

    private Boolean auditRst;
    private String auditMsg;

    public Boolean getAuditRst() {
        return auditRst;
    }

    public void setAuditRst(Boolean auditRst) {
        this.auditRst = auditRst;
    }

    public String getAuditMsg() {
        return auditMsg;
    }

    public void setAuditMsg(String auditMsg) {
        this.auditMsg = auditMsg;
    }
}
