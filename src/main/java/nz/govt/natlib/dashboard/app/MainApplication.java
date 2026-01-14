package nz.govt.natlib.dashboard.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

@SpringBootApplication
@ComponentScan(basePackages = {"nz.govt.natlib.dashboard.app", "nz.govt.natlib.dashboard.common.auth", "nz.govt.natlib.dashboard.domain.daemon", "nz.govt.natlib.dashboard.domain", "nz.govt.natlib.dashboard.ui"})
public class MainApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MainApplication.class);
    //    @Autowired
    private final ApplicationContext ctx;
    private final ConfigurableEnvironment env;

    public MainApplication(ApplicationContext ctx, ConfigurableEnvironment env) {
        this.ctx = ctx;
        this.env = env;
    }

    public static void main(String[] args) {
        try {
            SpringApplication.run(MainApplication.class, args);
        } catch (Throwable e) {
            log.error("Failed to start the application", e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        // Note that this is just here for debugging purposes. It can be deleted at any time.
        log.info("Let's inspect the beans provided by Spring Boot:");
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            log.info("bean: {}", beanName);
        }

        List<String> listProps = new ArrayList<>();
        for (var propertySource : env.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource eps) {
                for (String name : eps.getPropertyNames()) {
                    listProps.add(name + "=" + eps.getProperty(name));
                }
            }
        }
        String[] props = listProps.toArray(new String[0]);
        Arrays.sort(props);
        for (String prop : props) {
            log.info("prop: {}", prop);
        }
        log.info("^_^_^_^_^_^_^_^_^_^ Dashboard Initialed ^_^_^_^_^_^_^_^_^_^_^");
    }
}
