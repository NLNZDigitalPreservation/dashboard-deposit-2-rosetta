package nz.govt.natlib.dashboard.domain.entity;

public class EntityCommon {
    private Long id;
    private Boolean auditRst = Boolean.TRUE;
    private String auditMsg = "OK";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
