package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class TestScheduleProcessorBasicImplJobInjecting extends ScheduleProcessorTester {
    private final ScheduleProcessorBasic testInstance = new ScheduleProcessorImpl(flowSetting);

    @BeforeEach
    public void clearAndInit() throws IOException {
        initProcessor(testInstance);

        initSubFolder();
    }

    @Test
    public void testPreparingJobsInjectionNotReady() throws Exception {
        testInstance.handleIngest();

        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.size() == 1;

        EntityDepositJob job = jobs.get(0);
        assert job.getStage() == EnumDepositJobStage.INGEST;
        assert job.getState() == EnumDepositJobState.RUNNING;
        assert job.getFileCount() == 2;
        assert job.getFileSize() == testFileLength_1 + testFileLength_2;
    }

    @Test
    public void testPreparingJobsInjectionIsReady() throws Exception {
        addReadyForIngestionFile();

        testInstance.handleIngest();

        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;
        assert job.getStage() == EnumDepositJobStage.DEPOSIT;
        assert job.getState() == EnumDepositJobState.INITIALED;
        assert job.getFileCount() == 2;
        assert job.getFileSize() == testFileLength_1 + testFileLength_2;
    }

    @AfterEach
    public void clear() {
        clearSubFolders();
    }
}
