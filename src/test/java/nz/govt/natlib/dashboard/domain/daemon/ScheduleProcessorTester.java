package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.BasicTester;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

public class ScheduleProcessorTester extends BasicTester {
    protected static final boolean flowEnable = true;

    protected static final String materialFlowId = "31072513794";
    protected static final Long maxActiveDays = 14L;
    protected static final Long maxSaveDays = 180L;

    protected static final EntityFlowSetting flowSetting = new EntityFlowSetting();

    @BeforeAll
    public static void init() throws IOException {
        BasicTester.init();

        //Initial flowSetting

//        flowSetting.setId(repoFlowSetting.nextId());
        flowSetting.setDepositAccountId(Long.parseLong("0"));
        flowSetting.setEnabled(flowEnable);
        flowSetting.setMaterialFlowId(materialFlowId);
        flowSetting.setRootPath(scanRootPath);
        flowSetting.setStreamLocation(streamPath);
        flowSetting.setInjectionCompleteFileName(completedFileName);
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
        processor.setRepoDepositAccount(repoDepositAccount);
        processor.setRepoDepositJob(repoDepositJob);
        processor.setRepoFlowSetting(repoFlowSetting);
        processor.setRosettaWebService(rosettaWebService);
    }
}
