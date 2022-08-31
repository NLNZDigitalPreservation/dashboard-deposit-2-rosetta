package nz.govt.natlib.dashboard.domain.service;

import com.exlibris.dps.SipStatusInfo;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebServiceImpl;
import nz.govt.natlib.dashboard.common.injection.*;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.*;
import nz.govt.natlib.dashboard.domain.repo.*;
import nz.govt.natlib.dashboard.ui.command.DepositJobSearchCommand;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import nz.govt.natlib.dashboard.common.metadata.MetsHandler;
import nz.govt.natlib.dashboard.common.metadata.MetsXmlProperties;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Service
public class DepositJobService implements InterfaceFlowSetting, InterfaceMappingDTO {
    //    private static final String INJECTION_DONE_FILE_NAME = "ready-for-ingestion-FOLDER-COMPLETED";
    private static final Logger log = LoggerFactory.getLogger(DepositJobService.class);
    private static final Map<Long, Semaphore> DB_JOB_TOKENS = new HashMap<>();
    @Autowired
    private RosettaWebServiceImpl rosettaWebService;
    @Autowired
    private RepoDepositJob repoJob;
    @Autowired
    private RepoFlowSetting repoFlowSetting;

    public EntityDepositJob jobInitial(String injectionPath, String injectionTitle, EntityFlowSetting flowSetting) {
        EntityDepositJob job = new EntityDepositJob();
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setInjectionPath(injectionPath);
        job.setInjectionTitle(injectionTitle);
        job.setInitialTime(nowDatetime);
        job.setLatestTime(nowDatetime);
        job.setDepositSetId("1");
        job.setStage(EnumDepositJobStage.INGEST);
        job.setState(EnumDepositJobState.RUNNING);

        job.setAppliedFlowSetting(flowSetting);

        repoJob.save(job);
        return job;
    }

    public EntityDepositJob jobUpdateFilesStat(EntityDepositJob job, long fileCount, long fileSize) {
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        job.setFileCount(fileCount);
        job.setFileSize(fileSize);
        repoJob.save(job);
        return job;
    }

