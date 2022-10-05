package nz.govt.natlib.dashboard.domain.daemon;

import com.exlibris.dps.SipStatusInfo;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.ndha.common.exlibris.ResultOfDeposit;
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

        //Initial processor
        ScheduleProcessorBasic scheduleProcessor = new ScheduleProcessorImpl();
        initProcessor(scheduleProcessor);
        addReadyForIngestionFile();
        scheduleProcessor.handleIngest();

        //Deposit job
        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        EntityDepositJob job = jobs.get(0);
        when(rosettaWebService.deposit(any(), any(), any(), any(), any(), any())).thenReturn(new ResultOfDeposit(true, "OK", sipId));
        scheduleProcessor.handleDeposit(flowSetting, injectionPathScanClient, job);
    }

    @Test
    public void testPollingStatusRunning() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.RUNNING);
        repoDepositJob.save(job);

        SipStatusInfo sipStatusInfo = new SipStatusInfo();
        sipStatusInfo.setModule("HUB");
        sipStatusInfo.setStage("RUNNING");
        sipStatusInfo.setStatus("SUCCEED");
        when(rosettaWebService.getSIPStatusInfo(sipId)).thenReturn(sipStatusInfo);

        testInstance.handlePollingStatus(flowSetting, injectionPathScanClient, job);

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

        SipStatusInfo sipStatusInfo = new SipStatusInfo();
        sipStatusInfo.setModule("HUB");
        sipStatusInfo.setStage("Finished");
        sipStatusInfo.setStatus("SUCCEED");
        when(rosettaWebService.getSIPStatusInfo(sipId)).thenReturn(sipStatusInfo);

        testInstance.handlePollingStatus(flowSetting, injectionPathScanClient, job);

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

        SipStatusInfo sipStatusInfo = new SipStatusInfo();
        sipStatusInfo.setModule("HUB");
        sipStatusInfo.setStage("Finished");
        sipStatusInfo.setStatus("ERROR");
        when(rosettaWebService.getSIPStatusInfo(sipId)).thenReturn(sipStatusInfo);

        testInstance.handlePollingStatus(flowSetting, injectionPathScanClient, job);

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
