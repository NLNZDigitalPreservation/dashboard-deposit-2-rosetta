package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.util.DashboardHelper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class TestScheduleProcessorImplJobHistoryPruning extends ScheduleProcessorTester {
    private ScheduleProcessor testInstance = new ScheduleProcessorImplJobHistoryPruning();

    @BeforeEach
    public void clearAndInit() throws Exception {
        repoDepositJob.deleteAll();

        initProcessor(testInstance);

        initSubFolder();

        //Initial injection
        ScheduleProcessor injectionProcessor = new ScheduleProcessorImplJobInjecting();
        initProcessor(injectionProcessor);
        addReadyForIngestionFile();
        injectionProcessor.handle(flowSetting);
    }

    @Test
    public void testPruningNotExpired() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        //Pruning
        LocalDateTime ldt=LocalDateTime.now();
        ldt=ldt.minusYears(5);
        job.setLatestTime(DashboardHelper.getLocalMilliSeconds(ldt));
        repoDepositJob.save(job);

        testInstance.handle(flowSetting);

        job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);

        //Do nothing because it isn't expired
        assert job == null;
    }

    @Test
    public void testAgingExpired() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;


        LocalDateTime oldTime = LocalDateTime.now().minusDays(2 * flowSetting.getMaxSaveDays());
        long oldMilliSeconds = DashboardHelper.getLocalMilliSeconds(oldTime);
        job.setInitialTime(oldMilliSeconds);
        job.setLatestTime(oldMilliSeconds);

        //Added to history
        repoDepositJob.save(job);

        //Pruning
        testInstance.handle(flowSetting);

        job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);

        assert job == null;
    }

    @AfterEach
    public void clear() {
        clearSubFolders();
    }
}