    public EntityDepositJob jobScanComplete(EntityDepositJob job) {
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.INITIALED);
        repoJob.save(job);
        return job;
    }

    public void jobDepositAccept(EntityDepositJob job, String sipId, EntityFlowSetting flowSetting) {
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        job.setDepositStartTime(nowDatetime);
        job.setSipID(sipId);
        job.setSuccessful(true);
        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.RUNNING);
        repoJob.save(job);
    }

    public void jobDepositReject(EntityDepositJob job, String error) {
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        job.setDepositStartTime(nowDatetime);
        job.setResultMessage(error);
        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.FAILED);
        repoJob.save(job);
    }

    public EntityDepositJob jobUpdateStatus(EntityDepositJob job, SipStatusInfo sipStatusInfo) {
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        job.setSipModule(sipStatusInfo.getModule());
        job.setSipStage(sipStatusInfo.getStage());
        job.setSipStatus(sipStatusInfo.getStatus());
        job.setState(getStateFromSipStatusInfo(sipStatusInfo));
//        if (job.getState() == EnumDepositJobState.SUCCEED) {
//            job.setStage(EnumDepositJobStage.FINALIZE); //Going to next step
//            job.setState(EnumDepositJobState.INITIALED);
//        }
        repoJob.save(job);
        return job;
    }

    public EntityDepositJob jobUpdateStatus(EntityDepositJob job, EnumDepositJobStage stage, EnumDepositJobState state) {
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        if (stage != null) {
            job.setStage(stage);
        }
        if (state != null) {
            job.setState(state);
        }
        job.setResultMessage("");
        repoJob.save(job);
        return job;
    }

    public EntityDepositJob jobUpdateStatus(EntityDepositJob job, EnumDepositJobState state) {
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        if (state != null) {
            job.setState(state);
        }
        job.setResultMessage("");
        repoJob.save(job);
        return job;
    }

    public EntityDepositJob retry(EntityDepositJob job) {
        return jobUpdateStatus(job, EnumDepositJobState.INITIALED);
    }

    public EntityDepositJob pause(EntityDepositJob job) {
        if (!isPauseAble(job)) {
            log.warn("{} at stage [{}] and state [{}] could not be paused", job.getInjectionTitle(), job.getStage(), job.getState());
            return job;
        }
        return jobUpdateStatus(job, EnumDepositJobState.PAUSED);
    }

    private boolean isPauseAble(EntityDepositJob job) {
        return (job.getStage() != EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.RUNNING) || (job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.INITIALED);
    }

    public EntityDepositJob resume(EntityDepositJob job) {
        if (job.getState() != EnumDepositJobState.PAUSED) {
            log.warn("{} at stage [{}] and state [{}] could not be resumed", job.getInjectionTitle(), job.getStage(), job.getState());
            return job;
        }
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);

        if (job.getStage() == EnumDepositJobStage.INGEST || job.getStage() == EnumDepositJobStage.FINALIZE) {
            job.setState(EnumDepositJobState.RUNNING);
        } else {
            job.setState(EnumDepositJobState.INITIALED);
        }

        job.setResultMessage("");
        repoJob.save(job);
        return job;
    }

    public EntityDepositJob terminate(EntityDepositJob job) {
        if ((job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.SUCCEED) ||
                (job.getStage() == EnumDepositJobStage.FINALIZE && job.getState() == EnumDepositJobState.SUCCEED)) {
            log.warn("{} at stage [{}] and state [{}] could not be terminated", job.getInjectionTitle(), job.getStage(), job.getState());
            return job;
        }
//        String pathJob = job.getInjectionPath();
//        EntityFlowSetting flowSetting = job.getAppliedFlowSetting();
//        String injectionCompleteFileName = flowSetting == null ? "ready-for-ingestion-FOLDER-COMPLETED" : flowSetting.getInjectionCompleteFileName();
//        File injectFile = new File(pathJob, injectionCompleteFileName);
//        if (injectFile.exists() && injectFile.isFile()) {
//            boolean rst = injectFile.delete();
//            log.debug("Deleted injectionCompleteFile: {}, {}", injectFile.getAbsolutePath(), rst);
//        }
//        File doneFile = new File(pathJob, "DONE");
//        if (doneFile.exists() && doneFile.isFile()) {
//            boolean rst = doneFile.delete();
//            log.debug("Deleted DONE file: {}, {}", doneFile.getAbsolutePath(), rst);
//        }
        repoJob.deleteById(job.getId());
        return job;
    }

    public EntityDepositJob cancel(EntityDepositJob job) {
        if (job.getStage() != EnumDepositJobStage.DEPOSIT || job.getState() != EnumDepositJobState.FAILED) {
            log.warn("{} at stage [{}] and state [{}] could not be canceled", job.getInjectionTitle(), job.getStage(), job.getState());
            return job;
        }
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        job.setStage(EnumDepositJobStage.FINISHED);
        job.setState(EnumDepositJobState.CANCELED);
        job.setResultMessage("");
        repoJob.save(job);
        return job;
    }

    public void jobDepositFinished(EntityDepositJob job) {
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        job.setDepositEndTime(nowDatetime);
        repoJob.save(job);
    }

    public void jobFinalizeStart(EntityDepositJob job) {
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        job.setStage(EnumDepositJobStage.FINALIZE);
        job.setState(EnumDepositJobState.RUNNING);
        repoJob.save(job);
    }

    public void jobFinalizeEnd(EntityDepositJob job, EnumDepositJobState state) {
        long nowDatetime = DashboardHelper.getLocalCurrentMilliSeconds();
        job.setLatestTime(nowDatetime);
        job.setStage(EnumDepositJobStage.FINISHED);
        job.setState(state);
        repoJob.save(job);
    }


    private EnumDepositJobState getStateFromSipStatusInfo(SipStatusInfo sipStatusInfo) {
        String module = sipStatusInfo.getModule();
        String stage = sipStatusInfo.getStage();
        String status = sipStatusInfo.getStatus();

        if (module == null || stage == null || status == null) {
            return EnumDepositJobState.RUNNING;
        }

        //Take the "DECLINED" status at any stages as failed
        if (status.equalsIgnoreCase("DECLINED")) {
            return EnumDepositJobState.FAILED;
        }

        if (stage.equalsIgnoreCase("Finished")) {
            if (status.equalsIgnoreCase("REJECTED") ||
                    status.equalsIgnoreCase("DECLINED") ||
                    status.equalsIgnoreCase("ERROR") ||
                    status.equalsIgnoreCase("DELETED") ||
                    status.equalsIgnoreCase("REJECT_CONTENT")) {
                return EnumDepositJobState.FAILED;
            } else {
                return EnumDepositJobState.SUCCEED;
            }
        } else {
            return EnumDepositJobState.RUNNING;
        }
    }

    public boolean isInjectionReady(InjectionPathScan injectionPathScanClient, String injectionPath) {
        List<UnionFile> progressFiles = injectionPathScanClient.listFile(injectionPath);
        for (UnionFile f : progressFiles) {
            if (f.getName().equalsIgnoreCase("ready-for-ingestion-FOLDER-COMPLETED")) {
                return true;
            }
        }

        InputStream inputStream = injectionPathScanClient.readFile(injectionPath + File.separator + "content", "mets.xml");
        if (inputStream == null) {
            return false;
        }
        MetsXmlProperties prop = MetsHandler.parse(inputStream);
        if (prop == null || prop.getListFiles() == null || prop.getListFiles().size() == 0) {
            return false;
        }

        List<UnionFile> streamFiles = injectionPathScanClient.listFile(injectionPath + File.separator + "content" + File.separator + "streams");
        if (streamFiles == null || streamFiles.size() == 0) {
            return false;
        }

        final Map<String, UnionFile> mapStreamFiles = new HashMap<>();
        for (UnionFile f : streamFiles) {
            mapStreamFiles.put(f.getName(), f);
        }

        for (MetsXmlProperties.GeneralFileCharacters generalFileCharacters : prop.getListFiles()) {
            if (!mapStreamFiles.containsKey(generalFileCharacters.getFileOriginalName())) {
                return false;
            }
            long actualFileSize = Long.parseLong(generalFileCharacters.getFileSizeBytes());
            if (mapStreamFiles.get(generalFileCharacters.getFileOriginalName()).getSize() < actualFileSize) {
                return false;
            }
        }

        return true;
    }

