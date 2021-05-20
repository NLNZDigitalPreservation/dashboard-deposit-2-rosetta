package nz.govt.natlib.dashboard.domain.repo;

import org.springframework.stereotype.Component;

@Component("RepoDepositJobHistory")
public class RepoDepositJobHistory extends RepoDepositJob {
    private static final String STORAGE_FOLDER = "job-history";

    @Override
    public String getSubDirectory() {
        return STORAGE_FOLDER;
    }
}
