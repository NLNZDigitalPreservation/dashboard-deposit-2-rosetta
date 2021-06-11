package nz.govt.natlib.dashboard.ui.command;

public class DepositJobSearchCommand {
    private long dtStart;
    private long dtEnd;
    private String[] flowIds;
    private String[] stages;
    private String[] states;

    public long getDtStart() {
        return dtStart;
    }

    public void setDtStart(long dtStart) {
        this.dtStart = dtStart;
    }

    public long getDtEnd() {
        return dtEnd;
    }

    public void setDtEnd(long dtEnd) {
        this.dtEnd = dtEnd;
    }

    public String[] getFlowIds() {
        return flowIds;
    }

    public void setFlowIds(String[] flowIds) {
        this.flowIds = flowIds;
    }

    public String[] getStages() {
        return stages;
    }

    public void setStages(String[] stages) {
        this.stages = stages;
    }

    public String[] getStates() {
        return states;
    }

    public void setStates(String[] states) {
        this.states = states;
    }
}
