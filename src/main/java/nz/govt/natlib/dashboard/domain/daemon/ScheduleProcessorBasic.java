package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.core.RosettaWebServiceImpl;
import nz.govt.natlib.dashboard.common.injection.InjectionPathScan;
import nz.govt.natlib.dashboard.common.injection.InjectionUtils;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import nz.govt.natlib.dashboard.domain.repo.*;
import nz.govt.natlib.dashboard.domain.service.DepositJobService;
import nz.govt.natlib.dashboard.domain.service.FlowSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ScheduleProcessorBasic {
    @Autowired
    protected RosettaWebServiceImpl rosettaWebService;
    @Autowired
    protected DepositJobService depositJobService;
    @Autowired
    protected RepoFlowSetting repoFlowSetting;
    @Autowired
    protected RepoDepositAccount repoDepositAccount;
    @Autowired
    protected RepoDepositJob repoDepositJob;
    @Autowired
    protected FlowSettingService flowSettingService;
    @Autowired
    protected RepoGlobalSetting repoGlobalSetting;

    protected static final Logger log = LoggerFactory.getLogger(ScheduleProcessorBasic.class);

    public void handle() throws Exception {
        log.debug("On timer heartbeat.");
        EntityGlobalSetting globalSetting = repoGlobalSetting.getGlobalSetting();
        if (globalSetting != null && globalSetting.isPaused()) {
            LocalDateTime ldtPausedStartTime = LocalDateTime.parse(globalSetting.getPausedStartTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime ldtPausedEndTime = LocalDateTime.parse(globalSetting.getPausedEndTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime ldtNowDatetime = LocalDateTime.now();
            if (ldtNowDatetime.isAfter(ldtPausedStartTime) && ldtNowDatetime.isBefore(ldtPausedEndTime)) {
                log.debug("Skip the paused timeslot.");
                return;
            }
        }

        //To initial jobs
        handleIngest();

        List<EntityDepositJob> allJobs = repoDepositJob.getAll();
        if (allJobs == null) {
            return;
        }
        Map<Long, List<EntityDepositJob>> allJobGroups = allJobs.stream().collect(Collectors.groupingBy(job -> job.getAppliedFlowSetting().getId()));
        for (Long flowSettingId : allJobGroups.keySet()) {
            List<EntityDepositJob> jobs = allJobGroups.get(flowSettingId);
            EntityFlowSetting flowSetting = repoFlowSetting.getById(flowSettingId);
            if (flowSetting != null) {
                if (!flowSetting.isEnabled()) {
                    log.warn("Disabled Material Flow: {} {}", flowSetting.getId(), flowSetting.getMaterialFlowId());
                    continue;
                }
                InjectionPathScan injectionPathScanClient = InjectionUtils.createPathScanClient(flowSetting.getRootPath());

                long countRunning = jobs.stream().filter(job -> job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.RUNNING).count();
                //Get the current max concurrency limitation of running jobs according to the schedule.
                LocalDateTime now = LocalDateTime.now();
                int nowDay = now.getDayOfWeek().ordinal();
                int maxConcurrencyJobs = flowSetting.getWeeklyMaxConcurrency()[nowDay];
                log.debug("Now: {}, day: {}, maxConcurrencyJobs: {}, countRunning: {}", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), nowDay, maxConcurrencyJobs, countRunning);

                for (EntityDepositJob job : jobs) {
                    if (job.getState() == EnumDepositJobState.PAUSED) {
                        log.debug("Skip the Paused the job: {}", job.getId());
                        continue;
                    }

                    //Only launch the deposit task when Rosetta is idle
                    if (countRunning < maxConcurrencyJobs) {
                        if (handleDeposit(flowSetting, injectionPathScanClient, job)) {
                            countRunning++; //To be sure Rosetta not over load.
                        }
                    }

                    handlePollingStatus(job);

                    handleFinalize(flowSetting, injectionPathScanClient, job);

                    handleHistoryPruning(flowSetting, injectionPathScanClient, job);
                }
                injectionPathScanClient.disconnect();

            } else {
                for (EntityDepositJob job : jobs) {
                    handleFlowSettingMissingJob(job);
                }
            }
            jobs.clear();
        }
        allJobGroups.forEach((k, v) -> v.clear());
        allJobGroups.clear();
        allJobs.clear();
    }

    abstract public void handleIngest();

    abstract public boolean handleDeposit(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job);

    abstract public void handlePollingStatus(EntityDepositJob job);

    abstract public void handleFinalize(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) throws IOException;

    abstract public void handleHistoryPruning(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) throws IOException;

    abstract public void handleFlowSettingMissingJob(EntityDepositJob job) throws IOException;

    public void setRosettaWebService(RosettaWebServiceImpl rosettaWebService) {
        this.rosettaWebService = rosettaWebService;
    }

    public void setDepositJobService(DepositJobService depositJobService) {
        this.depositJobService = depositJobService;
    }

    public void setRepoFlowSetting(RepoFlowSetting repoFlowSetting) {
        this.repoFlowSetting = repoFlowSetting;
    }

    public void setRepoDepositAccount(RepoDepositAccount repoDepositAccount) {
        this.repoDepositAccount = repoDepositAccount;
    }

    public void setRepoDepositJob(RepoDepositJob repoDepositJob) {
        this.repoDepositJob = repoDepositJob;
    }

    public void setFlowSettingService(FlowSettingService flowSettingService) {
        this.flowSettingService = flowSettingService;
    }

    public void setRepoGlobalSetting(RepoGlobalSetting repoGlobalSetting) {
        this.repoGlobalSetting = repoGlobalSetting;
    }
}