package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestScheduleProcessorImplJobFinalizing extends ScheduleProcessorTester {
    private ScheduleProcessor testInstance = new ScheduleProcessorImplJobFinalizing();

    @BeforeEach
    public void clearAndInit() throws Exception {
        initProcessor(testInstance);

        initSubFolder();

        //Initial injection
        ScheduleProcessor injectionProcessor = new ScheduleProcessorImplJobInjecting();
        initProcessor(injectionProcessor);
        addReadyForIngestionFile();
        injectionProcessor.handle(flowSetting);
    }


    @Test
    public void testFinalizeSuccess() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        for (EnumDepositJobStage stage : EnumDepositJobStage.values()) {
            for (EnumDepositJobState state : EnumDepositJobState.values()) {
                job.setStage(stage);
                job.setState(state);
                repoDepositJob.save(job);

                //Finalizing
                testInstance.handle(flowSetting);

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

    @AfterEach
    public void clear() {
        clearSubFolders();
    }
}
