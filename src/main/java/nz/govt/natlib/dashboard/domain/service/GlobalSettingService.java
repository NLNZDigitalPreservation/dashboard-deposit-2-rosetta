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

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GlobalSettingService {
    private static final Logger log = LoggerFactory.getLogger(GlobalSettingService.class);
    private static final long UNIQUE_GLOBAL_SETTING_ID = 0;
    @Autowired
    private RepoGlobalSetting repoGlobalSetting;

    public EntityGlobalSetting getGlobalSetting() throws Exception {
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
        return globalSetting;
    }

    public EntityGlobalSetting saveGlobalSetting(EntityGlobalSetting globalSetting) throws Exception {
        if (globalSetting.isPaused()) {
            if (StringUtils.isEmpty(globalSetting.getPausedStartTime())) {               
                throw new InvalidParameterException("The start time is empty: "+RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            }
            if (StringUtils.isEmpty(globalSetting.getPausedEndTime())) {
                throw new InvalidParameterException("The end time is empty: "+RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            }
            LocalDateTime ldtNowDatetime = LocalDateTime.now();
            LocalDateTime ldtPausedStartTime = LocalDateTime.parse(globalSetting.getPausedStartTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime ldtPausedEndTime = LocalDateTime.parse(globalSetting.getPausedEndTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            if (ldtPausedEndTime.isBefore(ldtNowDatetime)) {
                throw new InvalidParameterException("The end time must after now: "+RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            }

            if (ldtPausedEndTime.isBefore(ldtPausedStartTime)) {
                throw new InvalidParameterException("The end time must after start time: "+RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            }
        }

        try {
            DashboardHelper.assertNotNull("Delays", globalSetting.getDelays());
            DashboardHelper.assertNotNull("DelayUnit", globalSetting.getDelayUnit());
        } catch (Exception e) {
            throw new InvalidParameterException(e.getMessage() + RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
        }

        globalSetting.setId(UNIQUE_GLOBAL_SETTING_ID);
        repoGlobalSetting.save(globalSetting);

        return globalSetting;
    }

    public void setRepoGlobalSetting(RepoGlobalSetting repoGlobalSetting) {
        this.repoGlobalSetting = repoGlobalSetting;
    }
}
