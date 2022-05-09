package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.common.exception.InvalidParameterException;
import nz.govt.natlib.dashboard.common.exception.NullParameterException;
import nz.govt.natlib.dashboard.common.exception.WebServiceException;
import nz.govt.natlib.dashboard.common.injection.UnionFile;
import nz.govt.natlib.dashboard.common.injection.InjectionPathScanFTP;
import nz.govt.natlib.dashboard.common.injection.InjectionUtils;
import nz.govt.natlib.dashboard.domain.daemon.TimerScheduledExecutors;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityStorageLocation;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositJob;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.ui.command.RawMaterialFlowCommand;
import nz.govt.natlib.dashboard.ui.command.RawProducerCommand;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import nz.govt.natlib.ndha.common.exlibris.MaterialFlow;
import nz.govt.natlib.ndha.common.exlibris.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private TimerScheduledExecutors timerScheduledExecutors;
    @Autowired
    private GlobalSettingService globalSettingService;

    public void validateStorage(EntityStorageLocation storage) throws NullParameterException, InvalidParameterException, WebServiceException {
        DashboardHelper.assertNotNull("ScanMode", storage.getScanMode());
        DashboardHelper.assertNotNull("RootPath", storage.getRootPath());
        if (storage.getScanMode().equalsIgnoreCase(InjectionUtils.SCAN_MODE_FTP) || storage.getScanMode().equalsIgnoreCase(InjectionUtils.SCAN_MODE_SFTP)) {
            DashboardHelper.assertNotNull("FTP Server", storage.getFtpServer());
            DashboardHelper.assertNotNull("FTP Username", storage.getFtpUsername());
            DashboardHelper.assertNotNull("FTP Password", storage.getFtpPassword());
            DashboardHelper.assertNotNull("FTP Proxy Enabled", storage.isFtpProxyEnabled());
            if (storage.isFtpProxyEnabled()) {
                DashboardHelper.assertNotNull("FTP Proxy Host", storage.getFtpProxyHost());
                DashboardHelper.assertNotNull("FTP Proxy Port", storage.getFtpProxyPort());
                DashboardHelper.assertNotNull("FTP Proxy Username", storage.getFtpProxyHost());
                DashboardHelper.assertNotNull("FTP Proxy Password", storage.getFtpProxyPort());
            }
        }

        //Verify RootPath and FTP Configuration
        if (storage.getScanMode().equalsIgnoreCase(InjectionUtils.SCAN_MODE_NFS)) {
            File rootPath = new File(storage.getRootPath());
            if (!rootPath.exists() || !rootPath.isDirectory()) {
                throw new InvalidParameterException("Invalid RootPath.");
            }
        } else if (storage.getScanMode().equalsIgnoreCase(InjectionUtils.SCAN_MODE_FTP)) {
            InjectionPathScanFTP ftpClient = new InjectionPathScanFTP(storage.getRootPath());
            try {
                ftpClient.init(storage);
            } catch (IOException e) {
                throw new WebServiceException(e);
            }
            List<UnionFile> rootDirs = ftpClient.listRootDir();
            if (rootDirs == null) {
                throw new InvalidParameterException("Invalid RootPath.");
            }
        }
    }

    public void validateFlowSetting(EntityFlowSetting flowSetting) throws InvalidParameterException, WebServiceException, NullParameterException {
        validateFlowSetting(flowSetting, flowSetting.getInjectionEndPoint(), flowSetting.getBackupEndPoint());
    }

    public void validateFlowSetting(EntityFlowSetting flowSetting, EntityStorageLocation storageInjection, EntityStorageLocation storageBackup) throws NullParameterException, WebServiceException, InvalidParameterException {
        DashboardHelper.assertNotNull("FlowSettingDTO", flowSetting);
        DashboardHelper.assertNotNull("Enabled", flowSetting.isEnabled());
        DashboardHelper.assertNotNull("Name", flowSetting.getName());
        if (!flowSetting.isEnabled()) {
            log.info("Material Flow {} is disabled", flowSetting.getName());
            return;
        }

        DashboardHelper.assertNotNull("ProducerId", flowSetting.getProducerId());
        DashboardHelper.assertNotNull("MaterialFlowId", flowSetting.getMaterialFlowId());
        DashboardHelper.assertNotNull("Delays", flowSetting.getDelays());
        DashboardHelper.assertNotNull("Stream Location", flowSetting.getStreamLocation());
        DashboardHelper.assertNotNull("Injection Completed File Name", flowSetting.getInjectionCompleteFileName());
        DashboardHelper.assertNotNull("DelayUnit", flowSetting.getDelayUnit());
        DashboardHelper.assertNotNull("MaxActiveDays", flowSetting.getMaxActiveDays());
        DashboardHelper.assertNotNull("MaxStorageDays", flowSetting.getMaxSaveDays());

        DashboardHelper.assertNotNull("Injection Storage", storageInjection);
        if (flowSetting.isBackupEnabled()) {
            DashboardHelper.assertNotNull("Backup Storage", storageBackup);
        }

        validateStorage(storageInjection);
        if (flowSetting.isBackupEnabled()) {
            validateStorage(storageBackup);
        }

        String pdsHandle;
        try {
            pdsHandle = rosettaWebService.login(globalSettingService.getDepositUserInstitute(), globalSettingService.getDepositUserName(), globalSettingService.getDepositUserPassword());
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
        if (DashboardHelper.isNull(pdsHandle)) {
            throw new WebServiceException("Could not access platform with given institution, username and password.");
        }

        //Verify ProduceId and MaterialFlowId
        try {
            if (!rosettaWebService.isValidProducer(globalSettingService.getDepositUserName(), flowSetting.getProducerId())) {
                throw new InvalidParameterException("Invalid producerId");
            }

            if (!rosettaWebService.isValidMaterialFlow(flowSetting.getProducerId(), flowSetting.getMaterialFlowId())) {
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

            if (StringUtils.equals(flow.getName(), flowSetting.getName())) {
                throw new InvalidParameterException("Duplicate name");
            }

            if (StringUtils.equals(flow.getMaterialFlowId(), flowSetting.getMaterialFlowId())) {
                throw new InvalidParameterException("Duplicate MaterialFlowId");
            }
        }
    }

    public RestResponseCommand getAllMaterialFlowDigests() {
        RestResponseCommand rstVal = new RestResponseCommand();
//        EntityGlobalSetting globalSetting = globalSettingService.getGlobalSettingInstance();
//        if (DashboardHelper.isNull(globalSetting)) {
//            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
//            rstVal.setRspMsg("Please initial deposit dashboard before using it");
//            return rstVal;
//        }
//
//        try {
//            final List<RawProducerCommand> allFlows = new ArrayList<>();
//            List<Producer> producers = this.rosettaWebService.getProducers(globalSetting.getDepositUserName());
//            for (Producer producer : producers) {
//                RawProducerCommand producerCommand = new RawProducerCommand();
//                producerCommand.setProducerId(producer.getID());
//                producerCommand.setProducerName(producer.getDescription());
//                List<MaterialFlow> materialFlows = this.rosettaWebService.getMaterialFlows(producer.getID());
//                materialFlows.forEach(materialFlow -> {
//                    RawMaterialFlowCommand materialFlowCommand = new RawMaterialFlowCommand();
//                    materialFlowCommand.setId(materialFlow.getID());
//                    materialFlowCommand.setName(materialFlow.getDescription());
//                    materialFlowCommand.setProducerId(producer.getID());
//                    producerCommand.getMaterialFlows().add(materialFlowCommand);
//                });
//                allFlows.add(producerCommand);
//            }
//            rstVal.setRspBody(allFlows);
//        } catch (Exception e) {
//            log.error("Failed to get MaterialFlows", e);
//            rstVal.setRspCode(RestResponseCommand.RSP_DEPOSIT_QUERY_ERROR);
//            rstVal.setRspMsg(e.getMessage());
//        }
        return rstVal;
    }

    public RestResponseCommand getProducers(String userName) throws Exception {
        RestResponseCommand restResponseCommand = new RestResponseCommand();
        try {
            List<Producer> producers = this.rosettaWebService.getProducers(userName);
            restResponseCommand.setRspBody(producers);
        } catch (Exception e) {
            log.error("Failed to get producers", e);
            restResponseCommand.setRspCode(RestResponseCommand.RSP_DEPOSIT_QUERY_ERROR);
            restResponseCommand.setRspMsg(e.getMessage());
        }
        return restResponseCommand;
    }

    public RestResponseCommand getMaterialFlows(String producerID) throws Exception {
        RestResponseCommand restResponseCommand = new RestResponseCommand();
        try {
            List<MaterialFlow> materialFlows = this.rosettaWebService.getMaterialFlows(producerID);
            restResponseCommand.setRspBody(materialFlows);
        } catch (Exception e) {
            log.error("Failed to get material flows", e);
            restResponseCommand.setRspCode(RestResponseCommand.RSP_DEPOSIT_QUERY_ERROR);
            restResponseCommand.setRspMsg(e.getMessage());
        }
        return restResponseCommand;
    }

    public RestResponseCommand getAllFlowSettings() {
        RestResponseCommand retVal = new RestResponseCommand();

//        List<FlowSettingCommand> rspFlowSettings = repoFlowSetting.getAll().stream().map(flowSetting -> {
//            FlowSettingCommand rspCmd = new FlowSettingCommand();
//            rspCmd.setFlowSetting(flowSetting);
//
//            if (!DashboardHelper.isNull(flowSetting.getInjectionEndPointId())) {
//                rspCmd.setInjectionEndPoint(repoStorageLocation.getById(flowSetting.getInjectionEndPointId()));
//            }
//
//            if (!DashboardHelper.isNull(flowSetting.getBackupEndPointId())) {
//                rspCmd.setBackupEndPoint(repoStorageLocation.getById(flowSetting.getBackupEndPointId()));
//            }
//
//            return rspCmd;
//        }).collect(Collectors.toList());

        retVal.setRspBody(repoFlowSetting.getAll());
        return retVal;
    }

    public RestResponseCommand getFlowSettingDetail(Long id) {
        RestResponseCommand retVal = new RestResponseCommand();

        EntityFlowSetting flowSetting = repoFlowSetting.getById(id);
        if (flowSetting == null) {
            retVal.setRspCode(RestResponseCommand.RSP_USER_QUERY_ERROR);
            retVal.setRspMsg("Could not find flowSetting with id: " + id);
            return retVal;
        }

        retVal.setRspBody(flowSetting);
        return retVal;
    }

    public EntityFlowSetting saveFlowSetting(EntityFlowSetting flowSetting) throws InvalidParameterException, WebServiceException, NullParameterException {
        //Validating input parameters
        EntityStorageLocation injectionEndPoint = flowSetting.getInjectionEndPoint();
        EntityStorageLocation backupEndPoint = flowSetting.getBackupEndPoint();
        this.validateFlowSetting(flowSetting, injectionEndPoint, backupEndPoint);

        flowSetting.setAuditRst(true);
        flowSetting.setAuditMsg("OK");
        repoFlowSetting.save(flowSetting);

        //Rescheduling or adding the existing timer
        timerScheduledExecutors.rescheduleDepositJobPreparing(flowSetting);

        return flowSetting;
    }

    public RestResponseCommand deleteFlowSetting(Long id) {
        RestResponseCommand retVal = getFlowSettingDetail(id);

        List<EntityDepositJob> jobs = repoDepositJob.getByFlowId(id);
        if (jobs.size() > 0) {
            retVal.setRspCode(RestResponseCommand.RSP_USER_OTHER_ERROR);
            retVal.setRspMsg("The flow is referenced by deposit jobs, can not be deleted");
            return retVal;
        }

        EntityFlowSetting flowSetting = repoFlowSetting.getById(id);
        if (flowSetting != null) {
            //Close the relevant timer
            timerScheduledExecutors.closeDepositJobPreparing(flowSetting);
            repoFlowSetting.deleteById(id);
        }

        return retVal;
    }
}
