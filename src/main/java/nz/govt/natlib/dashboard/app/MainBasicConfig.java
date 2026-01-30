package nz.govt.natlib.dashboard.app;

import nz.govt.natlib.dashboard.common.auth.LdapAuthenticationClient;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MainBasicConfig {
    private final static Logger log = LoggerFactory.getLogger(MainBasicConfig.class);

    @Value("${system.storage.path}")
    private String systemStoragePath;
    @Value("${Rosetta.RestApiDpsUrl}")
    private String restApiDpsUrl;
    @Value("${Rosetta.RestApiSipUrl}")
    private String restApiSipUrl;

    @Autowired
    private LdapAuthenticationClient authClient;


    @Bean(BeanDefinition.SCOPE_SINGLETON)
    public RosettaWebService rosettaRestApi() throws Exception {
        log.info("Start to initial Rosetta Web Service");
        RosettaWebService bean = new RosettaWebService(authClient, restApiDpsUrl, restApiSipUrl);

        log.info("End to initial Rosetta Web Service");
        return bean;
    }


    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new CustomTomcatServletWebServerFactory();
    }

    static class CustomTomcatServletWebServerFactory extends TomcatServletWebServerFactory {
        protected void postProcessContext(Context context) {
            ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
        }
    }
}


