package nz.govt.natlib.dashboard.domain.daemon;

import com.exlibris.dps.SipStatusInfo;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleProcessorImplJobStatusPolling extends ScheduleProcessor {
    @Override
    public void handle(EntityFlowSetting flowSetting) throws Exception {
        if (rosettaWebService == null) {
            log.info("RosettaWebService is initialing.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int nowDay = now.getDayOfWeek().ordinal();
        int maxConcurrencyJobs = flowSetting.getWeeklyMaxConcurrency()[nowDay];
        log.debug("Now: {}, day: {}, maxConcurrencyJobs: {}", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), nowDay, maxConcurrencyJobs);

        List<EntityDepositJob> listOfRunningJobs = repoDepositJob.getAll();
        for (EntityDepositJob job : listOfRunningJobs) {
            if (job.getStage() != EnumDepositJobStage.DEPOSIT || job.getState() != EnumDepositJobState.RUNNING) {
                log.debug("Skip polling. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
                continue;
            }
            SipStatusInfo sipStatusInfo;
            try {
                log.info("Update polling, before. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
                sipStatusInfo = rosettaWebService.getSIPStatusInfo(job.getSipID());
                log.info("Update polling. jobId: {}, jobName: {}, SIPStatusInfo: {}, {}, {}", job.getId(), job.getInjectionTitle(), sipStatusInfo.getModule(), sipStatusInfo.getStage(), sipStatusInfo.getStatus());
                job = depositJobService.jobUpdateStatus(job, sipStatusInfo);
                if (job.getState() == EnumDepositJobState.FAILED || job.getState() == EnumDepositJobState.SUCCEED) {
                    depositJobService.jobDepositFinished(job);
                }
                log.info("Update polling, after. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
            } catch (Exception e) {
                log.error("Failed to scan deposit job status", e);
            }
        }
    }
}