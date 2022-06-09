package nz.govt.natlib.dashboard.common.auth;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.metadata.EnumUserRole;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoWhiteList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PrivilegeManagementHandler {
    private final static String[] NEED_AUTHENTICATE_URLS = {
            DashboardConstants.PATH_SETTING_FLOW_SAVE,
            DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_SAVE,
            DashboardConstants.PATH_SETTING_WHITELIST_SAVE,
            DashboardConstants.PATH_SETTING_FLOW_DELETE,
            DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_DELETE,
            DashboardConstants.PATH_SETTING_WHITELIST_DELETE,
            DashboardConstants.PATH_DEPOSIT_JOBS_NEW,
            DashboardConstants.PATH_DEPOSIT_JOBS_UPDATE
    };

    private RepoWhiteList repoWhiteList;

    public PdsUserInfo getPdsUserInfo(HttpServletRequest req, HttpServletResponse rsp) {
        HttpSession hs = req.getSession();
        PdsUserInfo userInfo = (PdsUserInfo) hs.getAttribute(DashboardConstants.KEY_USER_INFO);
        return userInfo;
    }

    public boolean isActionPermit(HttpServletRequest req, HttpServletResponse rsp) {
        String contextUri = req.getContextPath(), reqUri = req.getRequestURI();
        String url = req.getRequestURI().substring(contextUri.length());
        boolean isNeedAuthenticate = false;
        for (String x : NEED_AUTHENTICATE_URLS) {
            if (url.startsWith(x)) {
                isNeedAuthenticate = true;
                break;
            }
        }

        if (!isNeedAuthenticate) {
            return true;
        }

        PdsUserInfo userInfo = getPdsUserInfo(req, rsp);
        EntityWhitelistSetting whitelistUser = repoWhiteList.getByUserName(userInfo.getUserName());
        return (EnumUserRole.valueOf(whitelistUser.getWhiteUserRole()) == EnumUserRole.admin);
    }

    public RepoWhiteList getRepoWhiteList() {
        return repoWhiteList;
    }

    public void setRepoWhiteList(RepoWhiteList repoWhiteList) {
        this.repoWhiteList = repoWhiteList;
    }
}
