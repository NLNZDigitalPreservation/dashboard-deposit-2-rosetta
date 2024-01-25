package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TestScheduleProcessorBasicImplJobFinalizing extends ScheduleProcessorTester {
    private final ScheduleProcessorBasic testInstance = new ScheduleProcessorImpl();

    @BeforeEach
    public void clearAndInit() {
        initProcessor(testInstance);

        initSubFolder();

        //Initial injection
        ScheduleProcessorBasic injectionProcessor = new ScheduleProcessorImpl();
        initProcessor(injectionProcessor);
        addReadyForIngestionFile();
        injectionProcessor.handleIngest();
    }


    @Test
    public void testFinalizeSuccess() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        for (EnumDepositJobStage stage : EnumDepositJobStage.values()) {
            for (EnumDepositJobState state : EnumDepositJobState.values()) {
                job.setStage(stage);
                job.setState(state);
                LocalDateTime ldt = LocalDateTime.now();
                job.setLatestTime(DashboardHelper.getLocalMilliSeconds(ldt));
                repoDepositJob.save(job);

                //Finalizing
                testInstance.handleFinalize(flowSetting, injectionPathScanClient, job);

                EntityDepositJob jobAfterFinalized = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
                assert jobAfterFinalized != null;
                if ((stage == EnumDepositJobStage.DEPOSIT && state == EnumDepositJobState.SUCCEED)
                        || (stage == EnumDepositJobStage.FINALIZE && state == EnumDepositJobState.INITIALED)
                        || (stage == EnumDepositJobStage.FINALIZE && state == EnumDepositJobState.RUNNING)) {
                    assert jobAfterFinalized.getStage() == EnumDepositJobStage.FINISHED;
                    assert jobAfterFinalized.getState() == EnumDepositJobState.SUCCEED;
                } else {
                    assert jobAfterFinalized.getStage() == stage;
                    assert jobAfterFinalized.getState() == state;
                }
            }
        }
    }

    @Test
    public void testFinalizeCancelledSuccess() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.CANCELED);
        LocalDateTime ldt = LocalDateTime.now();
        ldt = ldt.minusDays(1000);
        job.setLatestTime(DashboardHelper.getLocalMilliSeconds(ldt));
        repoDepositJob.save(job);

        //Finalizing
        testInstance.handleFinalize(flowSetting, injectionPathScanClient, job);

        assert job.getStage() == EnumDepositJobStage.FINISHED;
        assert job.getState() == EnumDepositJobState.CANCELED;


        ldt = ldt.minusDays(1000);
        job.setLatestTime(DashboardHelper.getLocalMilliSeconds(ldt));
        //Finalizing
        testInstance.handleFinalize(flowSetting, injectionPathScanClient, job);
        EntityDepositJob jobAfterFinalized = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert jobAfterFinalized == null;
    }

    @Test
    public void testFinalizeCancelledExpired() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        for (EnumDepositJobStage stage : EnumDepositJobStage.values()) {
            EnumDepositJobState state = EnumDepositJobState.CANCELED;
            job.setStage(stage);
            job.setState(state);
            LocalDateTime ldt = LocalDateTime.now().minusMonths(3L);
            job.setLatestTime(ldt.toEpochSecond(ZoneOffset.UTC));
            repoDepositJob.save(job);

            //Finalizing
            testInstance.handleFinalize(flowSetting, injectionPathScanClient, job);

            EntityDepositJob jobAfterFinalized = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
            assert jobAfterFinalized != null;
            assert jobAfterFinalized.getStage() == EnumDepositJobStage.FINISHED;
            assert jobAfterFinalized.getState() == EnumDepositJobState.CANCELED;
        }
    }

    @Test
    public void testFinalizeCancelledNoneExpired() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        for (EnumDepositJobStage stage : EnumDepositJobStage.values()) {
            EnumDepositJobState state = EnumDepositJobState.CANCELED;
            job.setStage(stage);
            job.setState(state);
            repoDepositJob.save(job);

            //Finalizing
            testInstance.handleFinalize(flowSetting, injectionPathScanClient, job);

            EntityDepositJob jobAfterFinalized = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
            assert jobAfterFinalized != null;
            assert jobAfterFinalized.getStage() == stage;
            assert jobAfterFinalized.getState() == EnumDepositJobState.CANCELED;
        }
    }

    @AfterEach
    public void clear() {
        clearSubFolders();
    }
}
