package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.injection.InjectionPathScan;
import nz.govt.natlib.dashboard.common.injection.InjectionUtils;
import nz.govt.natlib.dashboard.common.metadata.EnumActualContentDeletionOptions;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.util.DashboardHelper;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleProcessorImplJobFinalizing extends ScheduleProcessor {
    @Override
    public void handle(EntityFlowSetting flowSetting) throws Exception {
        List<EntityDepositJob> listOfJobs = repoDepositJob.getByFlowId(flowSetting.getId());
        for (EntityDepositJob job : listOfJobs) {
            //Using the applied Injection Storage Location
            InjectionPathScan injectionPathScanClient = InjectionUtils.createPathScanClient(job.getAppliedFlowSetting().getRootPath());

            //Finalize success jobs
            if ((job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.SUCCEED) ||
                    (job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.INITIALED) ||
                    (job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.RUNNING)) {
                depositJobService.jobFinalizeStart(job);

                File depositDoneFile = new File(job.getInjectionPath(), "done");
                if (!injectionPathScanClient.exists(depositDoneFile.getAbsolutePath())) {
                    if (!depositDoneFile.createNewFile()) {
                        log.error("Failed to create file: {}", depositDoneFile.getAbsolutePath());
                        continue;
                    }
                }
                depositJobService.jobFinalizeEnd(job, EnumDepositJobState.SUCCEED);

                log.info("Finalize job: {} {}", job.getId(), job.getInjectionTitle());
            }

            if (job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.SUCCEED) {
                EnumActualContentDeletionOptions deletionOptions = flowSetting.getEnumActualContentDeleteOptions();
                boolean isDeleteActualContent = false;
                if (deletionOptions == EnumActualContentDeletionOptions.deleteInstantly) {
                    isDeleteActualContent = true;
                } else if (deletionOptions == EnumActualContentDeletionOptions.deleteExceedMaxStorageDays) {
                    LocalDateTime deadlineTime = LocalDateTime.now().minusDays(flowSetting.getMaxSaveDays());
                    LocalDateTime jobLatestUpdateTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getLatestTime());
                    if (jobLatestUpdateTime.compareTo(deadlineTime) < 0) {
                        isDeleteActualContent = true;
                    }
                }

                log.info("Deletion options: {}, isDeleteActualContent: {}", deletionOptions.name(), isDeleteActualContent);
                if (isDeleteActualContent) {
                    File ingestPath = new File(job.getInjectionPath());
                    InjectionUtils.deleteFiles(injectionPathScanClient, ingestPath);
                    log.info("Delete the actual content of job: {}, folder: {}", job.getId(), ingestPath.getAbsolutePath());
                }
            }
        }
    }
}
