package nz.govt.natlib.dashboard.domain.entity;

public class EntityProducer extends EntityCommon {
    private String producerId;
    private String producerName;
    private String depositUserInstitute;
    private String depositUserName;
    private String depositUserPassword;
    private String depositUserPasswordConfirm;

    public String getProducerId() {
        return producerId;
    }

    public void setProducerId(String producerId) {
        this.producerId = producerId;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
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
}