//    public boolean isDepositDone(InjectionPathScan injectionPathScanClient, String injectionPath) {
//        List<UnionFile> progressFiles = injectionPathScanClient.listFile(injectionPath);
//        for (UnionFile f : progressFiles) {
//            if (f.getName().equalsIgnoreCase("done")) {
//                return true;
//            }
//        }
//
//        InputStream inputStream = injectionPathScanClient.readFile(injectionPath + File.separator + "content", "mets.xml");
//        if (inputStream == null) {
//            return false;
//        }
//        MetsXmlProperties prop = MetsHandler.parse(inputStream);
//        if (prop == null) {
//            return false;
//        }
//
//        int num = rosettaWebService.getNumberOfRecords(prop);
//        return num == 1;
//    }

    public RestResponseCommand manuallySubmitDepositJob(Long flowId, String sourceNfsDirectory, boolean isForceReplaceExistingJob) {
        RestResponseCommand retVal = new RestResponseCommand();

        EntityFlowSetting flowSetting = repoFlowSetting.getById(flowId);
        //Checking is input sourceNfsDirectory duplicated
        if (flowSetting == null || !flowSetting.isEnabled()) {
            String msg = String.format("%s flow setting does not exist or is disabled", flowId);
            log.warn(msg);
            retVal.setRspMsg(msg);
            retVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            return retVal;
        }

        File fSubFolder = new File(sourceNfsDirectory);
        if (!fSubFolder.exists()) {
            String msg = String.format("%s source file does not exist", sourceNfsDirectory);
            log.warn(msg);
            retVal.setRspMsg(msg);
            retVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            return retVal;
        }

        //Checking is sourceNfsDirectory valid and is ready
        InjectionPathScan targetInjectionPathScanClient = InjectionUtils.createPathScanClient(flowSetting.getRootPath());

        //
        EntityDepositJob job = repoJob.getByFlowIdAndInjectionTitle(flowId, fSubFolder.getName());

        File targetSubFolder = new File(flowSetting.getRootPath(), fSubFolder.getName());

        boolean isExisting = targetInjectionPathScanClient.exists(targetSubFolder.getAbsolutePath()) || job != null;
        if (isExisting && !isForceReplaceExistingJob) {
            String msg = String.format("%s folder does exist in: %s, and can not be replaced", fSubFolder.getName(), flowSetting.getRootPath());
            log.error(msg);
            retVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            retVal.setRspMsg(msg);
            return retVal;
        }

        //If existing and allow replaced
        if (isExisting) {
            InjectionUtils.deleteFiles(targetInjectionPathScanClient, targetSubFolder);
            if (job != null) {
                repoJob.deleteById(job.getId());
            }
        }

        //The upload job in the flow root location
        if (!fSubFolder.getParent().equalsIgnoreCase(flowSetting.getRootPath())) {
            //Copying the input sourceNfsDirectory to root location of the Flow
            cascadeCopyFiles(fSubFolder.getParentFile(), targetInjectionPathScanClient, fSubFolder, flowSetting);
//            FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
        }

        UnionPath completeInjectFile = new UnionPath(targetSubFolder, flowSetting.getInjectionCompleteFileName());
        if (!targetInjectionPathScanClient.exists(completeInjectFile.getAbsolutePath())) {
            ByteArrayInputStream completeFileInputStream = new ByteArrayInputStream(new byte[0]);
            targetInjectionPathScanClient.copy(completeFileInputStream, completeInjectFile);
            try {
                completeFileInputStream.close();
            } catch (IOException e) {
                log.error("Failed to upload: {}", flowSetting.getInjectionCompleteFileName(), e);
                retVal.setRspCode(RestResponseCommand.RSP_SYSTEM_ERROR);
                retVal.setRspMsg(e.getMessage());
                return retVal;
            }
        }

        return retVal;
    }

    private void cascadeCopyFiles(File sourceRootDir, InjectionPathScan targetInjectionPathScanClient, File curFile, EntityFlowSetting flowSetting) {
        log.debug("Copying: {}", curFile.getAbsolutePath());

        int lenSourceRootDir = sourceRootDir.getAbsolutePath().length();
        String curFileName = curFile.getAbsolutePath().substring(lenSourceRootDir + 1);

        if (curFile.isFile()) {
            if (!curFile.getName().equalsIgnoreCase(flowSetting.getInjectionCompleteFileName())) {
                targetInjectionPathScanClient.copy(UnionPath.of(curFile), new UnionPath(targetInjectionPathScanClient.getRootPath(), curFileName));
            }
        } else {
            targetInjectionPathScanClient.mkdirs(new UnionPath(targetInjectionPathScanClient.getRootPath(), curFileName));

            File[] files = curFile.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                cascadeCopyFiles(sourceRootDir, targetInjectionPathScanClient, f, flowSetting);
            }
        }
    }

    //Query deposit jobs by input condition
    public List<EntityDepositJob> searchDepositJobs(DepositJobSearchCommand cmd) {
        LocalDateTime ldtStart = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(cmd.getDtStart());
        ldtStart = ldtStart.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime ldtEnd = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(cmd.getDtEnd());
        ldtEnd = ldtEnd.withHour(23).withMinute(59).withSecond(59);

        log.debug("Search from {} to {}", ldtStart, ldtEnd);

        long lStart = DashboardHelper.getLocalMilliSeconds(ldtStart);
        long lEnd = DashboardHelper.getLocalMilliSeconds(ldtEnd);


        List<EntityDepositJob> listJobs = repoJob.getByLatestTime(lStart, lEnd);

        listJobs = listJobs.stream().filter(e -> {
            return isContains(cmd.getFlowIds(), e.getAppliedFlowSetting().getId().toString());
        }).filter(e -> {
            return isContains(cmd.getStages(), e.getStage().name());
        }).filter(e -> {
            return isContains(cmd.getStates(), e.getState().name());
        }).collect(Collectors.toList());

        return listJobs;
    }

    private static boolean isContains(String[] conditions, String item) {
        if (conditions == null || conditions.length == 0) {
            return true;
        }

        for (String condition : conditions) {
            if (condition.equals(item)) {
                return true;
            }
        }

        return false;
    }

    //    Export selected jobs to excel document
    public void exportData(List<Long> reqJobIDs, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        Resource resource = new ClassPathResource("deposit-jobs-template.xlsx");
        Workbook workbook = new XSSFWorkbook(resource.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        int rowIndex = 1;
        for (Long jobId : reqJobIDs) {
            EntityDepositJob job = repoJob.getById(jobId);
            if (job == null) {
                continue;
            }

            Row rowExcel = sheet.createRow(rowIndex++);

            Cell cId = rowExcel.createCell(0, CellType.NUMERIC);
            cId.setCellValue(job.getId());

            Cell cFlow = rowExcel.createCell(1, CellType.STRING);
            EntityFlowSetting flowSetting = job.getAppliedFlowSetting();
            if (flowSetting != null) {
                cFlow.setCellValue(flowSetting.getMaterialFlowId() + "-" + flowSetting.getMaterialFlowName());
            } else {
                cFlow.setCellValue("Unknown Material Flow");
            }

            Cell cTitle = rowExcel.createCell(2, CellType.STRING);
            cTitle.setCellValue(job.getInjectionTitle());

            Cell cSubFolder = rowExcel.createCell(3, CellType.STRING);
            cSubFolder.setCellValue(job.getInjectionPath());

            Cell cStatus = rowExcel.createCell(4, CellType.STRING);
            cStatus.setCellValue(job.getStage() + "-" + job.getState());

            LocalDateTime ldtStartTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getDepositStartTime());
            Cell cStartTime = rowExcel.createCell(5, CellType.STRING);
            cStartTime.setCellValue(ldtStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            LocalDateTime ldtLatestUpdateTime = DashboardHelper.getLocalDateTimeFromEpochMilliSecond(job.getDepositStartTime());
            Cell cLatestUpdateTime = rowExcel.createCell(6, CellType.STRING);
            cLatestUpdateTime.setCellValue(ldtLatestUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            Cell cNumOfFiles = rowExcel.createCell(7, CellType.NUMERIC);
            cNumOfFiles.setCellValue(job.getFileCount());

            Cell cSizeOfFiles = rowExcel.createCell(8, CellType.NUMERIC);
            cSizeOfFiles.setCellValue(job.getFileSize());

            Cell cSipId = rowExcel.createCell(9, CellType.NUMERIC);
            cSipId.setCellValue(job.getSipID());

            Cell cSipModule = rowExcel.createCell(10, CellType.STRING);
            cSipModule.setCellValue(job.getSipModule());

            Cell cSipStage = rowExcel.createCell(11, CellType.STRING);
            cSipStage.setCellValue(job.getSipStage());

            Cell cSipStatus = rowExcel.createCell(12, CellType.STRING);
            cSipStatus.setCellValue(job.getSipStatus());

            Cell cSipResult = rowExcel.createCell(13, CellType.STRING);
            cSipResult.setCellValue(job.getResultMessage());
        }
        rsp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        workbook.write(rsp.getOutputStream());
        workbook.close();
    }
}
