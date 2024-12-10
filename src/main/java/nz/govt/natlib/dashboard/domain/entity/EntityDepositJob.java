package nz.govt.natlib.dashboard.domain.entity;


import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;

public class EntityDepositJob extends EntityCommon {
    private Long initialTime;
    private Long latestTime;
    private Long depositStartTime;
    private Long depositEndTime;
    private Long finalizedTime;
    private Long finishedTime;

    private String injectionPath;
    private String injectionTitle;

    private long fileCount;
    private long fileSize;

    private boolean isSuccessful;

    private String sipID;
    private String sipModule;
    private String sipStage;
    private String sipStatus;

    private EnumDepositJobStage stage;
    private EnumDepositJobState state;

    private String depositSetId;
    private String resultMessage;

    private EntityFlowSetting appliedFlowSetting;

    private boolean actualContentDeleted = false;
    private boolean backupCompleted = false;


    public Long getInitialTime() {
        return initialTime;
    }

    public void setInitialTime(Long initialTime) {
        this.initialTime = initialTime;
    }

    public Long getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(Long latestTime) {
        this.latestTime = latestTime;
    }

    public Long getDepositStartTime() {
        return depositStartTime;
    }

    public void setDepositStartTime(Long depositStartTime) {
        this.depositStartTime = depositStartTime;
    }

    public Long getDepositEndTime() {
        return depositEndTime;
    }

    public void setDepositEndTime(Long depositEndTime) {
        this.depositEndTime = depositEndTime;
    }

    public Long getFinalizedTime() {
        return finalizedTime;
    }

    public void setFinalizedTime(Long finalizedTime) {
        this.finalizedTime = finalizedTime;
    }

    public Long getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(Long finishedTime) {
        this.finishedTime = finishedTime;
    }

    public String getInjectionPath() {
        return injectionPath;
    }

    public void setInjectionPath(String injectionPath) {
        this.injectionPath = injectionPath;
    }

    public String getInjectionTitle() {
        return injectionTitle;
    }

    public void setInjectionTitle(String injectionTitle) {
        this.injectionTitle = injectionTitle;
    }

    public long getFileCount() {
        return fileCount;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public String getSipID() {
        return sipID;
    }

    public void setSipID(String sipID) {
        this.sipID = sipID;
    }

    public String getSipModule() {
        return sipModule;
    }

    public void setSipModule(String sipModule) {
        this.sipModule = sipModule;
    }

    public String getSipStage() {
        return sipStage;
    }

    public void setSipStage(String sipStage) {
        this.sipStage = sipStage;
    }

    public String getSipStatus() {
        return sipStatus;
    }

    public void setSipStatus(String sipStatus) {
        this.sipStatus = sipStatus;
    }

    public EnumDepositJobStage getStage() {
        return stage;
    }

    public void setStage(EnumDepositJobStage stage) {
        this.stage = stage;
    }

    public EnumDepositJobState getState() {
        return state;
    }

    public void setState(EnumDepositJobState state) {
        this.state = state;
    }

    public String getDepositSetId() {
        return depositSetId;
    }

    public void setDepositSetId(String depositSetId) {
        this.depositSetId = depositSetId;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public EntityFlowSetting getAppliedFlowSetting() {
        return appliedFlowSetting;
    }

    public void setAppliedFlowSetting(EntityFlowSetting appliedFlowSetting) {
        this.appliedFlowSetting = appliedFlowSetting;
    }

    public boolean isActualContentDeleted() {
        return actualContentDeleted;
    }

    public void setActualContentDeleted(boolean actualContentDeleted) {
        this.actualContentDeleted = actualContentDeleted;
    }

    public boolean isBackupCompleted() {
        return backupCompleted;
    }

    public void setBackupCompleted(boolean backupCompleted) {
        this.backupCompleted = backupCompleted;
    }
}
