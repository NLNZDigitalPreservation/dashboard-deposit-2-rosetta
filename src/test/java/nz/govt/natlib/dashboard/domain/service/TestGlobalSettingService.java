package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.BasicTester;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.daemon.TimerScheduledExecutors;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class TestGlobalSettingService extends BasicTester {
    private static final GlobalSettingService testInstance = new GlobalSettingService();

    @BeforeAll
    public static void init() throws IOException {
        BasicTester.init();
        BasicTester.initSubFolder();

        ReflectionTestUtils.setField(testInstance, "repoGlobalSetting", repoGlobalSetting);
    }

    @Test
    public void testValidatePara() {
        //Valid global setting
        {
            LocalDateTime ldtNowDatetime = LocalDateTime.now();
            LocalDateTime ldtEndDatetime = ldtNowDatetime.plusDays(1L);
            EntityGlobalSetting globalSetting = new EntityGlobalSetting();
            globalSetting.setPaused(false);
            globalSetting.setPausedStartTime(ldtNowDatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            globalSetting.setPausedEndTime(ldtEndDatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            globalSetting.setDelays(300L);
            globalSetting.setDelayUnit("S");
            try{
                // EntityGlobalSetting
                testInstance.saveGlobalSetting(globalSetting);
            }catch(Exception e){
                assert(false);
            }
        }

        //Valid global setting
        {
            LocalDateTime ldtNowDatetime = LocalDateTime.now();
            LocalDateTime ldtEndDatetime = ldtNowDatetime.plusDays(1L);
            EntityGlobalSetting globalSetting = new EntityGlobalSetting();
            globalSetting.setPaused(false);
            globalSetting.setPausedStartTime(ldtNowDatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            globalSetting.setPausedEndTime(ldtEndDatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            globalSetting.setDelays(300L);
            globalSetting.setDelayUnit("");
            try{
                // EntityGlobalSetting
                testInstance.saveGlobalSetting(globalSetting);
                assert(false);
            }catch(Exception e){
                assert(true);
            }
        }

        //Valid global setting
        {
            LocalDateTime ldtNowDatetime = LocalDateTime.now().plusDays(2L);
            LocalDateTime ldtEndDatetime = ldtNowDatetime.minusDays(1L);
            EntityGlobalSetting globalSetting = new EntityGlobalSetting();
            globalSetting.setPaused(true);
            globalSetting.setPausedStartTime(ldtNowDatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            globalSetting.setPausedEndTime(ldtEndDatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            globalSetting.setDelays(300L);
            globalSetting.setDelayUnit("S");
            try{
                // EntityGlobalSetting
                testInstance.saveGlobalSetting(globalSetting);
                assert(false);
            }catch(Exception e){
                assert(true);
            }
        }

        //Valid global setting
        {
            LocalDateTime ldtNowDatetime = LocalDateTime.now().minusDays(2L);
            LocalDateTime ldtEndDatetime = ldtNowDatetime.minusDays(1L);
            EntityGlobalSetting globalSetting = new EntityGlobalSetting();
            globalSetting.setPaused(true);
            globalSetting.setPausedStartTime(ldtNowDatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            globalSetting.setPausedEndTime(ldtEndDatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            globalSetting.setDelays(300L);
            globalSetting.setDelayUnit("S");
            try{
                // EntityGlobalSetting
                testInstance.saveGlobalSetting(globalSetting);
                assert(false);
            }catch(Exception e){
                assert(true);
            }
        }
    }
}
