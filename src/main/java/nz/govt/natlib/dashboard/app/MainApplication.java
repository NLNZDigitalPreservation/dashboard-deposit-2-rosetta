package nz.govt.natlib.dashboard.app;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"nz.govt.natlib.dashboard.app", "nz.govt.natlib.dashboard.domain.daemon", "nz.govt.natlib.dashboard.domain", "nz.govt.natlib.dashboard.ui"})
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class MainApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_CYAN = "\u001B[36m";

    @Autowired
    private ApplicationContext ctx;

    public static void main(String[] args) {
        try {
            SpringApplication.run(MainApplication.class, args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        // Note that this is just here for debugging purposes. It can be deleted at any time.
//        System.out.println(ANSI_GREEN + "Let's inspect the beans provided by Spring Boot:" + ANSI_RESET);
        log.info("Let's inspect the beans provided by Spring Boot:");
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
//            System.out.println(ANSI_CYAN + beanName + ANSI_RESET);
            log.info(beanName);
        }

//        System.out.println(ANSI_BLUE + "^_^_^_^_^_^_^_^_^_^ Dashboard Initialed ^_^_^_^_^_^_^_^_^_^_^" + ANSI_RESET);
        log.info("^_^_^_^_^_^_^_^_^_^ Dashboard Initialed ^_^_^_^_^_^_^_^_^_^_^");
    }
}
