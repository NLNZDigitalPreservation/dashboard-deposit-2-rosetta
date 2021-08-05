package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

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
