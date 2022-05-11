package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.util.DashboardHelper;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleProcessorImplJobHistoryPruning extends ScheduleProcessor {
    @Override
    public void handle(EntityFlowSetting flowSetting) throws Exception {
        //Delete the expired history jobs
        List<EntityDepositJob> listOfHistoryJobs = repoDepositJob.getByFlowId(flowSetting.getId());
        for (EntityDepositJob job : listOfHistoryJobs) {
            //Remove canceled and expired job
            LocalDateTime deadlineTime = LocalDateTime.now().minusDays(flowSetting.getMaxSaveDays());
            LocalDateTime jobLatestUpdateTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getLatestTime());
            if (jobLatestUpdateTime.compareTo(deadlineTime) < 0) {
                repoDepositJob.deleteById(job.getId());
            }
        }
    }
}