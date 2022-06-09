package nz.govt.natlib.dashboard.domain.daemon;

import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.ndha.common.exlibris.ResultOfDeposit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleProcessorImplJobDepositing extends ScheduleProcessor {
    @Override
    public void handle(EntityFlowSetting flowSetting) throws Exception {
        List<EntityDepositJob> listOfJobs = repoDepositJob.getByFlowId(flowSetting.getId());
        long countRunning = listOfJobs.stream().filter(job -> job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.RUNNING).count();

        //Get the current max concurrency limitation of running jobs according to the schedule.
        LocalDateTime now = LocalDateTime.now();
        int nowDay = now.getDayOfWeek().ordinal();
        int maxConcurrencyJobs = flowSetting.getWeeklyMaxConcurrency()[nowDay];
        log.debug("Now: {}, day: {}, maxConcurrencyJobs: {}, countRunning: {}", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), nowDay, maxConcurrencyJobs, countRunning);

        for (EntityDepositJob job : listOfJobs) {
            //There is one job is running, ignoring to start new job
            if (countRunning >= maxConcurrencyJobs) {
                log.info("Material Flow: {}, The number of running jobs exceeds the max limitation, please have a wait. Current Running: {}", flowSetting.getId(), countRunning);
                break;
            }

            //
            if (!(job.getStage() == EnumDepositJobStage.DEPOSIT && job.getState() == EnumDepositJobState.INITIALED)) {
                log.debug("Skip deposit job for: {} --> {} at status [{}] [{}]", flowSetting.getId(), job.getInjectionTitle(), job.getStage(), job.getState());
                continue;
            }

            EntityDepositAccountSetting depositAccount = repoDepositAccount.getById(flowSetting.getDepositAccountId());
            String pdsHandle;
            try {
                pdsHandle = rosettaWebService.login(depositAccount.getDepositUserInstitute(), depositAccount.getDepositUserName(), depositAccount.getDepositUserPassword());
            } catch (Exception e) {
                log.error("Failed to submit job", e);
                break;
            }

            ResultOfDeposit resultOfDeposit;
            try {
                resultOfDeposit = this.rosettaWebService.deposit(job.getInjectionTitle(), pdsHandle, depositAccount.getDepositUserInstitute(), flowSetting.getProducerId(), flowSetting.getMaterialFlowId(), job.getDepositSetId());
            } catch (Exception e) {
                log.error("Failed to submit job", e);
                break;
            }

            if (resultOfDeposit.isSuccess()) {
                depositJobService.jobDepositAccept(job, resultOfDeposit.getSipID(), flowSetting);
                log.info("Job [{}] is submitted to Rosetta successfully, SIPId=[{}], Status=[{}]",
                        job.getInjectionTitle(), resultOfDeposit.getSipID(), resultOfDeposit.getResultMessage());
                countRunning++; //To be sure Rosetta not over load.
            } else {
                depositJobService.jobDepositReject(job, resultOfDeposit.getResultMessage());
                log.warn("Job [{}] is submitted to Rosetta failed, msg: [{}]", job.getInjectionTitle(), resultOfDeposit.getResultMessage());
            }
        }
    }
}