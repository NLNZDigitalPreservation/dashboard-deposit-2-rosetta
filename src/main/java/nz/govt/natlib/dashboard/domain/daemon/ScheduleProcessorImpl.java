package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.injection.*;
import nz.govt.natlib.dashboard.common.metadata.*;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.util.DashboardHelper;
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
    public boolean handleDeposit(EntityDepositAccountSetting depositAccount,EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) {
        if (!(job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.INITIALED)) {
            log.debug("Skip deposit job for: {} --> {} at status [{}] [{}]", flowSetting.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
            return false;
        }

        ResultOfDeposit resultOfDeposit;
        try {
            resultOfDeposit = this.rosettaWebService.deposit(depositAccount, job.getInjectionTitle(), flowSetting.getProducerId(), flowSetting.getMaterialFlowId());
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
    public void handlePollingStatus(EntityDepositAccountSetting depositAccount,EntityDepositJob job) {
        if (job.getStage() != EnumDepositJobStage.DEPOSIT || job.getState() != EnumDepositJobState.RUNNING) {
            log.debug("Ignore polling. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
            return;
        }
        SipStatusInfo sipStatusInfo;
        try {
            log.info("Update polling, before. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
            sipStatusInfo = rosettaWebService.getSIPStatusInfo(depositAccount,job.getSipID());
            log.info("Update polling. jobId: {}, jobName: {}, SIPStatusInfo: {}, {}", job.getId(), job.getInjectionTitle(), sipStatusInfo.getStage(), sipStatusInfo.getStatus());

            job = depositJobService.jobUpdateStatus(job, sipStatusInfo);
            log.debug("Polling, after. jobId: {}, jobName: {}, jobStage: {}, jobState: {}", job.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
        } catch (Exception e) {
            log.error("Failed to scan deposit job status", e);
        }
    }

    @Override
    public void handleFinalize(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) {
        //Finalize successful jobs
        if ((job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.SUCCEED) ||
                (job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.INITIALED) ||
                (job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.RUNNING)) {

            File depositDoneFile = new File(job.getInjectionPath(), "done");
            if (!depositDoneFile.getParentFile().exists()) {
                log.error("The original directory does not exist: {}", job.getInjectionPath());
                return;
            }

            if (!injectionPathScanClient.exists(depositDoneFile.getAbsolutePath())) {
                log.warn("The succeed finished job: {} has no [done] file: {}", job.getId(), depositDoneFile.getAbsolutePath());
                try {
                    if (!depositDoneFile.createNewFile()) {
                        log.error("Failed to create file: {}, {}", job.getId(), depositDoneFile.getAbsolutePath());
                        return;
                    }
                } catch (Exception e) {
                    log.error("Failed to create file: {}, {}", job.getId(), depositDoneFile.getAbsolutePath());
                    return;
                }
            }
            job = depositJobService.jobFinalizeEnd(job, EnumDepositJobState.SUCCEED);
            log.info("Finalize job: {} {}", job.getId(), job.getInjectionTitle());
        } else if (job.getState() == EnumDepositJobState.CANCELED && job.getStage() != EnumDepositJobStage.FINISHED) {
            job = depositJobService.jobUpdateStatus(job, EnumDepositJobStage.FINISHED, job.getState());
            log.info("Finalized job: {} {}", job.getId(), job.getInjectionTitle());
        }

        // log.info("{} {} {}", job.getId(), job.getStage(), job.getState());

        //Skip unfinished jobs
        if (job.getStage() != EnumDepositJobStage.FINISHED) {
            return;
        }

        // Backup the actual contents
        this.tryToBackupActualContents(flowSetting, job);
        if (!job.isBackupCompleted()) {
            return;
        }

        // Delete the actual contents
        this.tryToDeleteActualContents(flowSetting, injectionPathScanClient, job);
        if (!job.isActualContentDeleted()) {
            return;
        }

        //Remove canceled and expired job
        LocalDateTime deadlineTime = LocalDateTime.now().minusDays(flowSetting.getMaxActiveDays());
        LocalDateTime jobLatestUpdateTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getLatestTime());
        if (jobLatestUpdateTime.isBefore(deadlineTime)) {
            repoDepositJob.moveToHistory(job.getId());
            log.info("Pruned the history job: {}", job.getId());
        } else {
            log.debug("Ignore pruning the history job: {} {}", job.getId(), jobLatestUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
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

    private void tryToBackupActualContents(EntityFlowSetting flowSetting, EntityDepositJob job) {
        if (job.isBackupCompleted()) {
            log.debug("Skip completed backup: {}", job.getInjectionTitle());
            return;
        }

        if (StringUtils.isEmpty(flowSetting.getActualContentBackupOptions()) ||
                StringUtils.equalsIgnoreCase(flowSetting.getActualContentBackupOptions(), "notBackup")) {
            this.depositJobService.jobCompletedBackup(job);
            return;
        }

        String subFolders = flowSetting.getBackupSubFolders();
        if (StringUtils.isEmpty(subFolders) || (!isSubFoldersExisting(subFolders, job))) {
            log.info("Sub folders are empty. flow setting: {}", flowSetting.getMaterialFlowName());
            this.depositJobService.jobCompletedBackup(job);
            return;
        }

        File targetDirectory = new File(flowSetting.getBackupPath(), job.getInjectionTitle());
        //Clear the existing directory
        try {
            if (targetDirectory.exists() && targetDirectory.isDirectory()) {
                FileUtils.deleteDirectory(targetDirectory);
            }
        } catch (IOException e) {
            log.error("Failed to clear the existing directory: {}", targetDirectory.getAbsolutePath(), e);
            return;
        }

        if (!targetDirectory.exists()) {
            boolean ret = targetDirectory.mkdirs();
            if (!ret) {
                log.error("Failed to create backup directory: {}", targetDirectory.getAbsolutePath());
                return;
            } else {
                log.debug("Created the backup directory: {}", targetDirectory.getAbsolutePath());
            }
        }

        File[] existingFiles = targetDirectory.listFiles();
        if (existingFiles != null && existingFiles.length > 0) {
            log.error("The backup target folder is not empty: {}", targetDirectory);
            return;
        }

        // Backup the sidecar file
        Object[] subFolderAry = subFolders.lines().toArray();
        if (StringUtils.equalsIgnoreCase(flowSetting.getActualContentBackupOptions(), "backupSubFolder")) {
            for (Object obj : subFolderAry) {
                String subFolder = (String) obj;
                File srcSubFolder = new File(job.getInjectionPath(), subFolder);
                if (srcSubFolder.exists()) {
                    File destSubFolder = new File(targetDirectory, subFolder);
                    boolean ret = destSubFolder.mkdirs();
                    if (!ret) {
                        log.error("Failed to create sub folder: {}", destSubFolder.getAbsolutePath());
                        return;
                    } else {
                        log.debug("Created the sub folder: {}", destSubFolder.getAbsolutePath());
                    }
                    try {
                        FileUtils.copyDirectory(srcSubFolder, destSubFolder, true);
                        log.debug("Failed to copy file: {} -> {}", srcSubFolder.getAbsolutePath(), destSubFolder.getAbsolutePath());
                    } catch (IOException e) {
                        log.error("Failed to copy file: {} -> {}", srcSubFolder.getAbsolutePath(), destSubFolder.getAbsolutePath());
                        return;
                    }
                }
            }
        } else if (StringUtils.equalsIgnoreCase(flowSetting.getActualContentBackupOptions(), "backupContentsWithoutSubFolderName")) {
            for (Object obj : subFolderAry) {
                String subFolder = (String) obj;
                File srcSubFolder = new File(job.getInjectionPath(), subFolder);
                if (srcSubFolder.exists()) {
                    try {
                        FileUtils.copyDirectory(srcSubFolder, targetDirectory, true);
                        log.debug("Failed to copy file: {} -> {}", srcSubFolder.getAbsolutePath(), targetDirectory.getAbsolutePath());
                    } catch (IOException e) {
                        log.error("Failed to copy file: {} -> {}", srcSubFolder.getAbsolutePath(), targetDirectory.getAbsolutePath());
                        return;
                    }
                }
            }
        }

        this.depositJobService.jobCompletedBackup(job);
    }

    private void tryToDeleteActualContents(EntityFlowSetting flowSetting, InjectionPathScan injectionPathScanClient, EntityDepositJob job) {
        if (job.isActualContentDeleted()) {
            return;
        }

        String strDeletionOption = flowSetting.getActualContentDeleteOptions();
        EnumActualContentDeletionOptions deletionOptions;
        if (StringUtils.isEmpty(strDeletionOption)) {
            deletionOptions = EnumActualContentDeletionOptions.notDelete;
        } else {
            deletionOptions = EnumActualContentDeletionOptions.valueOf(strDeletionOption);
        }

        //Ignoring
        if (deletionOptions == EnumActualContentDeletionOptions.notDelete) {
            log.info("Do not need to delete, {}, {}", job.getId(), deletionOptions);
            this.depositJobService.jobDeletedActualContent(job);
            return;
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

        // log.info("{}, deletion options: {}, isDeleteActualContent: {}", job.getId(), deletionOptions.name(), isDeleteActualContent);
        if (isDeleteActualContent) {
            File ingestPath = new File(job.getInjectionPath());
            boolean ret = InjectionUtils.deleteFiles(injectionPathScanClient, ingestPath);
            if (ret) {
                this.depositJobService.jobDeletedActualContent(job);
            }
            log.info("{}, deleted: {}", job.getId(), ret);
        }
    }
}
