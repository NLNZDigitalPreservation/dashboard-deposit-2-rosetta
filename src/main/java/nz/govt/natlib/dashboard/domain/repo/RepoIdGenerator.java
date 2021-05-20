package nz.govt.natlib.dashboard.domain.repo;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.PrimaryIndex;
import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityID;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("RepoIdGenerator")
public class RepoIdGenerator extends RepoAbstract {
    private static final String STORAGE_FOLDER = "id-generator";
    private PrimaryIndex<String, EntityID> primaryIndexById;

    @Override
    public void init() throws DatabaseException, IOException {
        super.initInternal();
        primaryIndexById = store.getPrimaryIndex(String.class, EntityID.class);
    }

    @Override
    public String getSubDirectory() {
        return STORAGE_FOLDER;
    }

    synchronized public Long nextId(EnumEntityKey key) {
        EntityID obj = primaryIndexById.get(key.name());
        if (obj == null) {
            obj = new EntityID();
            obj.setKey(key.name());
            obj.setNumber(1L);
        } else {
            obj.setNumber(obj.getNumber() + 1);
        }

        primaryIndexById.put(obj);
        return obj.getNumber();
    }
}
