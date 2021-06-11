package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositJob;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositJobActive;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositJobHistory;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.domain.service.DepositJobService;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.ui.command.DepositJobManualAddCommand;
import nz.govt.natlib.dashboard.ui.command.DepositJobSearchCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DepositJobController {
    private static final Logger log = LoggerFactory.getLogger(DepositJobController.class);
    @Autowired
    private DepositJobService depositJobService;
    @Autowired
    private RepoFlowSetting repoFlowSetting;
    @Autowired
    private RepoDepositJobActive repoDepositJobActive;
    @Autowired
    private RepoDepositJobHistory repoDepositJobHistory;

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_ACTIVE_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand retrieveActiveDepositJobs() {
        log.info("Retrieve all active jobs.");
        RestResponseCommand rsp = new RestResponseCommand();
        List<EntityDepositJob> listActiveJobs = repoDepositJobActive.getAll();
        List<EntityFlowSetting> listFlowSettings = repoFlowSetting.getAll();
        depositJobService.appendFlowName(listActiveJobs);
//        Map<String, Object> mapPayload = new HashMap<>();
//        mapPayload.put("groups", listFlowSettings);
//        mapPayload.put("activeJobs", listActiveJobs);
//        rsp.setRspBody(mapPayload);
        rsp.setRspBody(listActiveJobs);
        return rsp;
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_ALL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand retrieveAllDepositJobs(@RequestParam("scope") String scope) {
        log.info("Retrieve all jobs from [{}]", scope);
        RestResponseCommand rsp = new RestResponseCommand();
        List<EntityDepositJob> listJobs;
        if (scope.equalsIgnoreCase(RepoDepositJob.DEPOSIT_JOB_DIR_ACTIVE)) {
            listJobs = repoDepositJobActive.getAll();
        } else {
            listJobs = repoDepositJobHistory.getAll();
        }
        depositJobService.appendFlowName(listJobs);
        rsp.setRspBody(listJobs);
        return rsp;
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_DETAIL, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand retrieveDepositJobDetails(@RequestParam("scope") String scope, @RequestParam("jobId") Long jobId) {
        log.info("Retrieve job details from [{}]: {}", scope, jobId);
        RestResponseCommand rsp = new RestResponseCommand();
        EntityDepositJob dto;
        if (scope.equalsIgnoreCase(RepoDepositJob.DEPOSIT_JOB_DIR_ACTIVE)) {
            dto = repoDepositJobActive.getById(jobId);
        } else {
            dto = repoDepositJobHistory.getById(jobId);
        }
        EntityFlowSetting flowSetting = repoFlowSetting.getById(dto.getFlowId());
        if (flowSetting != null) {
            dto.setFlowName(flowSetting.getName());
        }
        rsp.setRspBody(dto);
        return rsp;
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_UPDATE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand updateDepositJobs(@RequestParam("action") String action, @RequestBody List<EntityDepositJob> listJobs) {
        List<EntityDepositJob> retVal = new ArrayList<>();
        listJobs.forEach(job -> {
            log.info("{} {} {}", action, job.getId(), job.getInjectionTitle());
            EntityDepositJob updatedJob = null;
            if (action.equalsIgnoreCase("retry")) {
                updatedJob = depositJobService.retry(job);
            } else if (action.equalsIgnoreCase("pause")) {
                updatedJob = depositJobService.pause(job);
            } else if (action.equalsIgnoreCase("resume")) {
                updatedJob = depositJobService.resume(job);
            } else if (action.equalsIgnoreCase("cancel")) {
                updatedJob = depositJobService.cancel(job);
            } else if (action.equalsIgnoreCase("terminate")) {
                updatedJob = depositJobService.terminate(job);
            }
            retVal.add(updatedJob);
        });

        RestResponseCommand rsp = new RestResponseCommand();
        rsp.setRspBody(retVal);
        return rsp;
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_NEW, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand newDepositJobs(@RequestBody DepositJobManualAddCommand cmd) {
        log.info("Received new deposit job request: {} {} {} {}", cmd.getFlowId(), cmd.getFlowName(), cmd.getNfsDirectory(), cmd.isForcedReplaceExistingJob());
        return depositJobService.manuallySubmitDepositJob(cmd.getFlowId(), cmd.getNfsDirectory(), cmd.isForcedReplaceExistingJob());
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_SEARCH, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand searchDepositJobs(@RequestBody DepositJobSearchCommand cmd) {
        RestResponseCommand rsp = new RestResponseCommand();
        rsp.setRspBody(depositJobService.searchDepositJobs(cmd));
        return rsp;
    }
}
