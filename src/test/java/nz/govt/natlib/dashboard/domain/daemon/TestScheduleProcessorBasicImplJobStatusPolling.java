package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.BasicTester;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestScheduleProcessorBasicImplJobStatusPolling extends ScheduleProcessorTester {
    private final ScheduleProcessorBasic testInstance = new ScheduleProcessorImpl();

    @BeforeEach
    public void clearAndInit() throws Exception {
        initProcessor(testInstance);

        initSubFolder();

        // Initial processor
        ScheduleProcessorBasic scheduleProcessor = new ScheduleProcessorImpl();
        initProcessor(scheduleProcessor);
        addReadyForIngestionFile();
        scheduleProcessor.handleIngest();

        // Deposit job
        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        EntityDepositJob job = jobs.get(0);

        String depositResponse = BasicTester.readResourceFile("data/deposit-inprogress.json");
        when(restApi.fetch(any(), any(), any(), any())).thenReturn(depositResponse);

        scheduleProcessor.handleDeposit(depositAccount, flowSetting, injectionPathScanClient, job);
    }

    @Test
    public void testPollingStatusRunning() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.RUNNING);
        repoDepositJob.save(job);

        String depositResponse = BasicTester.readResourceFile("data/sipstatusinfo-ongoing.json");
        when(restApi.fetch(any(), any(), any(), any())).thenReturn(depositResponse);

        testInstance.handlePollingStatus(depositAccount, job);

        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.size() == 1;

        EntityDepositJob jobAfterHandled = jobs.get(0);
        assert jobAfterHandled.getSipID().equals("12345");
        assert jobAfterHandled.getStage() == EnumDepositJobStage.DEPOSIT;
        assert jobAfterHandled.getState() == EnumDepositJobState.RUNNING;
        assert jobAfterHandled.getFileCount() == 2;
        assert jobAfterHandled.getFileSize() == testFileLength_1 + testFileLength_2;
    }

    @Test
    public void testPollingStatusSuccess() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.RUNNING);
        repoDepositJob.save(job);

        String depositResponse = BasicTester.readResourceFile("data/sipstatusinfo-succeed.json");
        when(restApi.fetch(any(), any(), any(), any())).thenReturn(depositResponse);

        testInstance.handlePollingStatus(depositAccount, job);

        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.size() == 1;

        EntityDepositJob jobAfterHandled = jobs.get(0);
        assert jobAfterHandled.getSipID().equals("12345");
        assert jobAfterHandled.getStage() == EnumDepositJobStage.DEPOSIT;
        assert jobAfterHandled.getState() == EnumDepositJobState.SUCCEED;
        assert jobAfterHandled.getFileCount() == 2;
        assert jobAfterHandled.getFileSize() == testFileLength_1 + testFileLength_2;
    }

    @Test
    public void testPollingStatusFailed() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.RUNNING);
        repoDepositJob.save(job);

        String depositResponse = BasicTester.readResourceFile("data/sipstatusinfo-failed.json");
        when(restApi.fetch(any(), any(), any(), any())).thenReturn(depositResponse);

        testInstance.handlePollingStatus(depositAccount, job);

        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.size() == 1;

        EntityDepositJob jobAfterHandled = jobs.get(0);
        assert jobAfterHandled.getSipID().equals("12345");
        assert jobAfterHandled.getStage() == EnumDepositJobStage.DEPOSIT;
        assert jobAfterHandled.getState() == EnumDepositJobState.FAILED;
        assert jobAfterHandled.getFileCount() == 2;
        assert jobAfterHandled.getFileSize() == testFileLength_1 + testFileLength_2;
    }

    @AfterEach
    public void clear() {
        clearSubFolders();
    }
}
