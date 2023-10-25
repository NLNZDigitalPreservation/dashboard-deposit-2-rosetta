package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaApi;
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
    private RosettaApi rosettaApi;
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
        String pdsHandle = rosettaApi.login(producer.getDepositUserInstitute(), producer.getDepositUserName(), producer.getDepositUserPassword());
        if (StringUtils.isEmpty(pdsHandle)) {
            String err_msg = "Invalid deposit username or password";
            log.error(err_msg);
            rstVal.setRspCode(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            rstVal.setRspMsg(err_msg);
            return rstVal;
        }
        rosettaApi.logout(pdsHandle);

        repoDepositAccount.save(producer);
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
            pdsHandle = rosettaApi.login(producer.getDepositUserInstitute(), producer.getDepositUserName(), producer.getDepositUserPassword());
            if (StringUtils.isEmpty(pdsHandle)) {
                producer.setAuditRst(Boolean.FALSE);
                producer.setAuditMsg("The essential message is not correct");
            }
        } catch (Exception e) {
            producer.setAuditRst(Boolean.FALSE);
            producer.setAuditMsg("The essential message is not correct:" + e.getMessage());
        } finally {
            if (!StringUtils.isEmpty(pdsHandle)) {
                rosettaApi.logout(pdsHandle);
            }
        }

        rstVal.setRspBody(producer);
        return rstVal;
    }
}
