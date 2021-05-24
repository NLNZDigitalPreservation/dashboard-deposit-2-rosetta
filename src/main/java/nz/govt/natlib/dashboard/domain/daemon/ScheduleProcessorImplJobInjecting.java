package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.injection.*;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.common.metadata.EnumSystemEventLevel;
import nz.govt.natlib.dashboard.common.metadata.EnumSystemEventModule;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class ScheduleProcessorImplJobInjecting extends ScheduleProcessor {
    @Override
    public void handle(EntityFlowSetting flowSetting) throws Exception {
        if (!flowSetting.isEnabled()) {
            log.warn("Disabled Material Flow: {} {}", flowSetting.getName(), flowSetting.getMaterialFlowId());
            return;
        }

        InjectionPathScan injectionPathScanClient = InjectionUtils.createPathScanClient(repoStorageLocation.getById(flowSetting.getInjectionEndPointId()));
        if (injectionPathScanClient == null) {
            log.error("Failed to initial PathScanClient instance.");
            return;
        }

        //Summarize the number and size of files
        InjectionFileStat stat = new InjectionFileStat();

        List<UnionFile> injectionDirs = injectionPathScanClient.listRootDir();
        for (UnionFile injectionDir : injectionDirs) {
            if (!injectionDir.isPath()) {
                continue;
            }

            File injectionPath = injectionDir.getAbsolutePath();
            EntityDepositJob job = repoDepositJobActive.getByFlowIdAndInjectionTitle(flowSetting.getId(), injectionPath.getName());
            //Initial job
            if (job == null) {
                job = depositJobService.jobInitial(injectionPath.getAbsolutePath(), injectionDir.getName(), flowSetting);
            }

            //Ignore the jobs not in the INITIAL stage
            if (job.getStage() != EnumDepositJobStage.INJECT || job.getState() != EnumDepositJobState.RUNNING) {
                log.debug("Skip unprepared job for: {} --> {} at status [{}] [{}]", flowSetting.getName(), job.getInjectionTitle(), job.getStage(), job.getState());
                continue;
            }

            stat.stat(injectionPathScanClient, new UnionPath(injectionPath, flowSetting.getStreamLocation()));
            job = depositJobService.jobUpdateFilesStat(job, stat.getFileCount(), stat.getFileSize());

            File doneFile = new File(injectionPath, flowSetting.getInjectionCompleteFileName());
            if (!injectionPathScanClient.exists(UnionPath.of(doneFile))) {
                log.debug("{} file does not exist in: {}", flowSetting.getInjectionCompleteFileName(), injectionPath.getAbsolutePath());
                continue;
            }

            job = depositJobService.jobScanComplete(job);

            //Flush the file stat
            stat.stat(injectionPathScanClient, new UnionPath(injectionPath, flowSetting.getStreamLocation()));
            job = depositJobService.jobUpdateFilesStat(job, stat.getFileCount(), stat.getFileSize());

            log.debug("Initialed new job : {}", job.getId());

            injectionPathScanClient.disconnect();
        }
    }
}