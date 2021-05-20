package nz.govt.natlib.dashboard.ui.command;

public class DepositJobManualAddCommand {
    Long flowId;
    String flowName;
    String nfsDirectory;
    boolean forcedReplaceExistingJob;

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

    public String getNfsDirectory() {
        return nfsDirectory;
    }

    public void setNfsDirectory(String nfsDirectory) {
        this.nfsDirectory = nfsDirectory;
    }

    public boolean isForcedReplaceExistingJob() {
        return forcedReplaceExistingJob;
    }

    public void setForcedReplaceExistingJob(boolean forcedReplaceExistingJob) {
        this.forcedReplaceExistingJob = forcedReplaceExistingJob;
    }
}
