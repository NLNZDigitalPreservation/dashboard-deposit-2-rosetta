package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.BasicTester;
import nz.govt.natlib.dashboard.common.exception.InvalidParameterException;
import nz.govt.natlib.dashboard.common.exception.NullParameterException;
import nz.govt.natlib.dashboard.common.exception.WebServiceException;
import nz.govt.natlib.dashboard.common.injection.InjectionUtils;
import nz.govt.natlib.dashboard.domain.daemon.TimerScheduledExecutors;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityStorageLocation;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFlowSettingService extends BasicTester {
    private static final FlowSettingService testInstance = new FlowSettingService();

    @BeforeAll
    public static void init() throws IOException {
        BasicTester.init();
        BasicTester.initSubFolder();

        ReflectionTestUtils.setField(testInstance, "rosettaWebService", rosettaWebService);
        ReflectionTestUtils.setField(testInstance, "repoDepositAccount", repoDepositAccount);
        ReflectionTestUtils.setField(testInstance, "repoFlowSetting", repoFlowSetting);

        TimerScheduledExecutors timerScheduledExecutors = mock(TimerScheduledExecutors.class);

        ReflectionTestUtils.setField(testInstance, "timerScheduledExecutors", timerScheduledExecutors);
    }


    @Test
    public void testValidateFlowSetting() {
        //Valid flow setting
        {

            EntityFlowSetting flowSetting = new EntityFlowSetting();
            setValues(flowSetting);
            flowSetting.setId(1L);

            try {
                //Set mock data
                when(rosettaWebService.login(any(), any(), any())).thenReturn(pdsHandle);
                when(rosettaWebService.isValidProducer("serverside", flowSetting.getProducerId())).thenReturn(true);
                when(rosettaWebService.isValidMaterialFlow(flowSetting.getProducerId(), flowSetting.getMaterialFlowId())).thenReturn(true);

                testInstance.validateFlowSetting(flowSetting);
                assert true;
            } catch (Exception e) {
                e.printStackTrace();
                assert false;
            }
        }

        //Invalid flow setting: invalid user name
        {
            EntityFlowSetting flowSetting = new EntityFlowSetting();
            setValues(flowSetting);

            try {
                //Set mock data
                when(rosettaWebService.login(any(), any(), any())).thenReturn(null);

                testInstance.validateFlowSetting(flowSetting);

                Exception e = new InvalidParameterException("Validate user name");
                e.printStackTrace();
                assert false;
            } catch (Exception e) {
                assert true;
            }
        }

        //Invalid flow setting: invalid producer Id
        {
            EntityFlowSetting flowSetting = new EntityFlowSetting();
            setValues(flowSetting);

            try {
                //Set mock data
                when(rosettaWebService.isValidProducer("serverside", flowSetting.getProducerId())).thenReturn(false);

                testInstance.validateFlowSetting(flowSetting);

                Exception e = new InvalidParameterException("Validate producer Id");
                e.printStackTrace();
                assert false;
            } catch (Exception e) {
                assert true;
            }
        }

        //Invalid flow setting: invalid material flow id
        {
            EntityFlowSetting flowSetting = new EntityFlowSetting();
            setValues(flowSetting);

            try {
                //Set mock data
                when(rosettaWebService.isValidMaterialFlow(flowSetting.getProducerId(), flowSetting.getMaterialFlowId())).thenReturn(true);

                testInstance.validateFlowSetting(flowSetting);

                Exception e = new InvalidParameterException("Validate material flow Id");
                e.printStackTrace();
                assert false;
            } catch (Exception e) {
                assert true;
            }
        }
    }

    @Test
    public void testSaveNewFlowSetting() {
        EntityFlowSetting flowSetting = new EntityFlowSetting();
        setValues(flowSetting);

        EntityFlowSetting rst = null;
        try {
            when(rosettaWebService.login(anyString(), anyString(), anyString())).thenReturn(UUID.randomUUID().toString());
            when(rosettaWebService.isValidProducer(anyString(), anyString())).thenReturn(true);
            when(rosettaWebService.isValidMaterialFlow(anyString(), anyString())).thenReturn(true);
            rst = testInstance.saveFlowSetting(flowSetting);
            assert rst != null;
            assert !DashboardHelper.isNull(rst.getId());
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testSaveOldFlowSetting() {
        EntityFlowSetting flowSetting = new EntityFlowSetting();
        setValues(flowSetting);
        flowSetting.setId(1L);

        EntityFlowSetting rst = null;
        try {
            when(rosettaWebService.login(anyString(), anyString(), anyString())).thenReturn(UUID.randomUUID().toString());
            when(rosettaWebService.isValidProducer(anyString(), anyString())).thenReturn(true);
            when(rosettaWebService.isValidMaterialFlow(anyString(), anyString())).thenReturn(true);
            rst = testInstance.saveFlowSetting(flowSetting);
            assert rst != null;
            assert !DashboardHelper.isNull(rst.getId());
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }

        flowSetting.setDelays(500L);
        flowSetting.setDelayUnit("D");
        flowSetting.setRootPath(FileUtils.getTempDirectoryPath());

        try {
            rst = testInstance.saveFlowSetting(flowSetting);
            assert rst != null;
        } catch (InvalidParameterException | WebServiceException | NullParameterException e) {
            e.printStackTrace();
            assert false;
        }

        EntityFlowSetting flowSettingAfterEdit = repoFlowSetting.getById(rst.getId());
        assert flowSettingAfterEdit != null;
        assert flowSettingAfterEdit.getDelays() == 500;
        assert flowSettingAfterEdit.getDelayUnit().equals("D");
    }


    private void setValues(EntityFlowSetting flowSetting) {
        flowSetting.setEnabled(true);
        flowSetting.setDepositAccountId(Long.parseLong("0"));
        flowSetting.setStreamLocation("content/streams");
        flowSetting.setRootPath(scanRootPath);
        flowSetting.setInjectionCompleteFileName("empty.done");
        flowSetting.setProducerId("0001");
        flowSetting.setProducerName("Producer-Test");
        flowSetting.setMaterialFlowId("12345");
        flowSetting.setMaterialFlowName("Material-Test");

        flowSetting.setDelays(300L);
        flowSetting.setDelayUnit("S");
        flowSetting.setMaxActiveDays(14L);
        flowSetting.setMaxSaveDays(365L);
    }

    private void setValues(EntityStorageLocation storage) {
        storage.setScanMode(InjectionUtils.SCAN_MODE_NFS);
        storage.setRootPath(testDir.getAbsolutePath());
    }
}
