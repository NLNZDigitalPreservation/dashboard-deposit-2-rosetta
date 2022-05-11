package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.core.RosettaWebServiceImpl;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.*;
import nz.govt.natlib.dashboard.domain.service.DepositJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ScheduleProcessor {
    protected RosettaWebServiceImpl rosettaWebService;
    protected RepoFlowSetting repoFlowSetting;
    protected RepoDepositJob repoDepositJob;
    protected DepositJobService depositJobService;
    protected RepoDepositAccount repoDepositAccount;

    protected static final Logger log = LoggerFactory.getLogger(ScheduleProcessor.class);

    abstract public void handle(EntityFlowSetting flowSetting) throws Exception;

    public RosettaWebServiceImpl getRosettaWebService() {
        return rosettaWebService;
    }

    public void setRosettaWebService(RosettaWebServiceImpl rosettaWebService) {
        this.rosettaWebService = rosettaWebService;
    }

    public RepoFlowSetting getRepoFlowSetting() {
        return repoFlowSetting;
    }

    public void setRepoFlowSetting(RepoFlowSetting repoFlowSetting) {
        this.repoFlowSetting = repoFlowSetting;
    }

    public RepoDepositJob getRepoDepositJob() {
        return repoDepositJob;
    }

    public void setRepoDepositJob(RepoDepositJob repoDepositJob) {
        this.repoDepositJob = repoDepositJob;
    }

    public DepositJobService getDepositJobService() {
        return depositJobService;
    }

    public void setDepositJobService(DepositJobService depositJobService) {
        this.depositJobService = depositJobService;
    }

    public RepoDepositAccount getRepoDepositAccount() {
        return repoDepositAccount;
    }

    public void setRepoDepositAccount(RepoDepositAccount repoDepositAccount) {
        this.repoDepositAccount = repoDepositAccount;
    }
}
