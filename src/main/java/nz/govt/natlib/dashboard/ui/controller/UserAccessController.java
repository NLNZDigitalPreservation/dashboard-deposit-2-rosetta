package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.metadata.PdsUserInfo;
import nz.govt.natlib.dashboard.app.MainSecurityConfig;
import nz.govt.natlib.dashboard.common.auth.Sessions;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.common.metadata.EnumUserRole;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.domain.service.WhitelistSettingService;
import nz.govt.natlib.dashboard.ui.command.UserAccessReqCommand;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

//import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class UserAccessController {
    private static final Logger log = LoggerFactory.getLogger(UserAccessController.class);
    private static final long EXPIRE_INTERNAL = 30 * 60 * 1000; //30 Minutes
    @Autowired
    private Sessions sessions;
    @Autowired
    private FreeMarkerConfigurer confFactory;
    @Autowired
    private MainSecurityConfig securityConfig;
    @Autowired
    private RepoFlowSetting repoFlowSetting;
    @Autowired
    private RosettaWebService rosettaWebService;
    @Autowired
    private WhitelistSettingService whitelistService;
    @Value("${TestEnabled}")
    private boolean isTestMode;

    @RequestMapping(path = DashboardConstants.PATH_USER_LOGIN_API, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> login(@RequestBody UserAccessReqCommand cmd, HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        String pdsHandle = null;
        try {
            pdsHandle = rosettaWebService.login("INS00", cmd.getUsername(), cmd.getPassword());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage() + ":" + RestResponseCommand.RSP_NETWORK_EXCEPTION);
        }

        PdsUserInfo pdsUserInfo;
        try {
            pdsUserInfo = rosettaWebService.getPdsUserByPdsHandle(pdsHandle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage() + ":" + RestResponseCommand.RSP_NETWORK_EXCEPTION);
        }

        if (!isTestMode) {
            if (!DashboardHelper.isNull(pdsUserInfo.getUserName())) {
                //To initial the system: save the current user as the first admin user.
                if (whitelistService.isEmptyWhiteList()) {
                    EntityWhitelistSetting whitelist = new EntityWhitelistSetting();
                    whitelist.setWhiteUserName(pdsUserInfo.getUserName());
                    whitelist.setWhiteUserRole(EnumUserRole.admin.name());
                    whitelistService.saveWhitelistSetting(whitelist);
                } else if (!whitelistService.isInWhiteList(pdsUserInfo)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No privilege: not in the white list " + RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to authenticate the credential " + RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            }
        }

        pdsUserInfo.setPid(pdsHandle);
        EntityWhitelistSetting whitelistUser = whitelistService.getUserFromWhiteList(cmd.getUsername());
        if (whitelistUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No privilege: not in the white list " + RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
        }

        try {
            sessions.addSession(pdsHandle, whitelistUser.getWhiteUserName(), whitelistUser.getWhiteUserRole(), EXPIRE_INTERNAL);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to authenticate: " + e.getMessage());
        }

        return ResponseEntity.ok().body(pdsHandle);
    }

    @RequestMapping(path = DashboardConstants.PATH_USER_LOGOUT_API, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> logout(@RequestBody UserAccessReqCommand cmd, HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        sessions.removeSession(cmd.getToken());

        return ResponseEntity.ok().body(true);
    }
}
