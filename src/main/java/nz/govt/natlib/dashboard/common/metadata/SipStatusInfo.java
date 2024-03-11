package nz.govt.natlib.dashboard.common.metadata;

public class SipStatusInfo {
    private String link;
    private String id;
    private String externalId;
    private String externalSystem;
    private String module = "HUB";
    private String stage;
    private String status;
    private String numberOfIEs;
    private String iePids;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalSystem() {
        return externalSystem;
    }

    public void setExternalSystem(String externalSystem) {
        this.externalSystem = externalSystem;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNumberOfIEs() {
        return numberOfIEs;
    }

    public void setNumberOfIEs(String numberOfIEs) {
        this.numberOfIEs = numberOfIEs;
    }

    public String getIePids() {
        return iePids;
    }

    public void setIePids(String iePids) {
        this.iePids = iePids;
    }
}
