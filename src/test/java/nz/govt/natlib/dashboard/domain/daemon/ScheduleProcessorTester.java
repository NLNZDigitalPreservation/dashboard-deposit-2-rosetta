package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.BasicTester;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityStorageLocation;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;

public class ScheduleProcessorTester extends BasicTester {
    protected static final String flowName = "Magazine";
    protected static final boolean flowEnable = true;
    protected static final String scanMode = "NFS";

    protected static final String materialFlowId = "31072513794";
    protected static final boolean backupEnable = true;
    protected static final String backupMode = "NFS";
    protected static final String backupRootPath = new File(testDir, "magazine_backup").getAbsolutePath();

    protected static final Long maxActiveDays = 14L;
    protected static final Long maxSaveDays = 180L;

    protected static final EntityFlowSetting flowSetting = new EntityFlowSetting();
    protected static final EntityStorageLocation storageInjection = new EntityStorageLocation();
    protected static final EntityStorageLocation storageBackup = new EntityStorageLocation();

    @BeforeAll
    public static void init() throws IOException {
        BasicTester.init();

        //Initial flowSetting
        storageInjection.setId(repoStorageLocation.nextId());
        storageInjection.setScanMode(scanMode);
        storageInjection.setRootPath(scanRootPath);
        repoStorageLocation.save(storageInjection);
        flowSetting.setInjectionEndPointId(storageInjection.getId());

        storageBackup.setId(repoStorageLocation.nextId());
        storageBackup.setScanMode(backupMode);
        storageBackup.setRootPath(backupRootPath);
        repoStorageLocation.save(storageBackup);
        flowSetting.setBackupEndPointId(storageBackup.getId());

        flowSetting.setId(repoFlowSetting.nextId());
        flowSetting.setName(flowName);
        flowSetting.setEnabled(flowEnable);
        flowSetting.setMaterialFlowId(materialFlowId);
        flowSetting.setStreamLocation(streamPath);
        flowSetting.setInjectionCompleteFileName(completedFileName);
        flowSetting.setBackupEnabled(backupEnable);
        flowSetting.setMaxActiveDays(maxActiveDays);
        flowSetting.setMaxSaveDays(maxSaveDays);

        int[] maxWeeklyMaxConcurrency = flowSetting.getWeeklyMaxConcurrency();
        for (int j = 0; j < 7; j++) {
            maxWeeklyMaxConcurrency[j] = 1;
        }

        repoFlowSetting.save(flowSetting);
    }

    public void initProcessor(ScheduleProcessor processor) {
        processor.setDepositJobService(depositJobService);
        processor.setGlobalSettingService(globalSettingService);
        processor.setRepoDepositJobActive(repoDepositJobActive);
        processor.setRepoDepositJobHistory(repoDepositJobHistory);
        processor.setRepoFlowSetting(repoFlowSetting);
        processor.setRepoFTPSetting(repoStorageLocation);
        processor.setRosettaWebService(rosettaWebService);
    }
}
