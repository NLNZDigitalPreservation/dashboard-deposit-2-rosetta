package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.metadata.EnumActualContentDeletionOptions;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;

public class TestScheduleProcessorBasicImplJobInactivePruning extends ScheduleProcessorTester {
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
    public void testPruningInactive() throws Exception {
        flowSetting.setActualContentDeleteOptions(EnumActualContentDeletionOptions.deleteInstantly.name());
        flowSetting.setMaxSaveDays(2L);
        flowSetting.setMaxActiveDays(1L);

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
                File injectionPath = new File(job.getInjectionPath());
                if (!injectionPath.exists()) {
                    injectionPath.mkdirs();
                }
                testInstance.handleFinalize(flowSetting, injectionPathScanClient, job);
                EntityDepositJob jobAfterFinalized = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
                assert jobAfterFinalized != null;
                boolean b = (stage == EnumDepositJobStage.DEPOSIT && state == EnumDepositJobState.SUCCEED)
                        || (stage == EnumDepositJobStage.FINALIZE && state == EnumDepositJobState.INITIALED)
                        || (stage == EnumDepositJobStage.FINALIZE && state == EnumDepositJobState.RUNNING);
                if (b) {
                    assert jobAfterFinalized.getStage() == EnumDepositJobStage.FINISHED;
                    assert jobAfterFinalized.getState() == EnumDepositJobState.SUCCEED;
                }

                ldt = LocalDateTime.now().minusDays(100L);
                job.setFinishedTime(DashboardHelper.getLocalMilliSeconds(ldt));
                repoDepositJob.save(job);
                testInstance.handleInactivePruning(flowSetting, job);
                EntityDepositJob jobAfterPruned = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
                if (b) {
                    assert jobAfterPruned == null;
                }
            }
        }
    }

    @Test
    public void testPruningActive() throws Exception {
        flowSetting.setActualContentDeleteOptions(EnumActualContentDeletionOptions.deleteInstantly.name());
        flowSetting.setMaxSaveDays(2L);
        flowSetting.setMaxActiveDays(1L);

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
                File injectionPath = new File(job.getInjectionPath());
                if (!injectionPath.exists()) {
                    injectionPath.mkdirs();
                }
                testInstance.handleFinalize(flowSetting, injectionPathScanClient, job);
                EntityDepositJob jobAfterFinalized = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
                assert jobAfterFinalized != null;
                if ((stage == EnumDepositJobStage.DEPOSIT && state == EnumDepositJobState.SUCCEED)
                        || (stage == EnumDepositJobStage.FINALIZE && state == EnumDepositJobState.INITIALED)
                        || (stage == EnumDepositJobStage.FINALIZE && state == EnumDepositJobState.RUNNING)) {
                    assert jobAfterFinalized.getStage() == EnumDepositJobStage.FINISHED;
                    assert jobAfterFinalized.getState() == EnumDepositJobState.SUCCEED;
                }

                ldt = LocalDateTime.now();
                job.setFinishedTime(DashboardHelper.getLocalMilliSeconds(ldt));
                repoDepositJob.save(job);
                testInstance.handleInactivePruning(flowSetting, job);
                EntityDepositJob jobAfterPruned = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
                assert jobAfterPruned != null;
            }
        }
    }

    @AfterEach
    public void clear() {
        clearSubFolders();
    }
}
