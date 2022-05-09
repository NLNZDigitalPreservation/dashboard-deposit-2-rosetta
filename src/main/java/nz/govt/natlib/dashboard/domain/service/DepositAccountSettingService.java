package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositAccount;
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

    public RestResponseCommand getAllProducerSettings() {
        RestResponseCommand rstVal = new RestResponseCommand();
        List<EntityDepositAccountSetting> data = repoDepositAccount.getAll();
        rstVal.setRspBody(data);
        return rstVal;
    }

    public RestResponseCommand saveProducerSetting(EntityDepositAccountSetting producer) throws Exception {
        //Validate the producer
        DashboardHelper.assertNotNull("Producer", producer);
        DashboardHelper.assertNotNull("DepositUserInstitute", producer.getDepositUserInstitute());
        DashboardHelper.assertNotNull("DepositUserName", producer.getDepositUserName());
        DashboardHelper.assertNotNull("DepositUserPassword", producer.getDepositUserPassword());
//        rosettaWebService.login(producer.getDepositUserInstitute(), producer.getDepositUserName(), producer.getDepositUserPassword());

        RestResponseCommand rstVal = new RestResponseCommand();
        if (rosettaWebService.login(producer.getDepositUserInstitute(), producer.getDepositUserName(), producer.getDepositUserPassword()) == null) {
            String err_msg = "Invalid deposit username or password";
            log.error(err_msg);
            rstVal.setRspCode(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            rstVal.setRspMsg(err_msg);
            return rstVal;
        }

        repoDepositAccount.save(producer);

        return rstVal;
    }

    public RestResponseCommand deleteProducerSetting(Long id) {
        RestResponseCommand rstVal = new RestResponseCommand();
        repoDepositAccount.deleteById(id);
        return rstVal;
    }

    public RestResponseCommand getProducerDetail(Long id) {
        RestResponseCommand rstVal = new RestResponseCommand();
        rstVal.setRspBody(repoDepositAccount.getById(id));
        return rstVal;
    }
}
