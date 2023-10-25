package nz.govt.natlib.dashboard.ui.controller;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.app.MainSecurityConfig;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RosettaApi;
import nz.govt.natlib.dashboard.common.metadata.EnumUserRole;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.domain.service.WhitelistSettingService;
import nz.govt.natlib.dashboard.ui.command.UserAccessReqCommand;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class UserAccessController {
    private static final Logger log = LoggerFactory.getLogger(UserAccessController.class);

    @Autowired
    private FreeMarkerConfigurer confFactory;
    @Autowired
    private MainSecurityConfig securityConfig;
    @Autowired
    private RepoFlowSetting repoFlowSetting;
    @Autowired
    private RosettaApi rosettaApi;
    @Autowired
    private WhitelistSettingService whitelistService;

    @RequestMapping(path = DashboardConstants.PATH_USER_LOGIN_API, method = {RequestMethod.GET, RequestMethod.POST})
    public RestResponseCommand login(@RequestBody UserAccessReqCommand cmd, HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        RestResponseCommand rstVal = new RestResponseCommand();

        String pdsHandle = null;
        try {
            pdsHandle = rosettaApi.login("INS00", cmd.getUsername(), cmd.getPassword());
        } catch (Exception e) {
            rstVal.setRspCode(RestResponseCommand.RSP_NETWORK_EXCEPTION);
            rstVal.setRspMsg("Failed to call pds service");
            return rstVal;
        }

        PdsUserInfo pdsUserInfo;
        try {
            pdsUserInfo = rosettaApi.getPdsUserByPdsHandle(pdsHandle);
        } catch (Exception e) {
            rstVal.setRspCode(RestResponseCommand.RSP_NETWORK_EXCEPTION);
            rstVal.setRspMsg("Failed to call pds service");
            return rstVal;
        }

        if (!DashboardHelper.isNull(pdsUserInfo.getUserName())) {
            //To initial the system: save the current user as the first admin user.
            if (whitelistService.isEmptyWhiteList()) {
                EntityWhitelistSetting whitelist = new EntityWhitelistSetting();
                whitelist.setWhiteUserName(pdsUserInfo.getUserName());
                whitelist.setWhiteUserRole(EnumUserRole.admin.name());
                whitelistService.saveWhitelistSetting(whitelist);
            } else if (!whitelistService.isInWhiteList(pdsUserInfo)) {
                rstVal.setRspCode(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
                return rstVal;
            }
        } else {
            rstVal.setRspBody(RestResponseCommand.RSP_LOGIN_ERROR);
            rstVal.setRspMsg("Failed to authenticate the credential.");
            return rstVal;
        }

        pdsUserInfo.setPid(pdsHandle);

        req.getSession().setAttribute(DashboardConstants.KEY_USER_INFO, pdsUserInfo);

        Cookie cookiePdsHandler = new Cookie(DashboardConstants.KEY_PDS_HANDLE, pdsHandle);
        cookiePdsHandler.setMaxAge(60 * 60 * 24 * 365);
        cookiePdsHandler.setPath(req.getContextPath());
        cookiePdsHandler.setHttpOnly(false);
        rsp.addCookie(cookiePdsHandler);

//        rsp.sendRedirect(DashboardConstants.PATH_USER_INDEX_HTML);
        return rstVal;
    }

    @RequestMapping(path = DashboardConstants.PATH_USER_LOGOUT_API, method = {RequestMethod.POST, RequestMethod.GET})
    public void logout(HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        PdsUserInfo userInfo = (PdsUserInfo) req.getSession().getAttribute(DashboardConstants.KEY_USER_INFO);
        if (!DashboardHelper.isNull(userInfo)) {
            rosettaApi.logout(userInfo.getPid());
        }
        req.getSession().invalidate();

        rsp.sendRedirect(req.getContextPath() + DashboardConstants.PATH_USER_LOGIN_HTML);
    }
}
