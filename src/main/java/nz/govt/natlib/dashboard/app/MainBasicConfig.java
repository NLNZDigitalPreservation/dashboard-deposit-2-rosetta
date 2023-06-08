package nz.govt.natlib.dashboard.app;

import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.common.core.RosettaWebServiceImpl;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import nz.govt.natlib.dashboard.common.core.RosettaWebServiceStub;
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

import javax.annotation.PostConstruct;
import java.lang.Exception;

@Configuration
public class MainBasicConfig {
    private final static Logger log = LoggerFactory.getLogger(MainBasicConfig.class);
//    private final static String JE_LOG_FILE_MAX = Long.toString(512 * 1014 * 1014);

    @Value("${system.storage.path}")
    private String systemStoragePath;
    @Value("${Rosetta.PDSUrl}")
    private String pdsUrl;
    @Value("${Rosetta.ProducerWsdlUrl}")
    private String wsdlUrlProducer;
    @Value("${Rosetta.DepositWsdlUrl}")
    private String wsdlUrlDeposit;
    @Value("${Rosetta.SipWsdlUrl}")
    private String wsdlUrlSip;
    @Value("${Rosetta.DeliveryAccessWsdlUrl}")
    private String wsdlUrlDeliveryAccess;

    @Value("${ProxyEnable}")
    private String proxyEnable;
    @Value("${ProxyHost}")
    private String proxyHost;
    @Value("${ProxyPort}")
    private String proxyPort;

    @Value("${TestEnabled}")
    private boolean isTestMode;

    @PostConstruct
    public void init() {
        if (proxyEnable.equalsIgnoreCase("YES") || proxyEnable.equalsIgnoreCase("TRUE")) {
            log.info("Proxy is enabled.");
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", proxyPort);
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", proxyPort);
        }
    }

    @Bean(BeanDefinition.SCOPE_SINGLETON)
    public RosettaWebService rosettaWebService() throws Exception {
        log.info("Start to initial Rosetta Web Service");
        RosettaWebService bean;
        if (isTestMode) {
            log.warn("Started with testing mode.");
            bean = new RosettaWebServiceStub();
        } else {
            bean = new RosettaWebServiceImpl(pdsUrl, wsdlUrlProducer, wsdlUrlDeposit, wsdlUrlSip, wsdlUrlDeliveryAccess);
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


