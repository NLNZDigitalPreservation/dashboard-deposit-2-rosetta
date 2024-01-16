package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.common.core.dto.DtoProducersRsp;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositAccount;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepositAccountSettingService {
    private static final Logger log = LoggerFactory.getLogger(DepositAccountSettingService.class);
    @Autowired
    private RosettaWebService rosettaWebService;
    @Autowired
    private RepoDepositAccount repoDepositAccount;
    @Autowired
    private RepoFlowSetting repoFlowSetting;

    public RestResponseCommand getAllDepositAccountSettings() {
        RestResponseCommand rstVal = new RestResponseCommand();
        List<EntityDepositAccountSetting> data = repoDepositAccount.getAll();
        rstVal.setRspBody(data);
        data.clear();
        return rstVal;
    }

    public RestResponseCommand saveDepositAccountSetting(EntityDepositAccountSetting producer) throws Exception {
        //Validate the producer
        DashboardHelper.assertNotNull("Producer", producer);
        DashboardHelper.assertNotNull("DepositUserInstitute", producer.getDepositUserInstitute());
        DashboardHelper.assertNotNull("DepositUserName", producer.getDepositUserName());
        DashboardHelper.assertNotNull("DepositUserPassword", producer.getDepositUserPassword());
//        rosettaWebService.login(producer.getDepositUserInstitute(), producer.getDepositUserName(), producer.getDepositUserPassword());

        RestResponseCommand rstVal = new RestResponseCommand();
        String pdsHandle = rosettaWebService.login(producer.getDepositUserInstitute(), producer.getDepositUserName(), producer.getDepositUserPassword());
        if (StringUtils.isEmpty(pdsHandle)) {
            String err_msg = "Invalid deposit username or password";
            log.error(err_msg);
            rstVal.setRspCode(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            rstVal.setRspMsg(err_msg);
            return rstVal;
        }
        rosettaWebService.logout(pdsHandle);

        List<DtoProducersRsp.Producer> producers = rosettaWebService.getProducers(producer);
        producer.setProducers(producers);

        repoDepositAccount.save(producer);
        producers.clear();
        return rstVal;
    }

    public RestResponseCommand deleteDepositAccountSetting(Long id) {
        RestResponseCommand retVal = new RestResponseCommand();
        List<EntityFlowSetting> flowSettings = repoFlowSetting.getAll();
        for (EntityFlowSetting flowSetting : flowSettings) {
            if (flowSetting.getDepositAccountId() == id.longValue()) {
                retVal.setRspCode(RestResponseCommand.RSP_USER_OTHER_ERROR);
                retVal.setRspMsg("The flow is referenced by material flows, can not be deleted");
                return retVal;
            }
        }
        repoDepositAccount.deleteById(id);
        return retVal;
    }

    public RestResponseCommand getDepositAccountDetail(Long id) throws Exception {
        RestResponseCommand rstVal = new RestResponseCommand();
        EntityDepositAccountSetting producer = repoDepositAccount.getById(id);
        String pdsHandle = null;

        try {
            pdsHandle = rosettaWebService.login(producer.getDepositUserInstitute(), producer.getDepositUserName(), producer.getDepositUserPassword());
            if (StringUtils.isEmpty(pdsHandle)) {
                producer.setAuditRst(Boolean.FALSE);
                producer.setAuditMsg("The essential message is not correct");
            }
        } catch (Exception e) {
            producer.setAuditRst(Boolean.FALSE);
            producer.setAuditMsg("The essential message is not correct:" + e.getMessage());
        } finally {
            if (!StringUtils.isEmpty(pdsHandle)) {
                rosettaWebService.logout(pdsHandle);
            }
        }

        rstVal.setRspBody(producer);
        return rstVal;
    }

    public RestResponseCommand refreshDepositAccountSetting(Long id) {
        RestResponseCommand retVal = new RestResponseCommand();

        EntityDepositAccountSetting depositAccountSetting = repoDepositAccount.getById(id);
        if (depositAccountSetting == null) {
            retVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            retVal.setRspMsg("Unknown deposit account, id=" + id);
            return retVal;
        }

        try {
            List<DtoProducersRsp.Producer> producers = rosettaWebService.getProducers(depositAccountSetting);
            depositAccountSetting.setProducers(producers);
            repoDepositAccount.save(depositAccountSetting);
            retVal.setRspBody(depositAccountSetting);
        } catch (Exception e) {
            retVal.setRspCode(RestResponseCommand.RSP_SYSTEM_ERROR);
            retVal.setRspMsg(e.getMessage());
            return retVal;
        }

        return retVal;
    }
}
