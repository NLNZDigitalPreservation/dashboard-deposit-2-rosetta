package nz.govt.natlib.dashboard.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.concurrent.TimeUnit;

public class EntityFlowSetting extends EntityCommon{
    private boolean enabled;
    private Long depositAccountId;
    private String materialFlowId;
    private String materialFlowName;
    private String producerId;
    private String producerName;
    private String rootPath;

    private String streamLocation;
    private String injectionCompleteFileName;


    private Long delays;
    private String delayUnit;
    private Long maxActiveDays;
    private Long maxSaveDays;

    private int[] weeklyMaxConcurrency = new int[7];


    public Long getDepositAccountId() {
        return depositAccountId;
    }

    public void setDepositAccountId(Long depositAccountId) {
        this.depositAccountId = depositAccountId;
    }

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

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
            return TimeUnit.SECONDS;
        }
    }
}
