package nz.govt.natlib.dashboard.common.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.govt.natlib.dashboard.app.MainSecurityConfig;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.DashboardConstants;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.http.HttpResponse;

public class HttpAccessManagementFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(HttpAccessManagementFilter.class);
    private MainSecurityConfig securityConfig;
    private PrivilegeManagementHandler privilegeManagementHandler;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rsp = ((HttpServletResponse) response);
        String contextUri = req.getContextPath(), reqUri = req.getRequestURI();
        String url = req.getRequestURI().substring(contextUri.length());

        log.debug("contextUri: {}, url: {}", contextUri, url);

        //Only allowed the authentication for restful APIs
        if (!url.startsWith(DashboardConstants.PATH_ROOT)) {
            chain.doFilter(request, response);
            return;
        }

        if (log.isDebugEnabled()) { //The securityConfig.getCurrentSessionMessage(req, rsp) method will consume huge calculation resources, so only execute it when info is available
            log.debug("Before doFilter {}", securityConfig.getCurrentSessionMessage(req, rsp));
        }

       if (!securityConfig.isValidSession(req, rsp)) {
            //responseError(req, rsp, RestResponseCommand.RSP_LOGIN_ERROR);
            rsp.sendError( HttpStatus.SC_UNAUTHORIZED);
           return;
       }

       if (!privilegeManagementHandler.isActionPermit(req, rsp)) {
           //responseError(req, rsp, RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
           rsp.sendError( HttpStatus.SC_UNAUTHORIZED);
           return;
       }

        chain.doFilter(request, response);

        if (log.isDebugEnabled()) { //The securityConfig.getCurrentSessionMessage(req, rsp) method will consume huge calculation resources, so only execute it when info is available
            log.debug("After doFilter {}", securityConfig.getCurrentSessionMessage(req, rsp));
        } else {
            String msg = String.format("%s %d", req.getRequestURI(), rsp.getStatus());
            log.info(msg);
        }
    }

    private void responseError(HttpServletRequest req, HttpServletResponse rsp, int response_code) throws IOException {
        rsp.setContentType("application/json");

        RestResponseCommand rst = new RestResponseCommand();
        rst.setRspCode(response_code);

        String json = "{}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(rst);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        rsp.getWriter().write(json);
    }

    @Override
    public void destroy() {

    }

    public MainSecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    public void setSecurityConfig(MainSecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public PrivilegeManagementHandler getPrivilegeManagementHandler() {
        return privilegeManagementHandler;
    }

    public void setPrivilegeManagementHandler(PrivilegeManagementHandler privilegeManagementHandler) {
        this.privilegeManagementHandler = privilegeManagementHandler;
    }
}