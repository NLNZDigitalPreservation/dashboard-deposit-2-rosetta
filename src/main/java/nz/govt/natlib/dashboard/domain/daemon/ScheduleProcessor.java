package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.*;
import nz.govt.natlib.dashboard.domain.service.DepositJobService;
import nz.govt.natlib.dashboard.domain.service.GlobalSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ScheduleProcessor {
    protected RosettaWebService rosettaWebService;
    protected RepoFlowSetting repoFlowSetting;
    protected RepoStorageLocation repoStorageLocation;
    protected RepoDepositJobActive repoDepositJobActive;
    protected RepoDepositJobHistory repoDepositJobHistory;
    protected DepositJobService depositJobService;
    protected GlobalSettingService globalSettingService;

    protected static final Logger log = LoggerFactory.getLogger(ScheduleProcessor.class);

    abstract public void handle(EntityFlowSetting flowSetting) throws Exception;

    public RosettaWebService getRosettaWebService() {
        return rosettaWebService;
    }

    public void setRosettaWebService(RosettaWebService rosettaWebService) {
        this.rosettaWebService = rosettaWebService;
    }

    public RepoFlowSetting getRepoFlowSetting() {
        return repoFlowSetting;
    }

    public void setRepoFlowSetting(RepoFlowSetting repoFlowSetting) {
        this.repoFlowSetting = repoFlowSetting;
    }

    public RepoStorageLocation getRepoFTPSetting() {
        return repoStorageLocation;
    }

    public void setRepoFTPSetting(RepoStorageLocation repoStorageLocation) {
        this.repoStorageLocation = repoStorageLocation;
    }

    public RepoDepositJobActive getRepoDepositJobActive() {
        return repoDepositJobActive;
    }

    public void setRepoDepositJobActive(RepoDepositJobActive repoDepositJobActive) {
        this.repoDepositJobActive = repoDepositJobActive;
    }

    public RepoDepositJobHistory getRepoDepositJobHistory() {
        return repoDepositJobHistory;
    }

    public void setRepoDepositJobHistory(RepoDepositJobHistory repoDepositJobHistory) {
        this.repoDepositJobHistory = repoDepositJobHistory;
    }

    public DepositJobService getDepositJobService() {
        return depositJobService;
    }

    public void setDepositJobService(DepositJobService depositJobService) {
        this.depositJobService = depositJobService;
    }

    public RepoStorageLocation getRepoStorageLocation() {
        return repoStorageLocation;
    }

    public void setRepoStorageLocation(RepoStorageLocation repoStorageLocation) {
        this.repoStorageLocation = repoStorageLocation;
    }

    public GlobalSettingService getGlobalSettingService() {
        return globalSettingService;
    }

    public void setGlobalSettingService(GlobalSettingService globalSettingService) {
        this.globalSettingService = globalSettingService;
    }
}
