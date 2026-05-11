package nz.govt.natlib.dashboard.common.auth;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.metadata.EnumUserRole;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.service.WhitelistSettingService;
import nz.govt.natlib.dashboard.util.DashboardHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HttpAccessManagementFilter implements Filter {
    public static final String ATTR_AUTH_USERNAME = "ENTRA_AUTH_USERNAME";
    public static final String ATTR_AUTH_DISPLAY_NAME = "ENTRA_AUTH_DISPLAY_NAME";
    public static final String ATTR_AUTH_OBJECT_ID = "ENTRA_AUTH_OBJECT_ID";
    public static final String ATTR_AUTH_ROLE = "ENTRA_AUTH_ROLE";
    public static final String ATTR_AUTH_TOKEN = "ENTRA_AUTH_TOKEN";

    private static final Logger log = LoggerFactory.getLogger(HttpAccessManagementFilter.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private WhitelistSettingService whitelistService;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rsp = ((HttpServletResponse) response);
        String contextUri = req.getContextPath();
        String url = req.getRequestURI().substring(contextUri.length());

        log.debug("contextUri: {}, url: {}", contextUri, url);

        // Ignore the request of Non-REST APIs.
        if (!url.startsWith(DashboardConstants.PATH_ROOT) || url.equalsIgnoreCase(DashboardConstants.SYSTEM_INFO)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            EntraIdentity identity = resolveIdentity(req);
            if (identity == null || StringUtils.isBlank(identity.username)) {
                rsp.sendError(HttpStatus.SC_UNAUTHORIZED, "Missing Entra identity headers");
                return;
            }

            EntityWhitelistSetting whitelistUser = whitelistService.getUserFromWhiteList(identity.username);
            if (whitelistUser == null && whitelistService.isEmptyWhiteList()) {
                // Bootstrap the first authenticated Entra user as admin when no whitelist
                // exists.
                EntityWhitelistSetting firstAdmin = new EntityWhitelistSetting();
                firstAdmin.setWhiteUserName(identity.username);
                firstAdmin.setWhiteUserRole(EnumUserRole.admin.name());
                whitelistService.saveWhitelistSetting(firstAdmin);
                whitelistUser = firstAdmin;
                log.info("Initialized whitelist with first Entra user: {}", identity.username);
            }

            if (whitelistUser == null) {
                rsp.sendError(HttpStatus.SC_FORBIDDEN, "User is not in whitelist");
                return;
            }

            String role = whitelistUser.getWhiteUserRole();
            if (!isPermit(role, req.getMethod(), url)) {
                rsp.sendError(HttpStatus.SC_FORBIDDEN);
                return;
            }

            req.setAttribute(ATTR_AUTH_USERNAME, identity.username);
            req.setAttribute(ATTR_AUTH_DISPLAY_NAME, identity.displayName);
            req.setAttribute(ATTR_AUTH_OBJECT_ID, identity.objectId);
            req.setAttribute(ATTR_AUTH_ROLE, role);
            req.setAttribute(ATTR_AUTH_TOKEN, identity.idToken);
        } catch (Exception e) {
            log.warn("Failed to verify Entra request", e);
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
        return !method.equalsIgnoreCase("POST")
                && !method.equalsIgnoreCase("PUT")
                && !method.equalsIgnoreCase("DELETE");
    }

    @Override
    public void destroy() {
    }

    public WhitelistSettingService getWhitelistService() {
        return whitelistService;
    }

    public void setWhitelistService(WhitelistSettingService whitelistService) {
        this.whitelistService = whitelistService;
    }

    private EntraIdentity resolveIdentity(HttpServletRequest req) {
        EntraIdentity identity = resolveIdentityFromPrincipal(req.getHeader("X-MS-CLIENT-PRINCIPAL"));
        if (identity == null) {
            identity = new EntraIdentity();
        }

        // App Service can forward these direct headers.
        identity.username = firstNonBlank(
                identity.username,
                req.getHeader("X-MS-CLIENT-PRINCIPAL-NAME"),
                req.getHeader("X-MS-CLIENT-PRINCIPAL-ID"));
        identity.displayName = firstNonBlank(identity.displayName, req.getHeader("X-MS-CLIENT-PRINCIPAL-NAME"));
        identity.objectId = firstNonBlank(identity.objectId, req.getHeader("X-MS-CLIENT-PRINCIPAL-ID"));
        identity.idToken = req.getHeader("X-MS-TOKEN-AAD-ID-TOKEN");

        if (StringUtils.isBlank(identity.username)) {
            return null;
        }

        return identity;
    }

    private EntraIdentity resolveIdentityFromPrincipal(String encodedPrincipal) {
        if (StringUtils.isBlank(encodedPrincipal)) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encodedPrincipal);
            String json = new String(decoded, StandardCharsets.UTF_8);
            JsonNode root = JSON.readTree(json);

            EntraIdentity identity = new EntraIdentity();
            identity.username = findClaimValue(root, "preferred_username", "upn", "name",
                    "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name");
            identity.displayName = firstNonBlank(root.path("name").asText(null), identity.username);
            identity.objectId = firstNonBlank(
                    root.path("oid").asText(null),
                    findClaimValue(root, "http://schemas.microsoft.com/identity/claims/objectidentifier"));
            return identity;
        } catch (Exception e) {
            log.warn("Failed to parse X-MS-CLIENT-PRINCIPAL header", e);
            return null;
        }
    }

    private String findClaimValue(JsonNode root, String... targetTypes) {
        JsonNode claims = root.path("claims");
        if (claims == null || !claims.isArray()) {
            return null;
        }

        for (JsonNode claim : claims) {
            String type = claim.path("typ").asText("");
            for (String targetType : targetTypes) {
                if (targetType.equalsIgnoreCase(type)) {
                    String value = claim.path("val").asText("");
                    if (!DashboardHelper.isEmpty(value)) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static class EntraIdentity {
        private String username;
        private String displayName;
        private String objectId;
        private String idToken;
    }
}