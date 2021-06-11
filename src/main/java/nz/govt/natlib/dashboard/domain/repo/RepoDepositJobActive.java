package nz.govt.natlib.dashboard.domain.repo;

import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("RepoDepositJobActive")
public class RepoDepositJobActive extends RepoDepositJob {
    private static final String STORAGE_FOLDER = "job-active";

    @Autowired
    private RepoIdGenerator idGenerator;

    @Override
    public String getSubDirectory() {
        return STORAGE_FOLDER;
    }

    public Long nextId() {
        return idGenerator.nextId(EnumEntityKey.DepositJob);
    }
}
