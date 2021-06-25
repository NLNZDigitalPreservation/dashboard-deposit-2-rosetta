package nz.govt.natlib.dashboard.domain.daemon;

import com.exlibris.dps.SipStatusInfo;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;

import java.util.List;

public class ScheduleProcessorImplJobStatusPolling extends ScheduleProcessor {
    @Override
    public void handle(EntityFlowSetting flowSetting) throws Exception {
        List<EntityDepositJob> listOfRunningJobs = repoDepositJob.getAll();
        for (EntityDepositJob job : listOfRunningJobs) {
            if (job.getStage() != EnumDepositJobStage.DEPOSIT || job.getState() != EnumDepositJobState.RUNNING) {
                continue;
            }
            SipStatusInfo sipStatusInfo;
            try {
                sipStatusInfo = rosettaWebService.getSIPStatusInfo(job.getSipID());
                job = depositJobService.jobUpdateStatus(job, sipStatusInfo);
                if (job.getState() == EnumDepositJobState.FAILED || job.getState() == EnumDepositJobState.SUCCEED) {
                    depositJobService.jobDepositFinished(job);
                }
            } catch (Exception e) {
                log.error("Failed to scan deposit job status", e);
            }
        }
    }
}