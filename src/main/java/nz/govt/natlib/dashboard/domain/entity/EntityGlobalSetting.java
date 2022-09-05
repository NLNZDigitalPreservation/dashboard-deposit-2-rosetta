package nz.govt.natlib.dashboard.domain.entity;

public class EntityGlobalSetting extends EntityCommon{
    private boolean paused;
    private String pausedStartTime;
    private String pausedEndTime;

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
}
