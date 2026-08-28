package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.ui.command.HealthStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class HealthCheckController {
    @RequestMapping(path = { DashboardConstants.PATH_HEALTH_CHECK,
            DashboardConstants.PATH_HEALTH_CHECK + "/" }, method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<HealthStatus> release(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        HealthStatus healthStatus = new HealthStatus("healthy", "Deposit Dashboard");
        return ResponseEntity.ok(healthStatus);
    }
}
