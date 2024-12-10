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
        flowSetting.setActualContentDeleteOptions(EnumActualContentDeletionOptions.deleteInstantly.name());
        flowSetting.setMaxSaveDays(2L);

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
            }
        }
    }

    @Test
    public void testFinalizeCancelledSuccess() throws Exception {
        flowSetting.setActualContentDeleteOptions(EnumActualContentDeletionOptions.deleteExceedMaxStorageDays.name());
        flowSetting.setMaxSaveDays(2L);

        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        job.setStage(EnumDepositJobStage.DEPOSIT);
        job.setState(EnumDepositJobState.CANCELED);
        job.setBackupCompleted(false);
        job.setActualContentDeleted(false);

        LocalDateTime ldt = LocalDateTime.now();
//        ldt = ldt.minusDays(1000);
        job.setLatestTime(DashboardHelper.getLocalMilliSeconds(ldt));
        repoDepositJob.save(job);

        //Finalizing
        testInstance.handleFinalize(flowSetting, injectionPathScanClient, job);
        assert job.getStage() == EnumDepositJobStage.FINALIZE; //Not finished because the maxStorageDays is not reached
        assert job.getState() == EnumDepositJobState.CANCELED;

        ldt = ldt.minusDays(1000);
        job.setLatestTime(DashboardHelper.getLocalMilliSeconds(ldt));
        repoDepositJob.save(job);

        //Finalizing
        testInstance.handleFinalize(flowSetting, injectionPathScanClient, job);
        EntityDepositJob jobAfterFinalized = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert jobAfterFinalized != null;
        assert jobAfterFinalized.getStage() == EnumDepositJobStage.FINISHED;
        assert job.getState() == EnumDepositJobState.CANCELED;
    }

    @Test
    public void testFinalizeCancelledExpired() throws Exception {
        flowSetting.setActualContentDeleteOptions(EnumActualContentDeletionOptions.deleteExceedMaxStorageDays.name());
        flowSetting.setMaxSaveDays(2L);

        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        for (EnumDepositJobStage stage : EnumDepositJobStage.values()) {
            EnumDepositJobState state = EnumDepositJobState.CANCELED;
            job.setStage(stage);
            job.setState(state);
            LocalDateTime ldt = LocalDateTime.now();
            ldt = ldt.minusDays(3L);
//            job.setLatestTime(ldt.toEpochSecond(ZoneOffset.UTC));
            job.setLatestTime(DashboardHelper.getLocalMilliSeconds(ldt));
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
            assert jobAfterFinalized.getStage() == EnumDepositJobStage.FINISHED;
            assert jobAfterFinalized.getState() == EnumDepositJobState.CANCELED;
        }
    }

    @Test
    public void testStringToLines() {
        String subFolders = "Sidecar files";
        subFolders.lines().forEach(System.out::println);
        Object[] subFolderAry = subFolders.lines().toArray();
//        String[] subFolderAry = (String[]) subFolders.lines().toArray();
        assert subFolderAry.length > 0;
    }


    @AfterEach
    public void clear() {
        clearSubFolders();
    }
}
