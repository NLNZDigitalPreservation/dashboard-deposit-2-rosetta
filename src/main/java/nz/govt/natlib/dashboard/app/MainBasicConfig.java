package nz.govt.natlib.dashboard.app;

import nz.govt.natlib.dashboard.common.core.RosettaApi;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import nz.govt.natlib.dashboard.common.core.RosettaApiStub;
import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.lang.Exception;

@Configuration
public class MainBasicConfig {
    private final static Logger log = LoggerFactory.getLogger(MainBasicConfig.class);
//    private final static String JE_LOG_FILE_MAX = Long.toString(512 * 1014 * 1014);

    @Value("${system.storage.path}")
    private String systemStoragePath;
    @Value("${Rosetta.PDSUrl}")
    private String pdsUrl;
    @Value("${Rosetta.RestApiUrl}")
    private String restApiUrl;

    @Value("${TestEnabled}")
    private boolean isTestMode;


    @Bean(BeanDefinition.SCOPE_SINGLETON)
    public RosettaApi rosettaRestApi() throws Exception {
        log.info("Start to initial Rosetta Web Service");
        RosettaApi bean;
        if (isTestMode) {
            log.warn("Started with testing mode.");
            bean = new RosettaApiStub();
        } else {
            bean = new RosettaApi(pdsUrl, restApiUrl);
        }
//        bean.init();
        log.info("End to initial Rosetta Web Service");
        return bean;
    }

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        log.info("Start to initial FreeMarkerConfigure");
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPath("/app");
        log.info("End to initial FreeMarkerConfigure");
        return freeMarkerConfigurer;
    }

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new CustomTomcatServletWebServerFactory();
    }

    static class CustomTomcatServletWebServerFactory extends TomcatServletWebServerFactory {
        @Override
        protected void postProcessContext(Context context) {
            ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
        }
    }
}


