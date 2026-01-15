package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SystemInfoController {
    @Autowired
    private BuildProperties buildProperties;

    @Value("${system.deployment:dev}")
    private String systemDeployment;

    @RequestMapping(path = DashboardConstants.SYSTEM_INFO, method = {RequestMethod.GET})
    public ResponseEntity<?> get() {
        try {
            Map<String, String> sysInfo = new HashMap<>();
            sysInfo.put("version", buildProperties.getVersion());
            sysInfo.put("systemDeployment", systemDeployment);
            return ResponseEntity.ok().body(sysInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
