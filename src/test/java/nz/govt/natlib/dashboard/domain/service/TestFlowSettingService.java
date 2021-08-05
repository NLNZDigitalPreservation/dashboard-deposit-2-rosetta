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

import java.io.File;
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

        ReflectionTestUtils.setField(testInstance, "rosettaWebService", rosettaWebService);
        ReflectionTestUtils.setField(testInstance, "globalSettingService", globalSettingService);
        ReflectionTestUtils.setField(testInstance, "repoFlowSetting", repoFlowSetting);

        TimerScheduledExecutors timerScheduledExecutors = mock(TimerScheduledExecutors.class);

        ReflectionTestUtils.setField(testInstance, "timerScheduledExecutors", timerScheduledExecutors);
    }

    @Test
    public void testValidateStorage() {
        //Valid Storage Location: NFS
        {
            EntityStorageLocation storage = new EntityStorageLocation();
            storage.setScanMode(InjectionUtils.SCAN_MODE_NFS);
            storage.setRootPath(scanRootPath);

            try {
                FileUtils.forceMkdir(new File(scanRootPath));
                testInstance.validateStorage(storage);
                assert true;
            } catch (NullParameterException | InvalidParameterException | WebServiceException | IOException e) {
                e.printStackTrace();
                assert false;
            }
        }

        //Invalid Storage Location: NFS
        {
            EntityStorageLocation storage = new EntityStorageLocation();
            storage.setScanMode(InjectionUtils.SCAN_MODE_NFS);
            storage.setRootPath(scanRootPath);

            try {
                FileUtils.forceDelete(new File(scanRootPath));
                testInstance.validateStorage(storage);
                assert false;
            } catch (NullParameterException | InvalidParameterException | WebServiceException | IOException e) {
                assert true;
            }
        }
    }

    @Test
    public void testValidateFlowSetting() {
        //Valid flow setting
        {
            EntityStorageLocation injectionEndLocation = new EntityStorageLocation();
            setValues(injectionEndLocation);

            EntityStorageLocation backupEndLocation = new EntityStorageLocation();
            setValues(backupEndLocation);

            EntityFlowSetting flowSetting = new EntityFlowSetting();
            setValues(flowSetting);
            flowSetting.setId(1L);

            try {
                //Set mock data
                when(rosettaWebService.login(any(), any(), any())).thenReturn(pdsHandle);
                when(rosettaWebService.isValidProducer(globalSettingService.getDepositUserName(), flowSetting.getProducerId())).thenReturn(true);
                when(rosettaWebService.isValidMaterialFlow(flowSetting.getProducerId(), flowSetting.getMaterialFlowId())).thenReturn(true);

                testInstance.validateFlowSetting(flowSetting, injectionEndLocation, backupEndLocation);
                assert true;
            } catch (Exception e) {
                e.printStackTrace();
                assert false;
            }
        }

        //Invalid flow setting: invalid user name
        {
            EntityStorageLocation injectionEndLocation = new EntityStorageLocation();
            setValues(injectionEndLocation);
            EntityStorageLocation backupEndLocation = new EntityStorageLocation();
            setValues(backupEndLocation);

            EntityFlowSetting flowSetting = new EntityFlowSetting();
            setValues(flowSetting);

            try {
                //Set mock data
                when(rosettaWebService.login(any(), any(), any())).thenReturn(null);

                testInstance.validateFlowSetting(flowSetting, injectionEndLocation, backupEndLocation);

                Exception e = new InvalidParameterException("Validate user name");
                e.printStackTrace();
                assert false;
            } catch (Exception e) {
                assert true;
            }
        }

        //Invalid flow setting: invalid producer Id
        {
            EntityStorageLocation injectionEndLocation = new EntityStorageLocation();
            setValues(injectionEndLocation);
            EntityStorageLocation backupEndLocation = new EntityStorageLocation();
            setValues(backupEndLocation);

            EntityFlowSetting flowSetting = new EntityFlowSetting();
            setValues(flowSetting);

            try {
                //Set mock data
                when(rosettaWebService.isValidProducer(globalSettingService.getDepositUserName(), flowSetting.getProducerId())).thenReturn(false);

                testInstance.validateFlowSetting(flowSetting, injectionEndLocation, backupEndLocation);

                Exception e = new InvalidParameterException("Validate producer Id");
                e.printStackTrace();
                assert false;
            } catch (Exception e) {
                assert true;
            }
        }

        //Invalid flow setting: invalid material flow id
        {
            EntityStorageLocation injectionEndLocation = new EntityStorageLocation();
            setValues(injectionEndLocation);
            EntityStorageLocation backupEndLocation = new EntityStorageLocation();
            setValues(backupEndLocation);

            EntityFlowSetting flowSetting = new EntityFlowSetting();
            setValues(flowSetting);

            try {
                //Set mock data
                when(rosettaWebService.isValidMaterialFlow(flowSetting.getProducerId(), flowSetting.getMaterialFlowId())).thenReturn(true);

                testInstance.validateFlowSetting(flowSetting, injectionEndLocation, backupEndLocation);

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
        EntityStorageLocation injectionEndLocation = new EntityStorageLocation();
        setValues(injectionEndLocation);
        EntityStorageLocation backupEndLocation = new EntityStorageLocation();
        setValues(backupEndLocation);

        flowSetting.setInjectionEndPoint(injectionEndLocation);
        flowSetting.setBackupEndPoint(backupEndLocation);

        EntityFlowSetting rst = null;
        try {
            when(rosettaWebService.login(anyString(), anyString(), anyString())).thenReturn(UUID.randomUUID().toString());
            when(rosettaWebService.isValidProducer(anyString(), anyString())).thenReturn(true);
            when(rosettaWebService.isValidMaterialFlow(anyString(), anyString())).thenReturn(true);
            rst = testInstance.saveFlowSetting(flowSetting);
            assert rst != null;
            assert !DashboardHelper.isNull(rst.getId());
            assert !DashboardHelper.isNull(rst.getInjectionEndPoint());
            assert !DashboardHelper.isNull(rst.getBackupEndPoint());
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
        EntityStorageLocation injectionEndLocation = new EntityStorageLocation();
        setValues(injectionEndLocation);
        EntityStorageLocation backupEndLocation = new EntityStorageLocation();
        setValues(backupEndLocation);

        flowSetting.setInjectionEndPoint(injectionEndLocation);
        flowSetting.setBackupEndPoint(backupEndLocation);

        EntityFlowSetting rst = null;
        try {
            when(rosettaWebService.login(anyString(), anyString(), anyString())).thenReturn(UUID.randomUUID().toString());
            when(rosettaWebService.isValidProducer(anyString(), anyString())).thenReturn(true);
            when(rosettaWebService.isValidMaterialFlow(anyString(), anyString())).thenReturn(true);
            rst = testInstance.saveFlowSetting(flowSetting);
            assert rst != null;
            assert !DashboardHelper.isNull(rst.getId());
            assert !DashboardHelper.isNull(rst.getInjectionEndPoint());
            assert !DashboardHelper.isNull(rst.getBackupEndPoint());
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }

        flowSetting.setDelays(500L);
        flowSetting.setDelayUnit("D");

        String updatedInjectRootPath = FileUtils.getTempDirectoryPath();
        injectionEndLocation.setRootPath(updatedInjectRootPath);
        String updatedBackupRootPath = FileUtils.getTempDirectoryPath();
        backupEndLocation.setRootPath(updatedBackupRootPath);

        flowSetting.setInjectionEndPoint(injectionEndLocation);
        flowSetting.setBackupEndPoint(backupEndLocation);

        try {
            rst = testInstance.saveFlowSetting(flowSetting);
            assert rst != null;
        } catch (InvalidParameterException | WebServiceException | NullParameterException e) {
            e.printStackTrace();
            assert false;
        }

        EntityFlowSetting flowSettingAfterEdit = repoFlowSetting.getById(rst.getId());
        assert flowSettingAfterEdit != null;
        assert flowSettingAfterEdit.getDelays()==500;
        assert flowSettingAfterEdit.getDelayUnit().equals("D");
        EntityStorageLocation injectionEndLocationAfterEdit = rst.getInjectionEndPoint();
        assert injectionEndLocationAfterEdit != null;
        assert injectionEndLocationAfterEdit.getRootPath().equals(updatedInjectRootPath);
        EntityStorageLocation backupEndLocationAfterEdit =rst.getBackupEndPoint();
        assert backupEndLocationAfterEdit != null;
        assert backupEndLocationAfterEdit.getRootPath().equals(updatedBackupRootPath);
    }


    private void setValues(EntityFlowSetting flowSetting) {
        flowSetting.setEnabled(true);
        flowSetting.setName("Magazine");
        flowSetting.setStreamLocation("content/streams");
        flowSetting.setInjectionCompleteFileName("empty.done");
        flowSetting.setProducerId("0001");
        flowSetting.setProducerName("Producer-Test");
        flowSetting.setMaterialFlowId("12345");
        flowSetting.setMaterialFlowName("Material-Test");

        flowSetting.setDelays(300L);
        flowSetting.setDelayUnit("S");
        flowSetting.setMaxActiveDays(14L);
        flowSetting.setMaxSaveDays(365L);

        flowSetting.setBackupEnabled(false);
    }

    private void setValues(EntityStorageLocation storage) {
        storage.setScanMode(InjectionUtils.SCAN_MODE_NFS);
        storage.setRootPath(testDir.getAbsolutePath());
    }
}
