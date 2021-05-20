package nz.govt.natlib.dashboard.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sleepycat.persist.model.Persistent;

import java.util.concurrent.TimeUnit;

@Persistent
public class BaseFlowSetting implements InterfaceFlowSetting {
    private String materialFlowId;
    private String materialFlowName;
    private String producerId;
    private String producerName;
    private boolean enabled;
    private String name;
    private String streamLocation;
    private String injectionCompleteFileName;
    private boolean backupEnabled;

    private Long delays;
    private String delayUnit;
    private Long maxActiveDays;
    private Long maxSaveDays;

    private int[] weeklyMaxConcurrency = new int[7];

    private Long injectionEndPointId;
    private Long backupEndPointId;

    public String getMaterialFlowId() {
        return materialFlowId;
    }

    public void setMaterialFlowId(String materialFlowId) {
        this.materialFlowId = materialFlowId;
    }

    public String getMaterialFlowName() {
        return materialFlowName;
    }

    public void setMaterialFlowName(String materialFlowName) {
        this.materialFlowName = materialFlowName;
    }

    public String getProducerId() {
        return producerId;
    }

    public void setProducerId(String producerId) {
        this.producerId = producerId;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInjectionCompleteFileName() {
        return injectionCompleteFileName;
    }

    public void setInjectionCompleteFileName(String injectionCompleteFileName) {
        this.injectionCompleteFileName = injectionCompleteFileName;
    }


    public String getStreamLocation() {
        return streamLocation;
    }

    public void setStreamLocation(String streamLocation) {
        this.streamLocation = streamLocation;
    }

    public boolean isBackupEnabled() {
        return backupEnabled;
    }

    public void setBackupEnabled(boolean backupEnabled) {
        this.backupEnabled = backupEnabled;
    }

    public Long getInjectionEndPointId() {
        return injectionEndPointId;
    }

    public void setInjectionEndPointId(Long injectionEndPointId) {
        this.injectionEndPointId = injectionEndPointId;
    }

    public Long getBackupEndPointId() {
        return backupEndPointId;
    }

    public void setBackupEndPointId(Long backupEndPointId) {
        this.backupEndPointId = backupEndPointId;
    }

    public Long getDelays() {
        return delays;
    }

    public void setDelays(Long delays) {
        this.delays = delays;
    }

    public String getDelayUnit() {
        return delayUnit;
    }

    public void setDelayUnit(String delayUnit) {
        this.delayUnit = delayUnit;
    }

    public int[] getWeeklyMaxConcurrency() {
        return weeklyMaxConcurrency;
    }

    public void setWeeklyMaxConcurrency(int[] weeklyMaxConcurrency) {
        this.weeklyMaxConcurrency = weeklyMaxConcurrency;
    }

    public Long getMaxActiveDays() {
        return maxActiveDays;
    }

    public void setMaxActiveDays(Long maxActiveDays) {
        this.maxActiveDays = maxActiveDays;
    }

    public Long getMaxSaveDays() {
        return maxSaveDays;
    }

    public void setMaxSaveDays(Long maxSaveDays) {
        this.maxSaveDays = maxSaveDays;
    }

    @JsonIgnore
    public TimeUnit getDelayTimeUnit() {
        if (this.delayUnit.equalsIgnoreCase("S")) {
            return TimeUnit.SECONDS;
        } else if (this.delayUnit.equalsIgnoreCase("M")) {
            return TimeUnit.MINUTES;
        } else if (this.delayUnit.equalsIgnoreCase("H")) {
            return TimeUnit.HOURS;
        } else if (this.delayUnit.equalsIgnoreCase("D")) {
            return TimeUnit.DAYS;
        } else {
            return null;
        }
    }

    private Boolean auditRst=true;
    private String auditMsg="OK";

    public Boolean getAuditRst() {
        return auditRst;
    }

    public void setAuditRst(Boolean auditRst) {
        this.auditRst = auditRst;
    }

    public String getAuditMsg() {
        return auditMsg;
    }

    public void setAuditMsg(String auditMsg) {
        this.auditMsg = auditMsg;
    }
}
