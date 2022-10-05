package nz.govt.natlib.dashboard.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.concurrent.TimeUnit;

public class EntityGlobalSetting extends EntityCommon{
    private boolean paused;
    private String pausedStartTime;
    private String pausedEndTime;
    private Long delays;
    private String delayUnit;

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public String getPausedStartTime() {
        return pausedStartTime;
    }

    public void setPausedStartTime(String pausedStartTime) {
        this.pausedStartTime = pausedStartTime;
    }

    public String getPausedEndTime() {
        return pausedEndTime;
    }

    public void setPausedEndTime(String pausedEndTime) {
        this.pausedEndTime = pausedEndTime;
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
