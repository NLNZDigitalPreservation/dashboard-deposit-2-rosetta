package nz.govt.natlib.dashboard.domain.service;

import com.exlibris.dps.SipStatusInfo;
import nz.govt.natlib.dashboard.common.BasicTester;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityStorageLocation;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import nz.govt.natlib.ndha.common.exlibris.ResultOfDeposit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TestDepositJobService extends BasicTester {
    private static final Logger log = LoggerFactory.getLogger(TestDepositJobService.class);

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
        storageInjection.setScanMode(scanMode);
        storageInjection.setRootPath(scanRootPath);
        flowSetting.setInjectionEndPoint(storageInjection);

        storageBackup.setScanMode(backupMode);
        storageBackup.setRootPath(backupRootPath);
        flowSetting.setBackupEndPoint(storageBackup);

//        flowSetting.setId(repoFlowSetting.nextId());
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

    //    @Test
    @Disabled
    @Test
    public void testDeposit() {
        String institution = "INS00";
        String username = "leefr";
        String password = "******";

        String pdsHandle = null;
        try {
            when(rosettaWebService.login(anyString(), anyString(), anyString())).thenReturn(DashboardHelper.getUid());
            pdsHandle = rosettaWebService.login(institution, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }

        String producerId = "6355932079";
        String materialFlowId = "31072513794";

        {
            String injectionPath = "20201023_GTG_parent_grouping__Get_Growing";
            ResultOfDeposit resultOfDeposit;
            try {
                when(rosettaWebService.deposit(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(new ResultOfDeposit(true, "OK", "12345"));
                resultOfDeposit = rosettaWebService.deposit(injectionPath, pdsHandle, institution, producerId, materialFlowId, "1");
                assert resultOfDeposit.isSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                assert false;
            }
        }

        {
            String injectionPath = "/ndha/pre-deposit/frank/magazine/20201023_GTG_parent_grouping__Get_Growing";

            ResultOfDeposit resultOfDeposit = null;
            try {
                resultOfDeposit = rosettaWebService.deposit(injectionPath, pdsHandle, institution, producerId, materialFlowId, "1");
            } catch (Exception e) {
                e.printStackTrace();
                assert false;
            }
            if (resultOfDeposit.isSuccess()) {
                log.info("Job [{}] is submitted to Rosetta successfully, SIPId=[{}], Status=[{}]",
                        injectionPath, resultOfDeposit.getSipID(), resultOfDeposit.getResultMessage());
            } else {
                log.warn("Job [{}] is submitted to Rosetta failed, msg: [{}]", injectionPath, resultOfDeposit.getResultMessage());
            }
        }
    }

    @Test
    public void testUpdateStatus() {
        String sipId = "744722";
        SipStatusInfo sipStatusInfo = new SipStatusInfo();
        sipStatusInfo.setModule("DEP");
        sipStatusInfo.setStage("Finished");
        sipStatusInfo.setStatus("Finished");
        try {
            when(rosettaWebService.getSIPStatusInfo(sipId)).thenReturn(sipStatusInfo);
            sipStatusInfo = rosettaWebService.getSIPStatusInfo(sipId);
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }

        log.info("Sip: {}, Status: {} {} {}", sipId, sipStatusInfo.getModule(), sipStatusInfo.getStage(), sipStatusInfo.getStatus());
    }

    @Disabled
    @Test
    public void testManualSubmitDepositJob() {
        EntityFlowSetting flowSetting = repoFlowSetting.getAll().get(0);
//        String srcDirectory = "C:\\temp\\MPRESS_deposit105";
//        String srcDirectory="//wlgprdfile13/DFS_Shares/ndha/pre-deposit_prod/frank/PP_Example/MPRESS_deposit105";
        String srcDirectory = "/media/sf_frank/TEST_DATA/20201022_TVG_parent_grouping__TV_Guide";
        RestResponseCommand rstVal = depositJobService.manuallySubmitDepositJob(flowSetting.getId(), srcDirectory, false);
        assert rstVal.getRspCode() == RestResponseCommand.RSP_SUCCESS;
    }
}
