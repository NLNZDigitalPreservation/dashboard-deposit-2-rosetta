package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoGlobalSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class TimerScheduledExecutors {
    private static final Logger log = LoggerFactory.getLogger(TimerScheduledExecutors.class);

    private final ScheduledThreadPoolExecutor _schedule_executor = new ScheduledThreadPoolExecutor(256);
    @Autowired
    protected RepoGlobalSetting repoGlobalSetting;
    @Autowired
    protected ScheduleProcessorBasic scheduleProcessor;

    private ScheduledFuture<?> futureScheduleProcessor;

    @PostConstruct
    public void init() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5); //Postpone 3 seconds to wait for the preparation of Rosetta service.
        scheduleProcessor();
    }

    public void scheduleProcessor() {
        long delays = 60;
        TimeUnit delayUnit = TimeUnit.SECONDS;

        EntityGlobalSetting globalSetting = repoGlobalSetting.getGlobalSetting();
        if (globalSetting == null) {
            log.warn("The global setting is null.");
        } else {
            delays = globalSetting.getDelays();
            delayUnit = globalSetting.getDelayTimeUnit();
        }
        Runnable handler = () -> {
            try {
                scheduleProcessor.handle();
            } catch (Throwable e) {
                log.error("Failed to execute processor", e);
            }
        };
        this.futureScheduleProcessor = _schedule_executor.scheduleWithFixedDelay(handler, delays, delays, delayUnit);
    }

    public void close() {
        if (this.futureScheduleProcessor != null) {
            this.futureScheduleProcessor.cancel(true);
        }
        _schedule_executor.shutdownNow();
    }

    public void rescheduleProcessor() {
        if (this.futureScheduleProcessor != null) {
            this.futureScheduleProcessor.cancel(true);
        }
        this.scheduleProcessor();
    }
}
