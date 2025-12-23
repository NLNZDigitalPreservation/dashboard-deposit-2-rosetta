package nz.govt.natlib.dashboard.common.auth;

import nz.govt.natlib.dashboard.common.metadata.UserInfo;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.directory.Attribute;
import java.util.Hashtable;

@Component
public class LdapAuthenticationClient {
    @Value("${TestEnabled}")
    private boolean isTestMode;

    @Value("${ldap.enable}")
    private String ldapEnable;

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.usrSearchBase}")
    private String ldapUsrSearchBase;

    @Value("${ldap.usrSearchFilter}")
    private String ldapUsrSearchFilter;

    @Value("${ldap.groupSearchBase}")
    private String ldapGroupSearchBase;

    @Value("${ldap.groupSearchFilter}")
    private String ldapGroupSearchFilter;

    @Value("${ldap.contextSource.root}")
    private String ldapContextSourceRoot;

    @Value("${ldap.contextSource.manager.dn}")
    private String ldapContextSourceManagerDn;

    @Value("${ldap.contextSource.managerPassword}")
    private String ldapContextSourceManagerPassword;


    private LdapAuthenticationClient() {
    }


    public UserInfo login(String institution, String username, String password) throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setInstitution(institution);
        userInfo.setUserName(username);

        if (isTestMode || DashboardHelper.isEmpty(this.ldapEnable) || this.ldapEnable.equalsIgnoreCase("true") || this.ldapEnable.equalsIgnoreCase("yes")) {
            userInfo.setSessionId("241200811372143992420081372111");
            userInfo.setDisplayName("Test User");
            return userInfo;
        }

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapContextSourceManagerDn);
        env.put(Context.SECURITY_CREDENTIALS, ldapContextSourceManagerPassword);

        LdapContext managerCtx = new InitialLdapContext(env, null);

        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

        // Explicitly check if the filter is what you expect
        String filter = "(sAMAccountName=" + username + ")";

        NamingEnumeration<SearchResult> results = managerCtx.search(ldapUsrSearchBase, filter, constraints);

        if (!results.hasMore()) {
            managerCtx.close();
            throw new RuntimeException("User '" + username + "' not found");
        }

        SearchResult result = results.next();
        String userFullDn = result.getNameInNamespace();

        // Attempt to Bind as the User
        env.put(Context.SECURITY_PRINCIPAL, userFullDn);
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            InitialLdapContext initialLdapContext = new InitialLdapContext(env, null);
            initialLdapContext.close();
        } catch (AuthenticationException ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            managerCtx.close();
        }

        Attribute displayNameAttr = result.getAttributes().get("displayname");
        String displayName;
        if (displayNameAttr == null) {
            displayName = username;
        } else {
            try {
                displayName = (String) displayNameAttr.get();
            } catch (Exception ex) {
                displayName = username;
            }
        }
        userInfo.setDisplayName(displayName);
        userInfo.setSessionId(DashboardHelper.getUid());

        return userInfo;
    }


}
