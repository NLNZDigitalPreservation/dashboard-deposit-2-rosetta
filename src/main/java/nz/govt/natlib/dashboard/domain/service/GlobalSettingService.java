package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.daemon.TimerScheduledExecutors;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoGlobalSetting;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GlobalSettingService {
    private static final Logger log = LoggerFactory.getLogger(GlobalSettingService.class);
    private static final long UNIQUE_GLOBAL_SETTING_ID = 0;
    @Autowired
    private RepoGlobalSetting repoGlobalSetting;
    @Autowired
    private TimerScheduledExecutors timerScheduledExecutors;

    public RestResponseCommand getGlobalSetting() {
        RestResponseCommand rstVal = new RestResponseCommand();
        EntityGlobalSetting globalSetting = repoGlobalSetting.getGlobalSetting();
        LocalDateTime ldtNowDatetime = LocalDateTime.now();
        if (globalSetting == null) {
            globalSetting = new EntityGlobalSetting();
            globalSetting.setId(UNIQUE_GLOBAL_SETTING_ID);
            globalSetting.setPausedStartTime(ldtNowDatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).substring(0, 16));
            LocalDateTime ldtPausedEndTime = ldtNowDatetime.plusDays(1);
            globalSetting.setPausedEndTime(ldtPausedEndTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).substring(0, 16));
        } else {
            try {
                LocalDateTime.parse(globalSetting.getPausedStartTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                log.warn("Failed to parse pausedStartTime: {}", globalSetting.getPausedStartTime(), e);
                globalSetting.setPausedStartTime(ldtNowDatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).substring(0, 16));
            }

            LocalDateTime ldtPausedEndTime;
            try {
                ldtPausedEndTime = LocalDateTime.parse(globalSetting.getPausedEndTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                log.warn("Failed to parse pausedEndTime: {}", globalSetting.getPausedEndTime(), e);
                ldtPausedEndTime = ldtNowDatetime.plusDays(1);
                globalSetting.setPausedEndTime(ldtPausedEndTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).substring(0, 16));
            }

            if (ldtPausedEndTime.isBefore(ldtNowDatetime)) {
                globalSetting.setPaused(false);
                //globalSetting.setPausedStartTime(strNowDatetime);
                //globalSetting.setPausedEndTime(strNowDatetime);
            }
        }
        rstVal.setRspBody(globalSetting);
        return rstVal;
    }

    public RestResponseCommand saveGlobalSetting(EntityGlobalSetting globalSetting) {
        RestResponseCommand rstVal = new RestResponseCommand();
        if (globalSetting.isPaused()) {
            if (StringUtils.isEmpty(globalSetting.getPausedStartTime())) {
                rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
                rstVal.setRspMsg("The start time is empty.");
                return rstVal;
            }
            if (StringUtils.isEmpty(globalSetting.getPausedEndTime())) {
                rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
                rstVal.setRspMsg("The end time is empty.");
                return rstVal;
            }
            LocalDateTime ldtNowDatetime = LocalDateTime.now();
            LocalDateTime ldtPausedStartTime = LocalDateTime.parse(globalSetting.getPausedStartTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime ldtPausedEndTime = LocalDateTime.parse(globalSetting.getPausedEndTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            if (ldtPausedEndTime.isBefore(ldtNowDatetime)) {
                rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
                rstVal.setRspMsg("The end time must after now.");
                return rstVal;
            }

            if (ldtPausedEndTime.isBefore(ldtPausedStartTime)) {
                rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
                rstVal.setRspMsg("The end time must after the start time.");
                return rstVal;
            }
        }

        try {
            DashboardHelper.assertNotNull("Delays", globalSetting.getDelays());
            DashboardHelper.assertNotNull("DelayUnit", globalSetting.getDelayUnit());
        } catch (Exception e) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg(e.getMessage());
            return rstVal;
        }

        globalSetting.setId(UNIQUE_GLOBAL_SETTING_ID);
        repoGlobalSetting.save(globalSetting);

        //Rescheduling or adding the existing timer
        timerScheduledExecutors.rescheduleProcessor();

        rstVal.setRspBody(globalSetting);

        return rstVal;
    }

    public void setRepoGlobalSetting(RepoGlobalSetting repoGlobalSetting) {
        this.repoGlobalSetting = repoGlobalSetting;
    }

    public void setTimerScheduledExecutors(TimerScheduledExecutors timerScheduledExecutors) {
        this.timerScheduledExecutors = timerScheduledExecutors;
    }
}
