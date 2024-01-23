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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                File injectionPath = injectionDir.getAbsolutePath();
                String subFolderFullPath = injectionPath.getAbsolutePath();
                if (this.processingJobs.containsKey(subFolderFullPath)) {
                    log.debug("Ignore the [processing] subfolder: {}", subFolderFullPath);
                    continue;
                }

                if (!injectionDir.isPath()) {
                    log.debug("Skip the path which is not a subfolder: {}", injectionDir.getAbsolutePath());
                    continue;
                }

                File depositDoneFile = new File(injectionPath, "done");
                if (injectionPathScanClient.exists(depositDoneFile.getAbsolutePath())) {
                    log.debug("Ignore the subfolder {}, it had been deposited, 'done' file found.", injectionDir.getAbsolutePath());
                    this.processingJobs.put(subFolderFullPath, Boolean.TRUE);
                    continue;
                }

                //Ignore the jobs not in the INITIAL stage
                EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), injectionPath.getName());
                //Initial job
                if (job == null) {
                    job = depositJobService.jobInitial(injectionPath.getAbsolutePath(), injectionDir.getName(), flowSetting);
                    log.info("Created a new job: {} {}", job.getId(), job.getInjectionTitle());
                }

                if (job.getState() == EnumDepositJobState.PAUSED || job.getState() == EnumDepositJobState.CANCELED) {
                    log.debug("Ignore the subfolder, it's paused or cancelled: {}, status [{}] [{}]", subFolderFullPath, job.getStage(), job.getState());
                    continue;
                }

                if (job.getStage() != EnumDepositJobStage.INGEST) {
                    log.debug("Ignore the subfolder, it's ingested: {}, status [{}] [{}]", subFolderFullPath, job.getStage(), job.getState());
                    this.processingJobs.put(subFolderFullPath, Boolean.TRUE);
                    continue;
                }

                //Ignore the unprepared subfolders
                File predepositDoneFile = new File(injectionPath, flowSetting.getInjectionCompleteFileName());
                if (!injectionPathScanClient.exists(UnionPath.of(predepositDoneFile))) {
                    log.debug("Ignore the subfolder {}, {} file does not exist", subFolderFullPath, flowSetting.getInjectionCompleteFileName());
                    continue;
                }

                //Flush the file stat
                stat.stat(injectionPathScanClient, new UnionPath(injectionPath, flowSetting.getStreamLocation()));
                job = depositJobService.jobUpdateFilesStat(job, stat.getFileCount(), stat.getFileSize());
                job = depositJobService.jobScanComplete(job);

                log.info("Ingested new job : {}", job.getId());
                this.processingJobs.put(subFolderFullPath, Boolean.TRUE);
            }
            injectionDirs.clear();
        }
        allFlowSettings.clear();
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
    public void handlePollingStatus(EntityDepositJob job) {
        if (job.getStage() != EnumDepositJobStage.DEPOSIT || job.getState() != EnumDepositJobState.RUNNING) {
            log.debug("Ignore polling. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
            return;
        }
        SipStatusInfo sipStatusInfo;
        try {
            log.debug("Polling, before. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
            sipStatusInfo = rosettaWebService.getSIPStatusInfo(job.getSipID());
            log.debug("Polling, jobId: {}, jobName: {}, SIPStatusInfo: {}, {}, {}", job.getId(), job.getInjectionTitle(), sipStatusInfo.getModule(), sipStatusInfo.getStage(), sipStatusInfo.getStatus());
            job = depositJobService.jobUpdateStatus(job, sipStatusInfo);
            log.debug("Polling, after. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
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
//            job = depositJobService.jobFinalizeStart(job);

            File depositDoneFile = new File(job.getInjectionPath(), "done");
            if (!injectionPathScanClient.exists(depositDoneFile.getAbsolutePath())) {
                log.warn("The succeed finished job: {} has no [done] file: {}", job.getId(), depositDoneFile.getAbsolutePath());
                if (!depositDoneFile.createNewFile()) {
                    log.error("Failed to create file: {}, {}", job.getId(), depositDoneFile.getAbsolutePath());
                    return;
                }
            }
            job = depositJobService.jobFinalizeEnd(job, EnumDepositJobState.SUCCEED);
            log.info("Finalize job: {} {}", job.getId(), job.getInjectionTitle());
        } else if (job.getState() == EnumDepositJobState.CANCELED && job.getStage() != EnumDepositJobStage.FINISHED) {
            LocalDateTime deadlineTime = LocalDateTime.now().minusDays(flowSetting.getMaxActiveDays());
            LocalDateTime jobLatestUpdateTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getLatestTime());
            if (jobLatestUpdateTime.isBefore(deadlineTime)) {

                job = depositJobService.jobUpdateStatus(job, EnumDepositJobStage.FINISHED, job.getState());

                log.info("Finalize job: {} {}", job.getId(), job.getInjectionTitle());
            }
        }

        //Backup actual contents, delete actual contents and archive finished jobs
        if (job.getStage() != EnumDepositJobStage.FINISHED) {
            // Backup the actual contents
            if (!job.isBackupCompleted()) {
                if (this.backupActualContents(flowSetting, job)) {
                    depositJobService.jobCompletedBackup(job);
                    log.info("Backed up the actual content of job: {}", job.getId());
                } else {
                    log.error("Failed to backup job: {}", job.getId());
                    return;
                }
            }

            // Delete the actual contents
            String strDeletionOption = flowSetting.getActualContentDeleteOptions();
            EnumActualContentDeletionOptions deletionOptions;
            if (StringUtils.isEmpty(strDeletionOption)) {
                deletionOptions = EnumActualContentDeletionOptions.notDelete;
            } else {
                deletionOptions = EnumActualContentDeletionOptions.valueOf(strDeletionOption);
            }

            if (deletionOptions != EnumActualContentDeletionOptions.notDelete && !job.isActualContentDeleted()) {
                if (this.deleteActualContents(flowSetting, injectionPathScanClient, job)) {
                    depositJobService.jobDeletedActualContent(job);
                    log.info("Deleted the actual content of job: {}", job.getId());
                } else {
                    log.error("Failed to delete actual contents: {}", job.getId());
                    return;
                }
            }

            //Remove canceled and expired job
            LocalDateTime deadlineTime = LocalDateTime.now().minusDays(flowSetting.getMaxSaveDays());
            LocalDateTime jobLatestUpdateTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getLatestTime());
            if (jobLatestUpdateTime.isBefore(deadlineTime)) {
                repoDepositJob.moveToHistory(job.getId());
                log.info("Pruned the history job: {}", job.getId());
            } else {
                log.debug("Ignore pruning the history job: {} {}", job.getId(), jobLatestUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        }
    }

    @Override
    public void handleHistoryPruning(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) {
        //TODO
    }

    @Override
    public void handleFlowSettingMissingJob(EntityDepositJob job) throws IOException {
        try {
            if (job.getStage() == EnumDepositJobStage.FINISHED && (job.getState() == EnumDepositJobState.FAILED || job.getState() == EnumDepositJobState.CANCELED || job.getState() == EnumDepositJobState.SUCCEED)) {
                return;
            }
            depositJobService.cancelFlowMissingJob(job);
        } catch (Exception e) {
            log.error("Failed to cancel the flow missing job, {}", job.getId(), e);
            throw new IOException(e.getMessage());
        }
    }

    private boolean isSubFoldersExisting(String subFolders, EntityDepositJob job) {
        long existingSubFolders = subFolders.lines().filter(subFolder -> {
            File srcSubFolder = new File(job.getInjectionPath(), subFolder);
            return srcSubFolder.exists();
        }).count();
        return existingSubFolders > 0;
    }

    private boolean backupActualContents(EntityFlowSetting flowSetting, EntityDepositJob job) {
        if (StringUtils.isEmpty(flowSetting.getActualContentBackupOptions()) ||
                StringUtils.equalsIgnoreCase(flowSetting.getActualContentBackupOptions(), "notBackup")) {
            return true;
        }

        if (job.isBackupCompleted()) {
            log.debug("Skip completed backup: {}", job.getInjectionTitle());
            return true;
        }

        String subFolders = flowSetting.getBackupSubFolders();
        if (StringUtils.isEmpty(subFolders) || (!isSubFoldersExisting(subFolders, job))) {
            log.info("Sub folders are empty. flow setting: {}", flowSetting.getMaterialFlowName());
            return true;
        }

        File targetDirectory = new File(flowSetting.getBackupPath(), job.getInjectionTitle());
        //Clear the existing directory
        try {
            if (targetDirectory.exists() && targetDirectory.isDirectory()) {
                FileUtils.deleteDirectory(targetDirectory);
            }
        } catch (IOException e) {
            log.error("Failed to clear the existing directory: {}", targetDirectory.getAbsolutePath(), e);
            return false;
        }

        if (!targetDirectory.exists()) {
            boolean ret = targetDirectory.mkdirs();
            if (!ret) {
                log.error("Failed to create backup directory: {}", targetDirectory.getAbsolutePath());
                return false;
            } else {
                log.debug("Created the backup directory: {}", targetDirectory.getAbsolutePath());
            }
        }

        File[] existingFiles = targetDirectory.listFiles();
        if (existingFiles != null && existingFiles.length > 0) {
            log.error("The backup target folder is not empty: {}", targetDirectory);
            return false;
        }

        // Backup the sidecar file
        String[] subFolderAry = (String[]) subFolders.lines().toArray();
        if (StringUtils.equalsIgnoreCase(flowSetting.getActualContentBackupOptions(), "backupSubFolder")) {
            for (String subFolder : subFolderAry) {
                File srcSubFolder = new File(job.getInjectionPath(), subFolder);
                if (srcSubFolder.exists()) {
                    File destSubFolder = new File(targetDirectory, subFolder);
                    boolean ret = destSubFolder.mkdirs();
                    if (!ret) {
                        log.error("Failed to create sub folder: {}", destSubFolder.getAbsolutePath());
                        return false;
                    } else {
                        log.debug("Created the sub folder: {}", destSubFolder.getAbsolutePath());
                    }
                    try {
                        FileUtils.copyDirectory(srcSubFolder, destSubFolder, true);
                        log.debug("Failed to copy file: {} -> {}", srcSubFolder.getAbsolutePath(), destSubFolder.getAbsolutePath());
                    } catch (IOException e) {
                        log.error("Failed to copy file: {} -> {}", srcSubFolder.getAbsolutePath(), destSubFolder.getAbsolutePath());
                        return false;
                    }
                }
            }
        } else if (StringUtils.equalsIgnoreCase(flowSetting.getActualContentBackupOptions(), "backupContentsWithoutSubFolderName")) {
            for (String subFolder : subFolderAry) {
                File srcSubFolder = new File(job.getInjectionPath(), subFolder);
                if (srcSubFolder.exists()) {
                    try {
                        FileUtils.copyDirectory(srcSubFolder, targetDirectory, true);
                        log.debug("Failed to copy file: {} -> {}", srcSubFolder.getAbsolutePath(), targetDirectory.getAbsolutePath());
                    } catch (IOException e) {
                        log.error("Failed to copy file: {} -> {}", srcSubFolder.getAbsolutePath(), targetDirectory.getAbsolutePath());
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean deleteActualContents(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) {
        if (job.isActualContentDeleted()) {
            return true;
        }

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

            return InjectionUtils.deleteFiles(injectionPathScanClient, ingestPath);
        }
        return true;
    }
}
