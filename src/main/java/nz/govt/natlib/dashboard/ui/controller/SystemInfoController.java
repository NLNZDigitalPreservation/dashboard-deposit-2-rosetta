package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SystemInfoController {
    // @Autowired
    // private BuildProperties buildProperties;

    @Value("${app.deployment:dev}")
    private String appDeployment;

    @Value("${app.version:1.0.0}")
    private String version;

    @Value("${AuthMode}")
    private String authMode;

    @Value("${entra.tenantId}")
    private String entraTenantId;

    @Value("${entra.clientId}")
    private String entraClientId;

    @Value("${entra.redirectUrl}")
    private String entraRedirectUrl;

    @RequestMapping(path = DashboardConstants.SYSTEM_INFO, method = {RequestMethod.GET})
    public ResponseEntity<?> get() {
        try {
            Map<String, String> sysInfo = new HashMap<>();
            // String version = getClass().getPackage().getImplementationVersion();

            sysInfo.put("version", version);
            sysInfo.put("systemDeployment", appDeployment);
            sysInfo.put("authMode", authMode.toLowerCase());
            sysInfo.put("entraTenantId", entraTenantId);
            sysInfo.put("entraClientId", entraClientId);
            sysInfo.put("entraRedirectUrl", entraRedirectUrl);

            return ResponseEntity.ok().body(sysInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
