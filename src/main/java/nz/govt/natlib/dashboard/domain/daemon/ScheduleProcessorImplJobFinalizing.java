package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.injection.InjectionPathScan;
import nz.govt.natlib.dashboard.common.injection.InjectionUtils;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.common.metadata.EnumSystemEventLevel;
import nz.govt.natlib.dashboard.common.metadata.EnumSystemEventModule;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;

import java.io.File;
import java.util.List;

public class ScheduleProcessorImplJobFinalizing extends ScheduleProcessor {
    @Override
    public void handle(EntityFlowSetting flowSetting) throws Exception {
        List<EntityDepositJob> listOfJobs = repoDepositJobActive.getByFlowId(flowSetting.getId());
        for (EntityDepositJob job : listOfJobs) {
            //Using the applied Injection Storage Location
            InjectionPathScan injectionPathScanClient = InjectionUtils.createPathScanClient(job.getAppliedInjectionStorageLocation());
            if (injectionPathScanClient == null) {
                log.error("Failed to initial PathScanClient instance.");
                return;
            }

            //Backup to the current Backup Storage Location
            InjectionPathScan backupPathScanClient = InjectionUtils.createPathScanClient(repoStorageLocation.getById(flowSetting.getBackupEndPointId()));
            if (backupPathScanClient == null) {
                log.error("Failed to initial Backup PathScanClient instance.");
                return;
            }

            //Finalize success jobs
            if ((job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.SUCCEED) ||
                    (job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.INITIALED) ||
                    (job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.RUNNING)) {
                depositJobService.jobFinalizeStart(job);

                //Backup contents
                if (flowSetting.isBackupEnabled()) {
                    InjectionUtils.copyFiles(injectionPathScanClient, backupPathScanClient, new File(injectionPathScanClient.getRootPath(), job.getInjectionTitle()));
                }

                //Delete existing met contents
                InjectionUtils.deleteFiles(injectionPathScanClient, new File(injectionPathScanClient.getRootPath(), job.getInjectionTitle()));

                depositJobService.jobFinalizeEnd(job, EnumDepositJobState.SUCCEED);
            }
        }
    }
}
