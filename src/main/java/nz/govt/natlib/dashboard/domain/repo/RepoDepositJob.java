package nz.govt.natlib.dashboard.domain.repo;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class RepoDepositJob extends RepoAbstract {
    public static final String DEPOSIT_JOB_DIR_ACTIVE = "deposit_jobs_active";
    public static final String DEPOSIT_JOB_DIR_HISTORY = "deposit_jobs_history";

    private PrimaryIndex<Long, EntityDepositJob> primaryIndexById;
    private SecondaryIndex<String, Long, EntityDepositJob> secondaryIndexByInjectionTitle;
    private SecondaryIndex<Long, Long, EntityDepositJob> secondaryIndexByFlowId;
    private SecondaryIndex<Long, Long, EntityDepositJob> secondaryIndexByInitialTime;
    private SecondaryIndex<Long, Long, EntityDepositJob> secondaryIndexByLatestTime;

    @Override
    public void init() throws IOException {
        super.initInternal();
        primaryIndexById = store.getPrimaryIndex(Long.class, EntityDepositJob.class);
        secondaryIndexByInjectionTitle = store.getSecondaryIndex(primaryIndexById, String.class, "injectionTitle");
        secondaryIndexByFlowId = store.getSecondaryIndex(primaryIndexById, Long.class, "flowId");
        secondaryIndexByInitialTime = store.getSecondaryIndex(primaryIndexById, Long.class, "initialTime");
        secondaryIndexByLatestTime = store.getSecondaryIndex(primaryIndexById, Long.class, "latestTime");
    }

    public void save(EntityDepositJob job) {
        primaryIndexById.put(job);
    }

    public EntityDepositJob getById(Long id) {
        return primaryIndexById.get(id);
    }

    public EntityDepositJob getByFlowIdAndInjectionTitle(Long flowId, String injectionTitle) {
        EntityCursor<EntityDepositJob> cursor = secondaryIndexByInjectionTitle.subIndex(injectionTitle).entities();
        for (EntityDepositJob job : cursor) {
            if (job.getFlowId().equals(flowId)) {
                cursor.close();
                return job;
            }
        }
        cursor.close();
        return null;
    }

    public List<EntityDepositJob> getAll() {
        return toList(primaryIndexById.entities());
    }

    public List<EntityDepositJob> getByFlowId(Long flowId) {
        if (flowId==null){
            return new ArrayList<>();
        }
        EntityCursor<EntityDepositJob> cursor = secondaryIndexByFlowId.subIndex(flowId).entities();
        return toList(cursor);
    }

    public List<EntityDepositJob> getByInitialTime(Long startTime, Long endTime) {
        EntityCursor<EntityDepositJob> cursor = secondaryIndexByInitialTime.entities(startTime, true, endTime, true);
        return toList(cursor);
    }

    public List<EntityDepositJob> getByLatestTime(Long startTime, Long endTime) {
        EntityCursor<EntityDepositJob> cursor = secondaryIndexByLatestTime.entities(startTime, true, endTime, true);
        return toList(cursor);
    }

    public void deleteById(Long id) {
        primaryIndexById.delete(id);
    }

    public void deleteAll() {
        List<EntityDepositJob> list = getAll();
        for (EntityDepositJob job : list) {
            primaryIndexById.delete(job.getId());
        }
    }

    private List<EntityDepositJob> toList(EntityCursor<EntityDepositJob> cursor) {
        List<EntityDepositJob> list = new ArrayList<>();
        for (EntityDepositJob obj : cursor) {
            list.add(obj);
        }
        cursor.close();
        return list;
    }
}
