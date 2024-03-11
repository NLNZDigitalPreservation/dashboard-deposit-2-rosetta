package nz.govt.natlib.dashboard.domain.entity;

import nz.govt.natlib.dashboard.common.core.dto.DtoProducersRsp;

import java.util.ArrayList;
import java.util.List;

public class EntityDepositAccountSetting extends EntityCommon {
    private String depositUserInstitute;
    private String depositUserName;
    private String depositUserPassword;

    private List<DtoProducersRsp.Producer> producers=new ArrayList<>();

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

    public List<DtoProducersRsp.Producer> getProducers() {
        return producers;
    }

    public void setProducers(List<DtoProducersRsp.Producer> producers) {
        this.producers = producers;
    }

    public String toString() {
        return String.format("%s-%s:*****", depositUserName, depositUserInstitute);
    }
}
