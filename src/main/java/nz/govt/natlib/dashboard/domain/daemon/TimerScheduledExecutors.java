package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.core.RosettaWebServiceImpl;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.*;
import nz.govt.natlib.dashboard.domain.service.DepositJobService;
import nz.govt.natlib.dashboard.domain.service.FlowSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class TimerScheduledExecutors {
    private static final Logger log = LoggerFactory.getLogger(TimerScheduledExecutors.class);

    private final ScheduledThreadPoolExecutor _schedule_executor = new ScheduledThreadPoolExecutor(256);
    private final Map<Long, ScheduledFuture<?>> mapProcessors = new HashMap<>();

    private static final long initialDelay = 60;
    @Autowired
    protected RosettaWebServiceImpl rosettaWebService;
    @Autowired
    protected DepositJobService depositJobService;
    @Autowired
    protected RepoFlowSetting repoFlowSetting;
    @Autowired
    protected RepoDepositAccount repoDepositAccount;
    @Autowired
    protected RepoDepositJob repoDepositJob;
    @Autowired
    protected FlowSettingService flowSettingService;

    @PostConstruct
    public void init() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5); //Postpone 3 seconds to wait for the preparation of Rosetta service.
        List<EntityFlowSetting> listFlows = repoFlowSetting.getAll();
        for (EntityFlowSetting flowSetting : listFlows) {
            if (!flowSetting.isEnabled()) {
                continue;
            }
            scheduleProcessor(flowSetting);
        }
    }

    public void scheduleProcessor(EntityFlowSetting flowSetting) {
        ScheduleProcessorImpl processor = new ScheduleProcessorImpl(flowSetting);
        processor.setDepositJobService(depositJobService);
        processor.setRepoDepositAccount(repoDepositAccount);
        processor.setRepoDepositJob(repoDepositJob);
        processor.setRepoFlowSetting(repoFlowSetting);
        processor.setRosettaWebService(rosettaWebService);
        Runnable handler = () -> {
            try {
                processor.handle();
            } catch (Throwable e) {
                log.error("Failed to execute processor", e);
            }
        };

        ScheduledFuture<?> future = _schedule_executor.scheduleWithFixedDelay(handler, initialDelay, flowSetting.getDelays(), flowSetting.getDelayTimeUnit());
        mapProcessors.put(flowSetting.getId(), future);
    }


    public void close() {
        mapProcessors.values().forEach(scheduledFuture -> scheduledFuture.cancel(true));
        _schedule_executor.shutdownNow();
    }

    public void rescheduleProcessor(EntityFlowSetting flowSetting) {
        if (flowSetting == null) {
            return;
        }

        ScheduledFuture<?> future = mapProcessors.get(flowSetting.getId());
        if (future != null) {
            future.cancel(true);
        }

        if (flowSetting.isEnabled()) {
            scheduleProcessor(flowSetting);
        }
    }

    public void closeProcessor(EntityFlowSetting flowSetting) {
        if (flowSetting == null) {
            return;
        }
        ScheduledFuture<?> future = mapProcessors.get(flowSetting.getId());
        if (future != null) {
            future.cancel(true);
        }
    }
}
