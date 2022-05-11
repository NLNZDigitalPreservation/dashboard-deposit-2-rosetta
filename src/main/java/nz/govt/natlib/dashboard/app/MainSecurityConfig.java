package nz.govt.natlib.dashboard.app;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.auth.HttpAccessManagementFilter;
import nz.govt.natlib.dashboard.common.core.RosettaWebServiceImpl;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.catalina.session.StandardSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import javax.servlet.http.*;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class MainSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(MainSecurityConfig.class);
    private final String SESSION_LOCK = "lock";

    @Value("${Rosetta.PDSUrl}")
    private String pdsUrl;

    @Value("${base.url}")
    private String baseUrl;

    @Autowired
    private RosettaWebServiceImpl rosettaWebService;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
//        http.csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/").anonymous()
//                .antMatchers("/index.html").anonymous()
//                .antMatchers(RequestPathConstants.PATH_ROOT_RESTFUL + "/**").anonymous()
//                .anyRequest().permitAll()
//                .and()
//                .logout()
//                .invalidateHttpSession(true)
//                .logoutSuccessUrl(RequestPathConstants.PATH_USER_LOGOUT);
        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll();
        http.addFilterBefore(httpAccessManagementFilter(), AnonymousAuthenticationFilter.class);
        http.headers().frameOptions().sameOrigin();
    }

    @Bean
    public HttpAccessManagementFilter httpAccessManagementFilter() {
        HttpAccessManagementFilter bean = new HttpAccessManagementFilter();
        bean.setSecurityConfig(this);
        return bean;
    }

    private static final Map<String, HttpSession> sessions = new HashMap<>();

    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent hse) {
                synchronized (SESSION_LOCK) {
                    log.info("Created session: {}", hse.getSession().getId());
                    sessions.put(hse.getSession().getId(), hse.getSession());
                }
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent hse) {
                synchronized (SESSION_LOCK) {
                    sessions.remove(hse.getSession().getId());
                    log.info("Removed session: {}", hse.getSession().getId());
                }
            }
        };
    }

    public String getCurrentSessionMessage(HttpServletRequest req, HttpServletResponse rsp) {
        StringBuilder buf = new StringBuilder();

        try {
            buf.append(req.getRequestURI()).append(" ").append(rsp.getStatus()).append("\r\n");

            Enumeration<String> reqHeaderNames = req.getHeaderNames();
            buf.append("[RequestHeader]\r\n");
            while (reqHeaderNames.hasMoreElements()) {
                String key = reqHeaderNames.nextElement();
                buf.append(key).append("=").append(req.getHeader(key)).append("\r\n");
            }

            if (rsp.getHeaderNames().size() > 0) {
                buf.append("[ResponseHeader]").append("\r\n");

                rsp.getHeaderNames().forEach(key -> {
                    buf.append(key).append("=").append(rsp.getHeader(key)).append("\r\n");
                });
            }

            StandardSession ssCur = null;
            try {
                ssCur = getPrivateSessionField(req.getSession());
            } catch (Exception e) {
                log.error("Failed to get current session", e);
            }

            if (ssCur != null) {
                buf.append(String.format("[Current Session], isValid=%b", ssCur.isValid())).append("\r\n");
                buf.append("\tDetails: ").append(getSessionDetails(ssCur)).append("\r\n");
                buf.append("\tAuth: ").append(getAuthDetails(ssCur)).append("\r\n");
            }

            synchronized (SESSION_LOCK) {
                sessions.forEach((key, session) -> {
                    StandardSession ss = getPrivateSessionField(session);
                    if (ss != null) {
                        buf.append(String.format("[Existing Session], key=%s, isValid=%b", key, ss.isValid())).append("\r\n");
                        buf.append("\tDetails: ").append(getSessionDetails(ss)).append("\r\n");
                        buf.append("\tAuth: ").append(getAuthDetails(req.getSession())).append("\r\n");
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to generate message", e);
        }

        return buf.toString();
    }

    private String getSessionDetails(StandardSession ss) {
        if (ss == null) {
            return "null";
        }

        try {
            if (ss.isValid()) {
                return String.format("SessionId: %s, CreationTime: %s, LatestAccessTime: %s, Time Used: %d, MaxInactiveInterval: %d, isNew: %b",
                        ss.getId(),
                        getReadableDatetime(ss.getCreationTime()),
                        getReadableDatetime(ss.getLastAccessedTime()),
                        System.currentTimeMillis() - ss.getLastAccessedTime(),
//                        ss.getServletContext().getSessionTimeout(),
                        ss.getMaxInactiveInterval(),
                        ss.isNew());
            } else {
                return String.format("SessionId: %s", ss.getId());
            }
        } catch (Throwable e) {
            return e.getMessage();
        }
    }

    public StandardSession getPrivateSessionField(HttpSession session) {
        if (session == null) {
            return null;
        }

        Field f = null;
        try {
            f = session.getClass().getDeclaredField("session");
        } catch (NoSuchFieldException e) {
            return null;
        }
        f.setAccessible(true);
        StandardSession ss = null;
        try {
            ss = (StandardSession) f.get(session);
        } catch (IllegalAccessException e) {
            return null;
        }
        return ss;
    }

    private String getAuthDetails(HttpSession hs) {
        PdsUserInfo userInfo = (PdsUserInfo) hs.getAttribute(DashboardConstants.KEY_USER_INFO);
        if (DashboardHelper.isNull(userInfo)) {
            return "null";
        }

        return String.format("[userName: %s, userId: %s, pid: %s]", userInfo.getUserName(), userInfo.getUserId(), userInfo.getPid());
    }

    private String getAuthDetails(StandardSession ss) {
        if (ss == null) {
            return "null";
        }

        if (!ss.isValid()) {
            return "invalid";
        }
        SecurityContext auth = (SecurityContext) ss.getAttribute("SPRING_SECURITY_CONTEXT");
        if (auth == null) {
            return "null";
        }
        UsernamePasswordAuthenticationToken userAuth = (UsernamePasswordAuthenticationToken) auth.getAuthentication();
        if (userAuth == null) {
            return "null";
        }

        User user = (User) userAuth.getDetails();
        if (user == null) {
            return "null";
        }

        return String.format("Username: %s", user.getUsername());
    }

    private String getReadableDatetime(long milliSeconds) {
        ZoneId zoneId = OffsetDateTime.now().getOffset().normalized();
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliSeconds), zoneId);
        return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String getAbsolutePath(HttpServletRequest req, String targetUrl) {
        return String.format("%s%s", baseUrl, targetUrl);
    }

    public boolean isValidSession(HttpServletRequest req, HttpServletResponse rsp) {
        StandardSession session = this.getPrivateSessionField(req.getSession());
        if (session == null || !session.isValid()) {
            log.info("Redirect doFilter {}", this.getCurrentSessionMessage(req, rsp));
            return false;
        }

        PdsUserInfo userInfo = (PdsUserInfo) req.getSession().getAttribute(DashboardConstants.KEY_USER_INFO);
        if (DashboardHelper.isNull(userInfo)) {
            log.info("Redirect doFilter {}", this.getCurrentSessionMessage(req, rsp));
            return false;
        }

        String pdsHandleInCookie = null;
        Cookie[] cookies = req.getCookies();
        for (Cookie cookie : cookies) {
            if (StringUtils.equals(cookie.getName(), DashboardConstants.KEY_PDS_HANDLE)) {
                pdsHandleInCookie = cookie.getValue();
                break;
            }
        }

        if (StringUtils.isEmpty(pdsHandleInCookie) || !StringUtils.equals(pdsHandleInCookie, userInfo.getPid())) {
            log.info("Invalid pds_handle: cookie pds_handle: {}, session pds_handle: {}", pdsHandleInCookie, userInfo.getPid());
            return false;
        }

        return true;
    }
}
