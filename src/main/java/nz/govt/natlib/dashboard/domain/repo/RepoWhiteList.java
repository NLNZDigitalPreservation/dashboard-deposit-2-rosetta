package nz.govt.natlib.dashboard.domain.repo;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityWhiteList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("RepoWhiteList")
public class RepoWhiteList extends RepoAbstract {
    private static final String STORAGE_FOLDER = "setting-white-list";
    private PrimaryIndex<Long, EntityWhiteList> primaryIndexById;
    private SecondaryIndex<String, Long, EntityWhiteList> secondaryIndexByUserName;
    @Autowired
    private RepoIdGenerator idGenerator;

    public Long nextId() {
        return idGenerator.nextId(EnumEntityKey.WhiteList);
    }

    @Override
    public void init() throws DatabaseException, IOException {
        super.initInternal();
        primaryIndexById = store.getPrimaryIndex(Long.class, EntityWhiteList.class);
        secondaryIndexByUserName = store.getSecondaryIndex(primaryIndexById, String.class, "userName");
    }

    @Override
    public String getSubDirectory() {
        return STORAGE_FOLDER;
    }

    public void save(EntityWhiteList obj) {
        primaryIndexById.put(obj);
    }

    public EntityWhiteList getById(Long id) {
        return primaryIndexById.get(id);
    }

    public EntityWhiteList getByUserName(String userName) {
        EntityCursor<EntityWhiteList> cursor = secondaryIndexByUserName.subIndex(userName).entities();
        for (EntityWhiteList obj : cursor) {
            cursor.close();
            return obj;
        }
        cursor.close();
        return null;
    }

    public List<EntityWhiteList> getAll() {
        return toList(primaryIndexById.entities());
    }

    public void deleteById(Long id) {
        primaryIndexById.delete(id);
    }

    public void deleteAll() {
        List<EntityWhiteList> list = getAll();
        for (EntityWhiteList obj : list) {
            primaryIndexById.delete(obj.getId());
        }
    }

    private List<EntityWhiteList> toList(EntityCursor<EntityWhiteList> cursor) {
        List<EntityWhiteList> list = new ArrayList<>();
        for (EntityWhiteList obj : cursor) {
            list.add(obj);
        }
        cursor.close();
        return list;
    }
}
