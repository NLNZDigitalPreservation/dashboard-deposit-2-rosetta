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
    private final ScheduleProcessorBasic testInstance = new ScheduleProcessorImpl();

    @BeforeEach
    public void clearAndInit() throws IOException {
        initProcessor(testInstance);
        testInstance.processingJobs.clear();
        initSubFolder();
    }

    @Test
    public void testPreparingJobsIngestNotReady() {
        testInstance.handleIngest();

        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.isEmpty();

        //Verify is it cached
        assert testInstance.processingJobs.isEmpty();
    }

    @Test
    public void testPreparingJobsIngestIsReady() {
        addReadyForIngestionFile();

        testInstance.handleIngest();

        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;
        assert job.getStage() == EnumDepositJobStage.DEPOSIT;
        assert job.getState() == EnumDepositJobState.INITIALED;
        assert job.getFileCount() == 2;
        assert job.getFileSize() == testFileLength_1 + testFileLength_2;

        //Verify is it cached
        assert testInstance.processingJobs.size() == 1;

        testInstance.handleIngest();
        //Verify is it cached
        assert testInstance.processingJobs.size() == 1;
    }

    @Test
    public void testIngestDepositDone() {
        addReadyForIngestionFile();
        testInstance.handleIngest();
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;
        assert job.getStage() == EnumDepositJobStage.DEPOSIT;
        assert job.getState() == EnumDepositJobState.INITIALED;
        assert job.getFileCount() == 2;
        assert job.getFileSize() == testFileLength_1 + testFileLength_2;

        //Verify is it cached
        assert testInstance.processingJobs.size() == 1;

        addDepositDoneFile();
        testInstance.handleIngest();

        //Verify is it cached
        assert testInstance.processingJobs.size() == 1;
    }

    @Test
    public void testIngestDepositDoneFromStart() {
        addDepositDoneFile();
        testInstance.handleIngest();

        //Verify is it cached
        assert testInstance.processingJobs.size() == 1;
    }

    @AfterEach
    public void clear() {
        clearSubFolders();
        clearJobs();
    }
}
