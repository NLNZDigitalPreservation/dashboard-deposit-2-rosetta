package nz.govt.natlib.dashboard.common.core.dto;

public class DtoDepositRsp {
    private String id;
    private String creation_date;
    private String submission_date;
    private String update_date;
    private String status;
    private String title;
    private String sip_id;
    private String sip_reason;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(String creation_date) {
        this.creation_date = creation_date;
    }

    public String getSubmission_date() {
        return submission_date;
    }

    public void setSubmission_date(String submission_date) {
        this.submission_date = submission_date;
    }

    public String getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(String update_date) {
        this.update_date = update_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSip_id() {
        return sip_id;
    }

    public void setSip_id(String sip_id) {
        this.sip_id = sip_id;
    }

    public String getSip_reason() {
        return sip_reason;
    }

    public void setSip_reason(String sip_reason) {
        this.sip_reason = sip_reason;
    }
}
