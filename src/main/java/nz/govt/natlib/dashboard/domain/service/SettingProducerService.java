package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.domain.entity.EntityProducer;
import nz.govt.natlib.dashboard.domain.repo.RepoProducer;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingProducerService {
    private static final Logger log = LoggerFactory.getLogger(SettingProducerService.class);
    @Autowired
    private RosettaWebService rosettaWebService;
    @Autowired
    private RepoProducer repoProducer;

    RestResponseCommand getAllConfiguredProducers() {
        RestResponseCommand rstVal = new RestResponseCommand();
        List<EntityProducer> data = repoProducer.getAll();
        rstVal.setRspBody(data);
        return rstVal;
    }

    RestResponseCommand saveProducer(EntityProducer producer) throws Exception {
        //Validate the producer
        DashboardHelper.assertNotNull("Producer", producer);
        DashboardHelper.assertNotNull("ProducerId", producer.getProducerId());
        DashboardHelper.assertNotNull("ProducerName", producer.getProducerName());
        DashboardHelper.assertNotNull("DepositUserInstitute", producer.getDepositUserInstitute());
        DashboardHelper.assertNotNull("DepositUserName", producer.getDepositUserName());
        DashboardHelper.assertNotNull("DepositUserPassword", producer.getDepositUserPassword());
//        DashboardHelper.assertNotNull("ProducerUserConfirmedPassword", producer.getDepositUserPasswordConfirm());
//        rosettaWebService.login(producer.getDepositUserInstitute(), producer.getDepositUserName(), producer.getDepositUserPassword());

        RestResponseCommand rstVal = new RestResponseCommand();
        if (!rosettaWebService.isValidProducer(producer.getDepositUserName(), producer.getProducerId())) {
            String err_msg = String.format("The deposit user [%s] is not accessible for producer [%s | %s]", producer.getDepositUserName(), producer.getProducerId(), producer.getProducerName());
            log.error(err_msg);
            rstVal.setRspCode(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            rstVal.setRspMsg(err_msg);
            return rstVal;
        }

        if (rosettaWebService.login(producer.getDepositUserInstitute(), producer.getDepositUserName(), producer.getDepositUserPassword()) == null) {
            String err_msg = "Invalid deposit username or password";
            log.error(err_msg);
            rstVal.setRspCode(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            rstVal.setRspMsg(err_msg);
            return rstVal;
        }

        repoProducer.save(producer);

        return rstVal;
    }

    RestResponseCommand deleteProducerSetting(Long id) {
        RestResponseCommand rstVal = new RestResponseCommand();
        repoProducer.deleteById(id);
        return rstVal;
    }

    RestResponseCommand getProducerDetail(Long id) {
        RestResponseCommand rstVal = new RestResponseCommand();
        rstVal.setRspBody(repoProducer.getById(id));
        return rstVal;
    }
}
