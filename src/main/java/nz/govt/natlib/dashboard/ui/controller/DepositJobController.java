package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import nz.govt.natlib.dashboard.domain.repo.RepoDepositJob;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.domain.service.DepositJobService;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.ui.command.DepositJobManualAddCommand;
import nz.govt.natlib.dashboard.ui.command.DepositJobSearchCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class DepositJobController {
    private static final Logger log = LoggerFactory.getLogger(DepositJobController.class);
    @Autowired
    private DepositJobService depositJobService;
    @Autowired
    private RepoFlowSetting repoFlowSetting;
    @Autowired
    private RepoDepositJob repoDepositJob;

    @RequestMapping(path = DashboardConstants.PATH_JOBS_ACTIVE_LIST, method = {RequestMethod.GET})
    public ResponseEntity<?> listActiveJobs() {
        log.info("Retrieve job list");
        List<EntityDepositJob> jobs = repoDepositJob.getAll();
        return ResponseEntity.ok().body(jobs);
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_DETAIL, method = {RequestMethod.GET})
    public ResponseEntity<?> retrieveDepositJobDetails(@RequestParam("jobId") Long jobId) {
        log.info("Retrieve job details: {}", jobId);
        EntityDepositJob job = repoDepositJob.getById(jobId);
        return ResponseEntity.ok().body(job);
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_UPDATE, method = {RequestMethod.POST})
    public ResponseEntity<?> updateDepositJobs(@RequestParam("action") String action, @RequestBody List<EntityDepositJob> listJobs) {
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

        return ResponseEntity.ok().body(retVal);
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_NEW, method = {RequestMethod.POST})
    public ResponseEntity<?> newDepositJobs(@RequestBody DepositJobManualAddCommand cmd) {
        log.info("Received new deposit job request: {} {} {} {}", cmd.getFlowId(), cmd.getFlowName(), cmd.getNfsDirectory(), cmd.isForcedReplaceExistingJob());
        RestResponseCommand retVal = depositJobService.manuallySubmitDepositJob(cmd.getFlowId(), cmd.getNfsDirectory(), cmd.isForcedReplaceExistingJob());
        if (retVal.getRspCode() == RestResponseCommand.RSP_SUCCESS) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.internalServerError().body(retVal.getRspCode() + ": " + retVal.getRspMsg());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_SEARCH, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> searchDepositJobs(@RequestBody DepositJobSearchCommand cmd) {
        List<EntityDepositJob> jobs = depositJobService.searchDepositJobs(cmd);
        return ResponseEntity.ok().body(jobs);
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_EXPORT_DATA, method = {RequestMethod.POST})
    public ResponseEntity<?> exportDepositJobs(@RequestBody List<Long> cmd, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        depositJobService.exportData(cmd, req, rsp);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = DashboardConstants.PATH_DEPOSIT_JOBS_REDEPOSIT, method = {RequestMethod.POST})
    public ResponseEntity<?> redepositJob(@RequestParam String subFolder) throws IOException {
        RestResponseCommand retVal = depositJobService.redepositJob(subFolder);
        if (retVal.getRspCode() == RestResponseCommand.RSP_SUCCESS) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.internalServerError().body(retVal.getRspCode() + ": " + retVal.getRspMsg());
        }
    }
}
