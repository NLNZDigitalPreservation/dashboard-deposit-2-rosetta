package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.domain.daemon.TimerScheduledExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class SystemManagementController {
    private static final Logger log = LoggerFactory.getLogger(SystemManagementController.class);

    @Autowired
    private TimerScheduledExecutors timerScheduledExecutors;

    @RequestMapping(path = "/release", method = {RequestMethod.GET, RequestMethod.POST})
    public String release(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        String reqAddress = req.getRemoteAddr();
        if (!reqAddress.equals("127.0.0.1") && !reqAddress.equals("0:0:0:0:0:0:0:1") && !reqAddress.equals("localhost")) {
            rsp.getWriter().write("You can only shutdown the service from local machine.");
            return "You can only shutdown the service from local machine.";
        }
        log.info("Received [shutdown] command {}", req.getParameter("Referer"));
        log.info("Dashboard is going to exit.");
        timerScheduledExecutors.stopTimer();
        return "The system resources held by Dashboard were released.";
    }
}
