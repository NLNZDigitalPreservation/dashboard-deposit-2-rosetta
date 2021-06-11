package nz.govt.natlib.dashboard.domain.entity;


import com.sleepycat.persist.model.Persistent;

@Persistent
public class BaseStorageLocation {
    private String scanMode;
    private String rootPath;
    private String ftpServer;
    private int ftpPort;
    private String ftpUsername;
    private String ftpPassword;
    private boolean ftpProxyEnabled;
    private String ftpProxyHost;
    private int ftpProxyPort;
    private String ftpProxyUsername;
    private String ftpProxyPassword;

    public String getScanMode() {
        return scanMode;
    }

    public void setScanMode(String scanMode) {
        this.scanMode = scanMode;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public String getFtpUsername() {
        return ftpUsername;
    }

    public void setFtpUsername(String ftpUsername) {
        this.ftpUsername = ftpUsername;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    public boolean isFtpProxyEnabled() {
        return ftpProxyEnabled;
    }

    public void setFtpProxyEnabled(boolean ftpProxyEnabled) {
        this.ftpProxyEnabled = ftpProxyEnabled;
    }

    public String getFtpProxyHost() {
        return ftpProxyHost;
    }

    public void setFtpProxyHost(String ftpProxyHost) {
        this.ftpProxyHost = ftpProxyHost;
    }

    public int getFtpProxyPort() {
        return ftpProxyPort;
    }

    public void setFtpProxyPort(int ftpProxyPort) {
        this.ftpProxyPort = ftpProxyPort;
    }

    public String getFtpProxyUsername() {
        return ftpProxyUsername;
    }

    public void setFtpProxyUsername(String ftpProxyUsername) {
        this.ftpProxyUsername = ftpProxyUsername;
    }

    public String getFtpProxyPassword() {
        return ftpProxyPassword;
    }

    public void setFtpProxyPassword(String ftpProxyPassword) {
        this.ftpProxyPassword = ftpProxyPassword;
    }
}
