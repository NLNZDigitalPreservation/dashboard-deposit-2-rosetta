package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.injection.InjectionPathScan;
import nz.govt.natlib.dashboard.common.injection.InjectionUtils;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.common.metadata.EnumSystemEventLevel;
import nz.govt.natlib.dashboard.common.metadata.EnumSystemEventModule;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.util.DashboardHelper;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleProcessorImplJobAging extends ScheduleProcessor {
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

        List<EntityDepositJob> listOfJobs = repoDepositJobActive.getByFlowId(flowSetting.getId());
        for (EntityDepositJob job : listOfJobs) {
            //Only finalized succeed or canceled jobs can be moved to history
            if (!((job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.SUCCEED) || job.getState() == EnumDepositJobState.CANCELED)) {
                log.info("Skipped job: {}, stage={}, state={}", job.getInjectionTitle(), job.getStage(), job.getState());
                continue;
            }

            //Remove canceled and expired job
            LocalDateTime deadlineTime = LocalDateTime.now().minusDays(flowSetting.getMaxActiveDays());
            LocalDateTime jobLatestUpdateTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getLatestTime());
            if (jobLatestUpdateTime.compareTo(deadlineTime) < 0) {
                //Backup contents
                if (flowSetting.isBackupEnabled()) {
                    InjectionUtils.copyFiles(injectionPathScanClient, backupPathScanClient, new File(injectionPathScanClient.getRootPath(), job.getInjectionTitle()));
                }

                //Delete existing met contents
                InjectionUtils.deleteFiles(injectionPathScanClient, new File(injectionPathScanClient.getRootPath(), job.getInjectionTitle()));

                //Move the job entity to the history directory
                repoDepositJobHistory.save(job);
                repoDepositJobActive.deleteById(job.getId());
            }
        }
    }
}
