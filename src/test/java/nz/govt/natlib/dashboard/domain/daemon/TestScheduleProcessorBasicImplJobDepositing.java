package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.BasicTester;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.util.DashboardHelper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestScheduleProcessorBasicImplJobDepositing extends ScheduleProcessorTester {
    private final ScheduleProcessorBasic testInstance = new ScheduleProcessorImpl();

    @BeforeEach
    public void clearAndInit() {
        initProcessor(testInstance);
        repoDepositJob.deleteAll();
        initSubFolder();

        ScheduleProcessorBasic injectionProcessor = new ScheduleProcessorImpl();
        initProcessor(injectionProcessor);

        addReadyForIngestionFile();

        injectionProcessor.handleIngest();
    }

    @Test
    public void testDepositingJobsInjectionSuccess() throws Exception {
        String depositResponse = BasicTester.readResourceFile("data/deposit-inprogress.json");
        when(restApi.fetch(any(), any(), any(), any())).thenReturn(depositResponse);

        testInstance.handleIngest();

        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.size() == 1;

        EntityDepositJob job = jobs.get(0);
        testInstance.handleDeposit(depositAccount, flowSetting, injectionPathScanClient, job);
        assert job.getStage() == EnumDepositJobStage.DEPOSIT;
        assert job.getState() == EnumDepositJobState.RUNNING;
        assert job.getFileCount() == 2;
        assert job.getFileSize() == testFileLength_1 + testFileLength_2;

        assert job.getSipID().equals(sipId);
    }

    @Test
    public void testDepositingJobsInjectionFailed() throws Exception {
        String depositResponse = BasicTester.readResourceFile("data/deposit-declined.json");
        when(restApi.fetch(any(), any(), any(), any())).thenReturn(depositResponse);

        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.size() == 1;

        EntityDepositJob job = jobs.get(0);
        job.setSipID(null);
        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.INITIALED);
        repoDepositJob.save(job);

        testInstance.handleDeposit(depositAccount, flowSetting, injectionPathScanClient, job);

        jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.size() == 1;

        job = jobs.get(0);
        assert job.getStage() == EnumDepositJobStage.DEPOSIT;
        assert job.getState() == EnumDepositJobState.FAILED;
        assert job.getFileCount() == 2;
        assert job.getFileSize() == testFileLength_1 + testFileLength_2;

        assert DashboardHelper.isEmpty(job.getSipID());
    }

    @AfterEach
    public void clear() {
        clearSubFolders();
    }
}
