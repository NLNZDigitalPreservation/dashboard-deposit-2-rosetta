package nz.govt.natlib.dashboard.common.metadata;


public class PdsUserInfo {
    private String borPermission;
    private String borGroup;
    private String courseEnrollment;
    private String userId;
    private String userName;
    private String userIp;
    private String expiryDate;
    private String borGroupM;
    private String borDeptM;
    private String borTuplesM;
    private String courseEnrollmentM;
    private String pid;

    public PdsUserInfo() {
    }

    public String getBorPermission() {
        return this.borPermission;
    }

    public void setBorPermission(String borPermission) {
        this.borPermission = borPermission;
    }

    public String getBorGroup() {
        return this.borGroup;
    }

    public void setBorGroup(String borGroup) {
        this.borGroup = borGroup;
    }

    public String getCourseEnrollment() {
        return this.courseEnrollment;
    }

    public void setCourseEnrollment(String courseEnrollment) {
        this.courseEnrollment = courseEnrollment;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIp() {
        return this.userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getBorGroupM() {
        return this.borGroupM;
    }

    public void setBorGroupM(String borGroupM) {
        this.borGroupM = borGroupM;
    }

    public String getBorDeptM() {
        return this.borDeptM;
    }

    public void setBorDeptM(String borDeptM) {
        this.borDeptM = borDeptM;
    }

    public String getBorTuplesM() {
        return this.borTuplesM;
    }

    public void setBorTuplesM(String borTuplesM) {
        this.borTuplesM = borTuplesM;
    }

    public String getCourseEnrollmentM() {
        return this.courseEnrollmentM;
    }

    public void setCourseEnrollmentM(String courseEnrollmentM) {
        this.courseEnrollmentM = courseEnrollmentM;
    }

    public String getPid() {
        return this.pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
