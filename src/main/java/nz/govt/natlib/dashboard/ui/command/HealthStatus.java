package nz.govt.natlib.dashboard.ui.command;

public class HealthStatus {
    private String status;
    private String service;

    public HealthStatus(String status, String service) {
        this.status = status;
        this.service = service;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
