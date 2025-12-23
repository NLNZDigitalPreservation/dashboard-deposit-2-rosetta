package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.common.core.dto.DtoProducersRsp;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositAccount;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.exceptions.BadRequestException;
import nz.govt.natlib.dashboard.exceptions.SystemErrorException;
import nz.govt.natlib.dashboard.util.DashboardHelper;
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

    public List<EntityDepositAccountSetting> getAllDepositAccountSettings() {
        return repoDepositAccount.getAll();
    }

    public void saveDepositAccountSetting(EntityDepositAccountSetting account) throws Exception {
        //Validate the account
        DashboardHelper.assertNotNull("Producer", account);
        DashboardHelper.assertNotNull("DepositUserInstitute", account.getDepositUserInstitute());
        DashboardHelper.assertNotNull("DepositUserName", account.getDepositUserName());
        DashboardHelper.assertNotNull("DepositUserPassword", account.getDepositUserPassword());

        //        With the LDAP authentication mechanism, will not authenticate the account in Rosetta
        //        String sessionId = rosettaWebService.login(account.getDepositUserInstitute(), account.getDepositUserName(), account.getDepositUserPassword());
        //        if (StringUtils.isEmpty(sessionId)) {
        //            String err_msg = "Invalid deposit username or password";
        //            log.error(err_msg);
        //            throw new BadRequestException(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE, err_msg);
        //        }
        repoDepositAccount.save(account);
    }

    public void deleteDepositAccountSetting(Long id) {
        List<EntityFlowSetting> flowSettings = repoFlowSetting.getAll();
        for (EntityFlowSetting flowSetting : flowSettings) {
            if (flowSetting.getDepositAccountId() == id.longValue()) {
                throw new BadRequestException(RestResponseCommand.RSP_USER_OTHER_ERROR, "The flow is referenced by material flows, can not be deleted");
            }
        }
        repoDepositAccount.deleteById(id);
    }

    //        With the LDAP authentication mechanism, will not authenticate the account in Rosetta
    //    public EntityDepositAccountSetting getDepositAccountDetail(Long id) throws Exception {
    //        EntityDepositAccountSetting account = repoDepositAccount.getById(id);
    //        String sessionId = null;
    //
    //        try {
    //            sessionId = rosettaWebService.login(account.getDepositUserInstitute(), account.getDepositUserName(), account.getDepositUserPassword());
    //            if (StringUtils.isEmpty(sessionId)) {
    //                account.setAuditRst(Boolean.FALSE);
    //                account.setAuditMsg("The credential message is not correct");
    //            }
    //        } catch (Exception e) {
    //            account.setAuditRst(Boolean.FALSE);
    //            account.setAuditMsg("The credential message is not correct:" + e.getMessage());
    //        } finally {
    //            if (!StringUtils.isEmpty(sessionId)) {
    //                rosettaWebService.logout(sessionId);
    //            }
    //        }
    //        return account;
    //    }

    public void refreshDepositAccountSetting(Long id) {
        RestResponseCommand retVal = new RestResponseCommand();

        EntityDepositAccountSetting depositAccountSetting = repoDepositAccount.getById(id);
        if (depositAccountSetting == null) {
            throw new BadRequestException(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS, "Unknown deposit account, id=" + id);
        }

        try {
            List<DtoProducersRsp.Producer> producers = rosettaWebService.getProducers(depositAccountSetting);
            depositAccountSetting.setProducers(producers);
            repoDepositAccount.save(depositAccountSetting);
            retVal.setRspBody(depositAccountSetting);
        } catch (Exception e) {
            throw new SystemErrorException(RestResponseCommand.RSP_SYSTEM_ERROR, e.getMessage());
        }
    }
}
