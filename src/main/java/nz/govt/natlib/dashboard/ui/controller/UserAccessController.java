package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.metadata.UserInfo;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class UserAccessController {
    private static final Logger log = LoggerFactory.getLogger(UserAccessController.class);
    private static final long EXPIRE_INTERNAL = 30 * 60 * 1000; //30 Minutes
    @Autowired
    private Sessions sessions;
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
        UserInfo userInfo;
        try {
            userInfo = rosettaWebService.login("INS00", cmd.getUsername(), cmd.getPassword());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage() + ":" + RestResponseCommand.RSP_NETWORK_EXCEPTION);
        }

        if (DashboardHelper.isNull(userInfo) || DashboardHelper.isEmpty(userInfo.getUserName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to authenticate the credential " + RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
        }

        if (!isTestMode) {
            //To initial the system: save the current user as the first admin user.
            if (whitelistService.isEmptyWhiteList()) {
                EntityWhitelistSetting whitelist = new EntityWhitelistSetting();
                whitelist.setWhiteUserName(userInfo.getUserName());
                whitelist.setWhiteUserRole(EnumUserRole.admin.name());
                whitelistService.saveWhitelistSetting(whitelist);
            } else if (!whitelistService.isInWhiteList(userInfo)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No privilege: not in the white list " + RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            }
        }

        EntityWhitelistSetting whitelistUser = whitelistService.getUserFromWhiteList(cmd.getUsername());
        if (whitelistUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No privilege: not in the white list " + RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
        }

        try {
            sessions.addSession(userInfo.getSessionId(), whitelistUser.getWhiteUserName(), whitelistUser.getWhiteUserRole(), EXPIRE_INTERNAL, userInfo.getDisplayName());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to authenticate: " + e.getMessage());
        }

        return ResponseEntity.ok().body(userInfo.getSessionId());
    }

    @RequestMapping(path = DashboardConstants.PATH_USER_LOGOUT_API, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> logout(@RequestBody UserAccessReqCommand cmd, HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        sessions.removeSession(cmd.getToken());

        return ResponseEntity.ok().body(true);
    }
}
