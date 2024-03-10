package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.util.DashboardHelper;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class TestScheduleProcessorBasicImplJobHistoryPruning extends ScheduleProcessorTester {
    private final ScheduleProcessorBasic testInstance = new ScheduleProcessorImpl();

    @BeforeEach
    public void clearAndInit() {
        repoDepositJob.deleteAll();

        initProcessor(testInstance);

        initSubFolder();

        //Initial injection
        ScheduleProcessorBasic injectionProcessor = new ScheduleProcessorImpl();
        initProcessor(injectionProcessor);
        addReadyForIngestionFile();
        injectionProcessor.handleIngest();
    }

//    @Ignore
//    @Test
    public void testPruningNotExpired() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        //Pruning
        LocalDateTime ldt = LocalDateTime.now();
        job.setLatestTime(DashboardHelper.getLocalMilliSeconds(ldt));
        job.setStage(EnumDepositJobStage.FINISHED);
        job.setState(EnumDepositJobState.SUCCEED);
        repoDepositJob.save(job);

        testInstance.handleHistoryPruning(flowSetting, injectionPathScanClient, job);

        job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);

        //Do nothing because it isn't expired
        assert job != null;
    }

//    @Test
//    @Ignore
    public void testAgingExpired() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;


        LocalDateTime oldTime = LocalDateTime.now().minusDays(2 * flowSetting.getMaxSaveDays());
        long oldMilliSeconds = DashboardHelper.getLocalMilliSeconds(oldTime);
        job.setInitialTime(oldMilliSeconds);
        job.setLatestTime(oldMilliSeconds);
        job.setStage(EnumDepositJobStage.FINISHED);
        job.setState(EnumDepositJobState.SUCCEED);

        //Added to history
        repoDepositJob.save(job);

        //Pruning
        testInstance.handleHistoryPruning(flowSetting, injectionPathScanClient, job);

        job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);

        assert job == null;
    }

    @AfterEach
    public void clear() {
        clearSubFolders();
    }
}
