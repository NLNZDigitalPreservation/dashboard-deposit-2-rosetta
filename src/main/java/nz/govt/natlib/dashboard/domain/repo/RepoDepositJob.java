package nz.govt.natlib.dashboard.domain.repo;

import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component("RepoDepositJob")
public class RepoDepositJob extends RepoAbstract {
    public static final String SUB_FOLDER = "jobs";
    public static final String HISTORY_FOLDER = "jobs-history";

    @PostConstruct
    public void init() {
        this.subStoragePath = this.systemStoragePath + File.separator + SUB_FOLDER;
        this.entityKey = EnumEntityKey.DepositJob;
    }

    public EntityDepositJob getById(Long id) {
        return (EntityDepositJob) getById(id, EntityDepositJob.class);
    }

    public EntityDepositJob getByFlowIdAndInjectionTitle(Long flowId, String injectionTitle) {
        List<EntityDepositJob> allJobs = getAll();
        for (EntityDepositJob job : allJobs) {
            if (job.getAppliedFlowSetting().getId().equals(flowId)
                    && StringUtils.equals(job.getInjectionTitle(), injectionTitle)) {
                return job;
            }
        }
        return null;
    }

    public List<EntityDepositJob> getAll() {
        List<EntityDepositJob> retVal = new ArrayList<>();

        File rootDir = new File(this.subStoragePath);
        if (!rootDir.exists()) {
            return retVal;
        }
        File[] files = rootDir.listFiles();
        if (files == null) {
            return retVal;
        }

        for (File f : files) {
            String json = read(f);
            if (StringUtils.isEmpty(json)) {
                continue;
            }

            EntityDepositJob obj = (EntityDepositJob) json2Object(json, EntityDepositJob.class);
            retVal.add(obj);
        }

        return retVal;
    }

    public List<EntityDepositJob> getByFlowId(Long flowId) {
        List<EntityDepositJob> retVal = new ArrayList<>();
        List<EntityDepositJob> allJobs = getAll();
        for (EntityDepositJob job : allJobs) {
            if (job.getAppliedFlowSetting().getId().equals(flowId)) {
                retVal.add(job);
            }
        }
        return retVal;
    }

    public List<EntityDepositJob> getByInitialTime(Long startTime, Long endTime) {
        List<EntityDepositJob> retVal = new ArrayList<>();
        List<EntityDepositJob> allJobs = getAll();
        for (EntityDepositJob job : allJobs) {
            if (job.getInitialTime().compareTo(startTime) >= 0 && job.getInitialTime().compareTo(endTime) <= 0) {
                retVal.add(job);
            }
        }
        return retVal;
    }

    public List<EntityDepositJob> getByLatestTime(Long startTime, Long endTime) {
        List<EntityDepositJob> retVal = new ArrayList<>();
        List<EntityDepositJob> allJobs = getAll();
        for (EntityDepositJob job : allJobs) {
            if (job.getLatestTime().compareTo(startTime) >= 0 && job.getLatestTime().compareTo(endTime) <= 0) {
                retVal.add(job);
            }
        }
        return retVal;
    }

    public boolean moveToHistory(Long id) {
        String fileName = String.format("%d.json", id);
        EntityDepositJob job = this.getById(id);
        File historyPath = new File(this.systemStoragePath, HISTORY_FOLDER);

        this.save(historyPath.getAbsolutePath(), fileName, job);
        this.deleteById(id);

        return true;
    }
}
