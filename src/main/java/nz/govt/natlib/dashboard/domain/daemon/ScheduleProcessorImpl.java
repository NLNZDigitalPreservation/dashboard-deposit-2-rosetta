package nz.govt.natlib.dashboard.domain.daemon;

import com.exlibris.dps.SipStatusInfo;
import nz.govt.natlib.dashboard.common.injection.*;
import nz.govt.natlib.dashboard.common.metadata.EnumActualContentDeletionOptions;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import nz.govt.natlib.ndha.common.exlibris.ResultOfDeposit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduleProcessorImpl extends ScheduleProcessorBasic {
    @Override
    public void handleIngest() {
        List<EntityFlowSetting> allFlowSettings = this.repoFlowSetting.getAll();
        for (EntityFlowSetting flowSetting : allFlowSettings) {
            if (!flowSetting.isEnabled()) {
                log.warn("Disabled Material Flow: {} {}", flowSetting.getId(), flowSetting.getMaterialFlowId());
                continue;
            }
            //Initial the scanClient in each loop just in case the RootPath is changed
            InjectionPathScan injectionPathScanClient = InjectionUtils.createPathScanClient(flowSetting.getRootPath());


            //Summarize the number and size of files
            InjectionFileStat stat = new InjectionFileStat();
            List<UnionFile> injectionDirs = injectionPathScanClient.listRootDir();
            for (UnionFile injectionDir : injectionDirs) {
                if (!injectionDir.isPath()) {
                    log.info("Skip the path which is not a subfolder: {}", injectionDir.getAbsolutePath());
                    continue;
                }

                File depositDoneFile = new File(injectionDir.getAbsolutePath(), "done");
                if (injectionPathScanClient.exists(depositDoneFile.getAbsolutePath())) {
                    log.debug("Skip the subfolder {}, it had been deposited, 'done' file found.", injectionDir.getAbsolutePath());
                    continue;
                }

                File injectionPath = injectionDir.getAbsolutePath();
                EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), injectionPath.getName());
                //Initial job
                if (job == null) {
                    job = depositJobService.jobInitial(injectionPath.getAbsolutePath(), injectionDir.getName(), flowSetting);
                    log.info("Created a new job: {} {}", job.getId(), job.getInjectionTitle());
                }


                //Ignore the jobs not in the INITIAL stage
                if (job.getStage() != EnumDepositJobStage.INGEST || job.getState() != EnumDepositJobState.RUNNING) {
                    log.debug("Skip unprepared job for: {} --> {} at status [{}] [{}]", flowSetting.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
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
            }
        }
    }

    @Override
    public boolean handleDeposit(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) {
        if (!(job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.INITIALED)) {
            log.debug("Skip deposit job for: {} --> {} at status [{}] [{}]", flowSetting.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
            return false;
        }

        EntityDepositAccountSetting depositAccount = repoDepositAccount.getById(flowSetting.getDepositAccountId());
        String pdsHandle;
        try {
            pdsHandle = rosettaWebService.login(depositAccount.getDepositUserInstitute(), depositAccount.getDepositUserName(), depositAccount.getDepositUserPassword());
        } catch (Exception e) {
            log.error("Failed to submit job", e);
            return false;
        }

        ResultOfDeposit resultOfDeposit;
        try {
            resultOfDeposit = this.rosettaWebService.deposit(job.getInjectionTitle(), pdsHandle, depositAccount.getDepositUserInstitute(), flowSetting.getProducerId(), flowSetting.getMaterialFlowId(), job.getDepositSetId());
        } catch (Exception e) {
            log.error("Failed to submit job", e);
            return false;
        }

        if (resultOfDeposit.isSuccess()) {
            depositJobService.jobDepositAccept(job, resultOfDeposit.getSipID(), flowSetting);
            log.info("Job [{}] is submitted to Rosetta successfully, SIPId=[{}], Status=[{}]",
                    job.getInjectionTitle(), resultOfDeposit.getSipID(), resultOfDeposit.getResultMessage());
            return true;
        } else {
            depositJobService.jobDepositReject(job, resultOfDeposit.getResultMessage());
            log.warn("Job [{}] is submitted to Rosetta failed, msg: [{}]", job.getInjectionTitle(), resultOfDeposit.getResultMessage());
            return false;
        }
    }

    @Override
    public void handlePollingStatus(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) {
        if (job.getStage() != EnumDepositJobStage.DEPOSIT || job.getState() != EnumDepositJobState.RUNNING) {
            log.debug("Skip polling. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
            return;
        }
        SipStatusInfo sipStatusInfo;
        try {
            log.info("Update polling, before. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
            sipStatusInfo = rosettaWebService.getSIPStatusInfo(job.getSipID());
            log.info("Update polling. jobId: {}, jobName: {}, SIPStatusInfo: {}, {}, {}", job.getId(), job.getInjectionTitle(), sipStatusInfo.getModule(), sipStatusInfo.getStage(), sipStatusInfo.getStatus());
            job = depositJobService.jobUpdateStatus(job, sipStatusInfo);
            //            if (job.getState() == EnumDepositJobState.FAILED || job.getState() == EnumDepositJobState.SUCCEED) {
            //                depositJobService.jobDepositFinished(job);
            //            }
            log.info("Update polling, after. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
        } catch (Exception e) {
            log.error("Failed to scan deposit job status", e);
        }
    }

    @Override
    public void handleFinalize(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) throws IOException {
        //Finalize success jobs
        if ((job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.SUCCEED) ||
                (job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.INITIALED) ||
                (job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.RUNNING)) {
            depositJobService.jobFinalizeStart(job);

            File depositDoneFile = new File(job.getInjectionPath(), "done");
            if (!injectionPathScanClient.exists(depositDoneFile.getAbsolutePath())) {
                if (!depositDoneFile.createNewFile()) {
                    log.error("Failed to create file: {}", depositDoneFile.getAbsolutePath());
                    return;
                }
            }
            depositJobService.jobFinalizeEnd(job, EnumDepositJobState.SUCCEED);
            log.info("Finalize job: {} {}", job.getId(), job.getInjectionTitle());
        }

        if ((job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.SUCCEED) ||
                (job.getStage() == EnumDepositJobStage.FINISHED && job.getState() == EnumDepositJobState.SUCCEED)) {
            String strDeletionOption = flowSetting.getActualContentDeleteOptions();
            EnumActualContentDeletionOptions deletionOptions;
            if (StringUtils.isEmpty(strDeletionOption)) {
                deletionOptions = EnumActualContentDeletionOptions.notDelete;
            } else {
                deletionOptions = EnumActualContentDeletionOptions.valueOf(strDeletionOption);
            }

            boolean isDeleteActualContent = false;
            if (deletionOptions == EnumActualContentDeletionOptions.deleteInstantly) {
                isDeleteActualContent = true;
            } else if (deletionOptions == EnumActualContentDeletionOptions.deleteExceedMaxStorageDays) {
                LocalDateTime deadlineTime = LocalDateTime.now().minusDays(flowSetting.getMaxSaveDays());
                LocalDateTime jobLatestUpdateTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getLatestTime());
                if (jobLatestUpdateTime.isBefore(deadlineTime)) {
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

    @Override
    public void handleHistoryPruning(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) throws IOException {
        //Remove canceled and expired job
        LocalDateTime deadlineTime = LocalDateTime.now().minusDays(flowSetting.getMaxSaveDays());
        LocalDateTime jobLatestUpdateTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getLatestTime());
        if (jobLatestUpdateTime.isBefore(deadlineTime)) {
            repoDepositJob.deleteById(job.getId());
        }
    }

    @Override
    public void handleFlowSettingMissingJob(EntityDepositJob job) throws IOException {
        EntityFlowSetting flowSetting = job.getAppliedFlowSetting();
        InjectionPathScan injectionPathScanClient = InjectionUtils.createPathScanClient(flowSetting.getRootPath());
        job.setResultMessage("Canceled because the linked flowSetting is deleted.");
        depositJobService.cancel(job);
    }
}
