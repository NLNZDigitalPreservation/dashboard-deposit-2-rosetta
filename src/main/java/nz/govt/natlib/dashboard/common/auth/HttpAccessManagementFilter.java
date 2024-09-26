package nz.govt.natlib.dashboard.common.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.DashboardConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpAccessManagementFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(HttpAccessManagementFilter.class);
    private Sessions sessions;

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

        String token = req.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            rsp.sendError(HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        try {
            String role = sessions.getRole(token);
            String method = req.getMethod();
            if (!isPermit(role, method, url)) {
                rsp.sendError(HttpStatus.SC_FORBIDDEN);
                return;
            }
        } catch (Sessions.InvalidSessionException e) {
            rsp.sendError(HttpStatus.SC_UNAUTHORIZED);
            return;
        }


        chain.doFilter(request, response);
    }

    public boolean isPermit(String role, String method, String url) {
        if (role.equalsIgnoreCase("admin")) {
            return true;
        }

        if (url.contains("/restful/deposit-jobs/search")) {
            return true;
        }
        return !method.equalsIgnoreCase("POST") && !method.equalsIgnoreCase("PUT") && !method.equalsIgnoreCase("DELETE");
    }

    @Override
    public void destroy() {

    }

    public Sessions getSessions() {
        return sessions;
    }

    public void setSessions(Sessions sessions) {
        this.sessions = sessions;
    }
}