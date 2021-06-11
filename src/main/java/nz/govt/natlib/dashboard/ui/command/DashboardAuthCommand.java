package nz.govt.natlib.dashboard.ui.command;

public class DashboardAuthCommand {
    //User info
    private String userid;
    private String username;
    private String email;
    private String pid;

    //Auth SSO URL
    private String ssoLogoutUrl;
    private String ssoLoginUrl;
    private String ssoLoginFromStartUrl;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getSsoLogoutUrl() {
        return ssoLogoutUrl;
    }

    public void setSsoLogoutUrl(String ssoLogoutUrl) {
        this.ssoLogoutUrl = ssoLogoutUrl;
    }

    public String getSsoLoginUrl() {
        return ssoLoginUrl;
    }

    public void setSsoLoginUrl(String ssoLoginUrl) {
        this.ssoLoginUrl = ssoLoginUrl;
    }

    public String getSsoLoginFromStartUrl() {
        return ssoLoginFromStartUrl;
    }

    public void setSsoLoginFromStartUrl(String ssoLoginFromStartUrl) {
        this.ssoLoginFromStartUrl = ssoLoginFromStartUrl;
    }
}
