package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.auth.SessionInfo;
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

import java.util.UUID;

@RestController
public class UserAccessController {
    private static final Logger log = LoggerFactory.getLogger(UserAccessController.class);
    private static final String TEST_SESSION_ID = "241200811372143992420081372111";
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
    @Value("${AuthMode}")
    private String authMode;

    private boolean isTestMode() {
        if (DashboardHelper.isEmpty(this.authMode)) {
            return true;
        }
        return this.authMode.equalsIgnoreCase("test");
    }

    private boolean isLdapMode() {
        if (DashboardHelper.isEmpty(this.authMode)) {
            return false;
        }
        return this.authMode.equalsIgnoreCase("ldap");
    }

    private boolean isEntraMode() {
        if (DashboardHelper.isEmpty(this.authMode)) {
            return false;
        }
        return this.authMode.equalsIgnoreCase("entra");
    }

    @RequestMapping(path = DashboardConstants.PATH_USER_LOGIN_API, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> login(@RequestBody UserAccessReqCommand cmd, HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        //Return the TEST SESSION ID
        if (this.isTestMode()) {
            sessions.addSession(TEST_SESSION_ID, "test", "admin", EXPIRE_INTERNAL, "Test");
            return ResponseEntity.ok().body(sessions.getSession(TEST_SESSION_ID));
        }

        String existingUserName;
        try {
            existingUserName = sessions.getUsername(cmd.getToken());
        } catch (Exception e) {
            existingUserName = null;
        }

        //Return true if the session is valid and the username match
        if (!DashboardHelper.isEmpty(existingUserName) && existingUserName.equalsIgnoreCase(cmd.getUsername())) {
            return ResponseEntity.ok().body(cmd.getToken());
        }

        SessionInfo sessionInfo = null;
        if (this.isLdapMode()) {
            UserInfo userInfo;
            try {
                userInfo = rosettaWebService.login("INS00", cmd.getUsername(), cmd.getPassword());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage() + ":" + RestResponseCommand.RSP_NETWORK_EXCEPTION);
            }

            if (DashboardHelper.isNull(userInfo) || DashboardHelper.isEmpty(userInfo.getUserName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to authenticate the credential " + RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            }

            //To initial the system: save the current user as the first admin user.
            if (whitelistService.isEmptyWhiteList()) {
                EntityWhitelistSetting whitelist = new EntityWhitelistSetting();
                whitelist.setWhiteUserName(userInfo.getUserName());
                whitelist.setWhiteUserRole(EnumUserRole.admin.name());
                whitelistService.saveWhitelistSetting(whitelist);
            } else if (!whitelistService.isInWhiteList(userInfo)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No privilege: not in the white list " + RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
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
            sessionInfo = sessions.getSession(userInfo.getSessionId());
        }

        if (this.isEntraMode()) {
            if (DashboardHelper.isEmpty(cmd.getUsername())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to authenticate the credential " + RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            }
            String presentationName = cmd.getPresentationName();
            if (DashboardHelper.isEmpty(presentationName)) {
                presentationName = DashboardHelper.isEmpty(cmd.getEmail()) ? cmd.getUsername() : cmd.getEmail();
            }
            String sessionId = UUID.randomUUID().toString();
            sessions.addSession(sessionId, cmd.getUsername(), "admin", EXPIRE_INTERNAL, presentationName);
            sessionInfo = sessions.getSession(TEST_SESSION_ID);
        }

        if (sessionInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No privilege: not in the white list " + RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
        }

        return ResponseEntity.ok().body(sessionInfo);
    }

    @RequestMapping(path = DashboardConstants.PATH_USER_LOGOUT_API, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> logout(@RequestBody UserAccessReqCommand cmd, HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        sessions.removeSession(cmd.getToken());

        return ResponseEntity.ok().body(true);
    }
}
