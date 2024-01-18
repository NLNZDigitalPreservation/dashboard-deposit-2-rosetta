package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoGlobalSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class TimerScheduledExecutors {
    private static final Logger log = LoggerFactory.getLogger(TimerScheduledExecutors.class);

    @Autowired
    protected RepoGlobalSetting repoGlobalSetting;
    @Autowired
    protected ScheduleProcessorBasic scheduleProcessor;

    private boolean isRunning = true;

    @PostConstruct
    public void init() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5); //Postpone 3 seconds to wait for the preparation of Rosetta service.
        scheduleProcessor();
    }

    private void fixedDelay() throws InterruptedException {
        long delays;
        TimeUnit delayUnit;

        EntityGlobalSetting globalSetting = repoGlobalSetting.getGlobalSetting();
        if (globalSetting == null) {
            delays = 60;
            delayUnit = TimeUnit.SECONDS;
            log.warn("The global setting is null.");
        } else {
            delays = globalSetting.getDelays();
            delayUnit = globalSetting.getDelayTimeUnit();
        }

        delayUnit.sleep(delays);
    }

    public void scheduleProcessor() {
        Runnable scan = () -> {
            while (true) {
                try {
                    if (isRunning) {
                        scheduleProcessor.scan();
                    }
                    fixedDelay();
                } catch (Throwable e) {
                    log.error("Failed to execute scan", e);
                    log.error("Failed to execute scan: {}", e.getMessage());
                }
            }
        };
        Thread scanProcessor = new Thread(scan);
        scanProcessor.setName("scan timer");
        scanProcessor.start();

        Runnable pipeline = () -> {
            while (true) {
                try {
                    if (isRunning) {
                        scheduleProcessor.pipeline();
                    }
                    fixedDelay();
                } catch (Throwable e) {
                    log.error("Failed to execute pipeline", e);
                    log.error("Failed to execute pipeline: {}", e.getMessage());
                }
            }
        };
        Thread pipelineProcessor = new Thread(pipeline);
        pipelineProcessor.setName("pipeline timer");
        pipelineProcessor.start();
    }

    public void startTimer() {
        isRunning = true;
    }

    public void stopTimer() {
        isRunning = false;
    }
}
