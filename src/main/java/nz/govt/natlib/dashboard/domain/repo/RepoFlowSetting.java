package nz.govt.natlib.dashboard.domain.repo;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("RepoFlowSetting")
public class RepoFlowSetting extends RepoAbstract {
    private static final String STORAGE_FOLDER = "setting-flow";
    private PrimaryIndex<Long, EntityFlowSetting> primaryIndexById;
    @Autowired
    private RepoIdGenerator idGenerator;

    @Override
    public void init() throws DatabaseException, IOException {
        super.initInternal();
        primaryIndexById = store.getPrimaryIndex(Long.class, EntityFlowSetting.class);
    }

    @Override
    public String getSubDirectory() {
        return STORAGE_FOLDER;
    }

    public Long nextId() {
        return idGenerator.nextId(EnumEntityKey.FlowSetting);
    }

    public void save(EntityFlowSetting obj) {
        primaryIndexById.put(obj);
    }

    public EntityFlowSetting getById(Long id) {
        return primaryIndexById.get(id);
    }

    public List<EntityFlowSetting> getAll() {
        return toList(primaryIndexById.entities());
    }

    public void deleteById(Long id) {
        primaryIndexById.delete(id);
    }

    public void deleteAll() {
        List<EntityFlowSetting> list = getAll();
        for (EntityFlowSetting obj : list) {
            primaryIndexById.delete(obj.getId());
        }
    }

    private List<EntityFlowSetting> toList(EntityCursor<EntityFlowSetting> cursor) {
        List<EntityFlowSetting> list = new ArrayList<>();
        for (EntityFlowSetting obj : cursor) {
            list.add(obj);
        }
        cursor.close();
        return list;
    }
}
