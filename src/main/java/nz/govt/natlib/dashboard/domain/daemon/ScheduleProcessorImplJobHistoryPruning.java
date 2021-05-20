package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.injection.InjectionPathScan;
import nz.govt.natlib.dashboard.common.injection.InjectionUtils;
import nz.govt.natlib.dashboard.common.metadata.EnumSystemEventLevel;
import nz.govt.natlib.dashboard.common.metadata.EnumSystemEventModule;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.util.DashboardHelper;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleProcessorImplJobHistoryPruning extends ScheduleProcessor {
    @Override
    public void handle(EntityFlowSetting flowSetting) throws Exception {
        InjectionPathScan injectionPathScanClient = InjectionUtils.createPathScanClient(repoStorageLocation.getById(flowSetting.getInjectionEndPointId()));
        if (injectionPathScanClient == null) {
            log.error("Failed to initial PathScanClient instance.");
            return;
        }

        InjectionPathScan backupPathScanClient = InjectionUtils.createPathScanClient(repoStorageLocation.getById(flowSetting.getBackupEndPointId()));
        if (backupPathScanClient == null) {
            log.error("Failed to initial Backup PathScanClient instance.");
            return;
        }

        //Delete the expired history jobs
        List<EntityDepositJob> listOfHistoryJobs = repoDepositJobHistory.getByFlowId(flowSetting.getId());
        for (EntityDepositJob job : listOfHistoryJobs) {
            //Remove canceled and expired job
            LocalDateTime deadlineTime = LocalDateTime.now().minusDays(flowSetting.getMaxSaveDays());
            LocalDateTime jobLatestUpdateTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getLatestTime());
            if (jobLatestUpdateTime.compareTo(deadlineTime) < 0) {
                repoDepositJobHistory.deleteById(job.getId());
            }
        }
    }
}