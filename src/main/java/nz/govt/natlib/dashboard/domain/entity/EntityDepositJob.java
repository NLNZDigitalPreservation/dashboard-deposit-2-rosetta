package nz.govt.natlib.dashboard.domain.entity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;

@Entity
public class EntityDepositJob {
    @PrimaryKey
    private Long id;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private Long initialTime;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private Long latestTime;
    private Long depositStartTime;
    private Long depositEndTime;

    private String injectionPath;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
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

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private Long flowId;
    private String flowName;

    private String depositSetId;
    private String resultMessage;

    private DTOFlowSetting appliedFlowSetting;
    private DTOStorageLocation appliedInjectionStorageLocation;
    private DTOStorageLocation appliedBackupStorageLocation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
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

    public BaseFlowSetting getAppliedFlowSetting() {
        return appliedFlowSetting;
    }

    public void setAppliedFlowSetting(DTOFlowSetting appliedFlowSetting) {
        this.appliedFlowSetting = appliedFlowSetting;
    }

    public DTOStorageLocation getAppliedInjectionStorageLocation() {
        return appliedInjectionStorageLocation;
    }

    public void setAppliedInjectionStorageLocation(DTOStorageLocation appliedInjectionStorageLocation) {
        this.appliedInjectionStorageLocation = appliedInjectionStorageLocation;
    }

    public DTOStorageLocation getAppliedBackupStorageLocation() {
        return appliedBackupStorageLocation;
    }

    public void setAppliedBackupStorageLocation(DTOStorageLocation appliedBackupStorageLocation) {
        this.appliedBackupStorageLocation = appliedBackupStorageLocation;
    }
}
