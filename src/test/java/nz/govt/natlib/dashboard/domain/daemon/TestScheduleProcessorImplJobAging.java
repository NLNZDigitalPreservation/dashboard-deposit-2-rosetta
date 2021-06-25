package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;

public class TestScheduleProcessorImplJobAging extends ScheduleProcessorTester {
    private ScheduleProcessor testInstance = new ScheduleProcessorImplJobAging();

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
    public void testAgingNotExpired() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        for (EnumDepositJobStage stage : EnumDepositJobStage.values()) {
            for (EnumDepositJobState state : EnumDepositJobState.values()) {
                job.setStage(stage);
                job.setState(state);
                repoDepositJob.save(job);

                //Aging
                testInstance.handle(flowSetting);

                EntityDepositJob jobAfterFinalized = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);

                //Do nothing because it isn't expired
                assert jobAfterFinalized != null;
                assert jobAfterFinalized.getStage() == stage;
                assert jobAfterFinalized.getState() == state;
            }
        }
    }

    @Test
    public void testAgingExpired() throws Exception {
        EntityDepositJob job = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);
        assert job != null;

        for (EnumDepositJobStage stage : EnumDepositJobStage.values()) {
            for (EnumDepositJobState state : EnumDepositJobState.values()) {
                repoDepositJob.deleteAll();

                LocalDateTime oldTime = LocalDateTime.now().minusDays(2 * flowSetting.getMaxActiveDays());
                long oldMilliSeconds = DashboardHelper.getLocalMilliSeconds(oldTime);
                job.setInitialTime(oldMilliSeconds);
                job.setLatestTime(oldMilliSeconds);

                job.setStage(stage);
                job.setState(state);
                repoDepositJob.save(job);

                //Aging
                testInstance.handle(flowSetting);

                EntityDepositJob jobAfterFinalized = repoDepositJob.getByFlowIdAndInjectionTitle(flowSetting.getId(), subFolderName);

                if ((stage == EnumDepositJobStage.FINALIZE && state == EnumDepositJobState.SUCCEED)
                        || state == EnumDepositJobState.CANCELED) {

                    assert jobAfterFinalized != null;

                    File subFolder = new File(job.getInjectionPath());
                    assert !subFolder.exists();

                    File backupFolder = new File(storageBackup.getRootPath(), subFolderName);
                    assert backupFolder.exists();

                    File targetDirStream = new File(backupFolder, streamPath);
                    File f1 = new File(targetDirStream, testFileName_1);
                    File f2 = new File(targetDirStream, testFileName_2);
                    File metsXml = new File(targetDirStream.getParent(), metsXmlFileName);

                    assert f1.exists();
                    assert f1.length() == testFileLength_1;
                    assert f2.exists();
                    assert f2.length() == testFileLength_2;
                    assert metsXml.exists();
                } else {
                    assert jobAfterFinalized != null;
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
