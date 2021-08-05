package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import nz.govt.natlib.ndha.common.exlibris.ResultOfDeposit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestScheduleProcessorImplJobDepositing extends ScheduleProcessorTester {
    private ScheduleProcessor testInstance = new ScheduleProcessorImplJobDepositing();

    @BeforeEach
    public void clearAndInit() throws Exception {
        initProcessor(testInstance);
        repoDepositJob.deleteAll();
        initSubFolder();

        ScheduleProcessor injectionProcessor = new ScheduleProcessorImplJobInjecting();
        initProcessor(injectionProcessor);

        addReadyForIngestionFile();

        injectionProcessor.handle(flowSetting);
    }

    @Test
    public void testDepositingJobsInjectionSuccess() throws Exception {
        when(rosettaWebService.deposit(any(), any(), any(), any(), any(), any())).thenReturn(new ResultOfDeposit(true, "OK", sipId));

        testInstance.handle(flowSetting);

        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.size() == 1;

        EntityDepositJob job = jobs.get(0);
        assert job.getStage() == EnumDepositJobStage.DEPOSIT;
        assert job.getState() == EnumDepositJobState.RUNNING;
        assert job.getFileCount() == 2;
        assert job.getFileSize() == testFileLength_1 + testFileLength_2;

        assert job.getSipID().equals(sipId);
    }

    @Test
    public void testDepositingJobsInjectionFailed() throws Exception {
        when(rosettaWebService.deposit(any(), any(), any(), any(), any(), any())).thenReturn(new ResultOfDeposit(false, "Failed", "xyz"));

        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.size() == 1;

        EntityDepositJob job = jobs.get(0);
        job.setSipID(null);
        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.INITIALED);
        repoDepositJob.save(job);

        testInstance.handle(flowSetting);

        jobs = repoDepositJob.getAll();
        assert jobs != null;
        assert jobs.size() == 1;

         job = jobs.get(0);
        assert job.getStage() == EnumDepositJobStage.DEPOSIT;
        assert job.getState() == EnumDepositJobState.FAILED;
        assert job.getFileCount() == 2;
        assert job.getFileSize() == testFileLength_1 + testFileLength_2;

        assert DashboardHelper.isNull(job.getSipID());
    }


    @AfterEach
    public void clear() {
        clearSubFolders();
    }
}
