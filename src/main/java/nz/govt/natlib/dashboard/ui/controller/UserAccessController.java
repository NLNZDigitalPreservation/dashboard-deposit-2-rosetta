package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.auth.HttpAccessManagementFilter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserAccessController {

    @RequestMapping(path = DashboardConstants.PATH_USER_LOGIN_API, method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<?> login() {
        return ResponseEntity.status(HttpStatus.GONE)
                .body("Login is handled by the upstream App Service with Entra ID");
    }

    @RequestMapping(path = DashboardConstants.PATH_USER_LOGOUT_API, method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseEntity<?> logout() {
        // Authentication session is managed by upstream App Service.
        return ResponseEntity.ok().body(true);
    }

    @RequestMapping(path = DashboardConstants.PATH_ROOT + "/auth/me.json", method = { RequestMethod.GET })
    public ResponseEntity<?> me(HttpServletRequest request) {
        Map<String, Object> me = new HashMap<>();
        me.put("username", request.getAttribute(HttpAccessManagementFilter.ATTR_AUTH_USERNAME));
        me.put("displayName", request.getAttribute(HttpAccessManagementFilter.ATTR_AUTH_DISPLAY_NAME));
        me.put("objectId", request.getAttribute(HttpAccessManagementFilter.ATTR_AUTH_OBJECT_ID));
        me.put("role", request.getAttribute(HttpAccessManagementFilter.ATTR_AUTH_ROLE));
        me.put("idToken", request.getAttribute(HttpAccessManagementFilter.ATTR_AUTH_TOKEN));
        return ResponseEntity.ok().body(me);
    }
}
