package nz.govt.natlib.dashboard.domain.repo;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

public abstract class RepoAbstract {
    private final static String JE_LOG_FILE_MAX = Long.toString(512 * 1014 * 1014);
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${system.storage.path}")
    private String systemStoragePath;

    protected EntityStore store;

    public void initInternal() throws DatabaseException, IOException {
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setCacheSize(1024 * 1024);
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);
        environmentConfig.setConfigParam("je.log.fileMax", JE_LOG_FILE_MAX);
        File file = new File(systemStoragePath, getSubDirectory());
        if (!file.isDirectory()) {
            if (!file.mkdirs()) {
                log.error("Failed mkdirs( {} )", file.getAbsolutePath());
                throw new IOException("Failed to mkdirs: " + file.getAbsolutePath());
            }
        }
        Environment env = new Environment(file, environmentConfig);

        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setAllowCreateVoid(true);
        storeConfig.setTransactional(true);

        store = new EntityStore(env, "dashboard", storeConfig);
    }

    @PostConstruct
    abstract public void init() throws DatabaseException, IOException;

    abstract public String getSubDirectory();

    public void close() {
        this.store.close();
        this.store.getEnvironment().close();
    }
}
