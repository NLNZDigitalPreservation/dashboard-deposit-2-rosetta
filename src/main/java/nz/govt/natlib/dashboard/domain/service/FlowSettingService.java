package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.common.exception.InvalidParameterException;
import nz.govt.natlib.dashboard.common.exception.NullParameterException;
import nz.govt.natlib.dashboard.common.exception.WebServiceException;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositAccount;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositJob;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.exceptions.NotFoundException;
import nz.govt.natlib.dashboard.exceptions.SystemErrorException;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class FlowSettingService {
    private static final Logger log = LoggerFactory.getLogger(FlowSettingService.class);
    @Autowired
    private RosettaWebService rosettaWebService;
    @Autowired
    private RepoFlowSetting repoFlowSetting;
    @Autowired
    private RepoDepositJob repoDepositJob;
    @Autowired
    private RepoDepositAccount repoDepositAccount;

    public void validateFlowSetting(EntityFlowSetting flowSetting) throws NullParameterException, WebServiceException, InvalidParameterException {
        DashboardHelper.assertNotNull("FlowSettingDTO", flowSetting);
        DashboardHelper.assertNotNull("Enabled", flowSetting.isEnabled());
        DashboardHelper.assertNotNull("ProducerId", flowSetting.getProducerId());
        DashboardHelper.assertNotNull("RootPath", flowSetting.getRootPath());
        DashboardHelper.assertNotNull("ProducerId", flowSetting.getProducerId());
        DashboardHelper.assertNotNull("MaterialFlowId", flowSetting.getMaterialFlowId());
        DashboardHelper.assertNotNull("Stream Location", flowSetting.getStreamLocation());
        DashboardHelper.assertNotNull("Ingestion Completed File Name", flowSetting.getInjectionCompleteFileName());
        DashboardHelper.assertNotNull("MaxActiveDays", flowSetting.getMaxActiveDays());
        DashboardHelper.assertNotNull("MaxStorageDays", flowSetting.getMaxSaveDays());
        DashboardHelper.assertNotNull("ActualContentBackupOptions", flowSetting.getActualContentBackupOptions());

        File rootPath = new File(flowSetting.getRootPath());
        if (!rootPath.exists() || !rootPath.isDirectory()) {
            throw new InvalidParameterException("Invalid RootPath: the Root Path does not exist.");
        }

        EntityDepositAccountSetting depositAccount = repoDepositAccount.getById(flowSetting.getDepositAccountId());
        if (depositAccount == null) {
            throw new InvalidParameterException("The Deposit Account does not exist, depositAccountId:" + flowSetting.getDepositAccountId());
        }

        //        With the LDAP authentication mechanism, will not authenticate the account in Rosetta
        //        String sessionId;
        //        try {
        //            sessionId = rosettaWebService.login(depositAccount.getDepositUserInstitute(), depositAccount.getDepositUserName(), depositAccount.getDepositUserPassword());
        //        } catch (Exception e) {
        //            throw new WebServiceException(e);
        //        }
        //        if (DashboardHelper.isEmpty(sessionId)) {
        //            throw new WebServiceException("Could not access platform with given institution, username and password.");
        //        }

        //Verify ProduceId and MaterialFlowId
        try {
            if (!rosettaWebService.isValidProducer(depositAccount, flowSetting.getProducerId())) {
                throw new InvalidParameterException("Invalid producerId");
            }

            if (!rosettaWebService.isValidMaterialFlow(depositAccount, flowSetting.getProducerId(), flowSetting.getMaterialFlowId())) {
                throw new InvalidParameterException("Invalid materialFlowId");
            }
        } catch (Exception e) {
            throw new WebServiceException(e);
        }

        List<EntityFlowSetting> flowSettings = repoFlowSetting.getAll();
        for (EntityFlowSetting flow : flowSettings) {
            if (flow.getId().equals(flowSetting.getId())) {
                continue;
            }

            if (StringUtils.equals(flow.getRootPath(), flowSetting.getRootPath())) {
                throw new InvalidParameterException("Duplicate RootPath");
            }

            if (StringUtils.equals(flow.getMaterialFlowId(), flowSetting.getMaterialFlowId())) {
                throw new InvalidParameterException("Duplicate MaterialFlowId");
            }
        }
        flowSettings.clear();

        if (!StringUtils.equalsIgnoreCase(flowSetting.getActualContentBackupOptions(), "notBackup")) {
            if (StringUtils.isEmpty(flowSetting.getBackupPath())) {
                throw new InvalidParameterException("The backup path is empty.");
            }
            if (StringUtils.isEmpty(flowSetting.getBackupSubFolders())) {
                throw new InvalidParameterException("The sub folders is empty.");
            }
        }
    }


    public EntityFlowSetting saveFlowSetting(EntityFlowSetting flowSetting) throws InvalidParameterException, WebServiceException, NullParameterException {
        //Validating input parameters
        this.validateFlowSetting(flowSetting);
        repoFlowSetting.save(flowSetting);
        return flowSetting;
    }

    public EntityFlowSetting deleteFlowSetting(Long id) throws NotFoundException, SystemErrorException {
        EntityFlowSetting flowSetting = repoFlowSetting.getById(id);
        if (flowSetting == null) {
            throw new NotFoundException("Not able to find material flow: " + id);
        }

        List<EntityDepositJob> jobs = repoDepositJob.getByFlowId(id);
        if (!jobs.isEmpty()) {
            throw new SystemErrorException("The flow is referenced by deposit jobs, can not be deleted: " + RestResponseCommand.RSP_USER_OTHER_ERROR);
        }

        repoFlowSetting.deleteById(id);

        return flowSetting;
    }
}
