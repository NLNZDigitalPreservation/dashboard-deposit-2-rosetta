package nz.govt.natlib.dashboard.domain.repo;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.PrimaryIndex;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component("RepoGlobalSetting")
public class RepoGlobalSetting extends RepoAbstract {
    private static final String STORAGE_FOLDER = "setting-global";
    private PrimaryIndex<Long, EntityGlobalSetting> primaryIndexById;

    @Override
    public void init() throws DatabaseException, IOException {
        super.initInternal();
        primaryIndexById = store.getPrimaryIndex(Long.class, EntityGlobalSetting.class);
    }

    public void save(EntityGlobalSetting obj) {
        primaryIndexById.put(obj);
    }

    public EntityGlobalSetting getById(Long id) {
        return primaryIndexById.get(id);
    }

    public void deleteById(Long id) {
        primaryIndexById.delete(id);
    }

    @Override
    public String getSubDirectory() {
        return STORAGE_FOLDER;
    }
}
