package nz.govt.natlib.dashboard.domain.daemon;

import com.exlibris.dps.SipStatusInfo;
import nz.govt.natlib.dashboard.common.injection.InjectionFileStat;
import nz.govt.natlib.dashboard.common.injection.InjectionUtils;
import nz.govt.natlib.dashboard.common.injection.UnionFile;
import nz.govt.natlib.dashboard.common.injection.UnionPath;
import nz.govt.natlib.dashboard.common.metadata.EnumActualContentDeletionOptions;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import nz.govt.natlib.ndha.common.exlibris.ResultOfDeposit;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleProcessorImpl extends ScheduleProcessorBasic {
    public ScheduleProcessorImpl(EntityFlowSetting flowSetting) {
        super(flowSetting);
    }

    @Override
    public void handleIngest() {
        //Summarize the number and size of files
        InjectionFileStat stat = new InjectionFileStat();
        List<UnionFile> injectionDirs = injectionPathScanClient.listRootDir();
        for (UnionFile injectionDir : injectionDirs) {
            if (!injectionDir.isPath()) {
                log.info("Skip the path which is not subfolder: {}", injectionDir.getAbsolutePath());
                continue;
            }

            File depositDoneFile = new File(injectionDir.getAbsolutePath(), "done");
            if (injectionPathScanClient.exists(depositDoneFile.getAbsolutePath())) {
                log.debug("The job {} had been deposited", injectionDir.getAbsolutePath());
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

    @Override
    public boolean handleDeposit(EntityDepositJob job) {
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
    public void handlePollingStatus(EntityDepositJob job) {
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
    public void handleFinalize(EntityDepositJob job) throws IOException {
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

    @Override
    public void handleHistoryPruning() throws IOException {
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
