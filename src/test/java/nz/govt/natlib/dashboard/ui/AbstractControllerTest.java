package nz.govt.natlib.dashboard.ui;

import nz.govt.natlib.dashboard.common.auth.LdapAuthenticationClient;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


public class AbstractControllerTest {
    @SpringBootConfiguration // Provides the missing configuration context
    @EnableAutoConfiguration
    static class TestConfig {
    }


    @Autowired
    public MockMvc mockMvc;

    @MockitoBean
    private LdapAuthenticationClient authClient;

    @MockitoBean
    public RosettaWebService rosettaWebService;

    @MockitoBean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory;

    static class CustomTomcatServletWebServerFactory extends TomcatServletWebServerFactory {
        protected void postProcessContext(Context context) {
            ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
        }
    }
}
