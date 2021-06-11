package nz.govt.natlib.dashboard.domain.repo;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityStorageLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("RepoFTPSetting")
public class RepoStorageLocation extends RepoAbstract {
    private static final String STORAGE_FOLDER = "setting-storage-location";
    private PrimaryIndex<Long, EntityStorageLocation> primaryIndexById;

    @Autowired
    private RepoIdGenerator idGenerator;

    @Override
    public void init() throws DatabaseException, IOException {
        super.initInternal();
        primaryIndexById = store.getPrimaryIndex(Long.class, EntityStorageLocation.class);
    }

    public Long nextId() {
        return idGenerator.nextId(EnumEntityKey.StorageLocation);
    }

    public void save(EntityStorageLocation obj) {
        primaryIndexById.put(obj);
    }

    public EntityStorageLocation getById(Long id) {
        return primaryIndexById.get(id);
    }

    public List<EntityStorageLocation> getAll() {
        return toList(primaryIndexById.entities());
    }

    public void deleteById(Long id) {
        primaryIndexById.delete(id);
    }

    @Override
    public String getSubDirectory() {
        return STORAGE_FOLDER;
    }

    private List<EntityStorageLocation> toList(EntityCursor<EntityStorageLocation> cursor) {
        List<EntityStorageLocation> list = new ArrayList<>();
        for (EntityStorageLocation obj : cursor) {
            list.add(obj);
        }
        cursor.close();
        return list;
    }
}
