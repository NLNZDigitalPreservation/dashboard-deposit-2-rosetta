package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.core.RosettaWebServiceImpl;
import nz.govt.natlib.dashboard.common.injection.InjectionPathScan;
import nz.govt.natlib.dashboard.common.injection.InjectionUtils;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.*;
import nz.govt.natlib.dashboard.domain.service.DepositJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class ScheduleProcessorBasic {
    protected RosettaWebServiceImpl rosettaWebService;
    protected RepoFlowSetting repoFlowSetting;
    protected RepoDepositJob repoDepositJob;
    protected DepositJobService depositJobService;
    protected RepoDepositAccount repoDepositAccount;
    protected EntityFlowSetting flowSetting;
    protected InjectionPathScan injectionPathScanClient;

    protected static final Logger log = LoggerFactory.getLogger(ScheduleProcessorBasic.class);

    public ScheduleProcessorBasic(EntityFlowSetting flowSetting) {
        this.flowSetting = flowSetting;
        this.injectionPathScanClient = InjectionUtils.createPathScanClient(flowSetting.getRootPath());
    }

    public void handle() throws Exception {
        if (!flowSetting.isEnabled()) {
            log.warn("Disabled Material Flow: {} {}", flowSetting.getId(), flowSetting.getMaterialFlowId());
            return;
        }

        //To initial jobs
        handleIngest();

        List<EntityDepositJob> listOfJobs = repoDepositJob.getByFlowId(flowSetting.getId());
        long countRunning = listOfJobs.stream().filter(job -> job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.RUNNING).count();
        //Get the current max concurrency limitation of running jobs according to the schedule.
        LocalDateTime now = LocalDateTime.now();
        int nowDay = now.getDayOfWeek().ordinal();
        int maxConcurrencyJobs = flowSetting.getWeeklyMaxConcurrency()[nowDay];
        log.debug("Now: {}, day: {}, maxConcurrencyJobs: {}, countRunning: {}", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), nowDay, maxConcurrencyJobs, countRunning);

        for (EntityDepositJob job : listOfJobs) {
            //Only launch the deposit task when Rosetta is idle
            if (countRunning < maxConcurrencyJobs) {
                if (handleDeposit(job)) {
                    countRunning++; //To be sure Rosetta not over load.
                }
            }

            handlePollingStatus(job);

            handleFinalize(job);
        }

        handleHistoryPruning();

        injectionPathScanClient.disconnect();
    }

    abstract public void handleIngest();

    abstract public boolean handleDeposit(EntityDepositJob job);

    abstract public void handlePollingStatus(EntityDepositJob job);

    abstract public void handleFinalize(EntityDepositJob job) throws IOException;

    abstract public void handleHistoryPruning() throws IOException;

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

    public EntityFlowSetting getFlowSetting() {
        return flowSetting;
    }

    public void setFlowSetting(EntityFlowSetting flowSetting) {
        this.flowSetting = flowSetting;
    }
}
