package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.exception.InvalidParameterException;
import nz.govt.natlib.dashboard.common.exception.NullParameterException;
import nz.govt.natlib.dashboard.common.exception.WebServiceException;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.domain.service.FlowSettingService;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.exceptions.NotFoundException;
import nz.govt.natlib.dashboard.exceptions.SystemErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FlowSettingController {
    @Autowired
    private FlowSettingService flowSettingService;

    @Autowired
    private RepoFlowSetting repoFlowSetting;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_FLOW_ALL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> getAllFlowSettings() {
        List<EntityFlowSetting> ret = repoFlowSetting.getAll();
        return ResponseEntity.ok().body(ret);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_FLOW_DETAIL, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> getFlowSettingDetail(@RequestParam("id") Long id) {
        EntityFlowSetting ret = repoFlowSetting.getById(id);
        if (ret == null) {
            return ResponseEntity.badRequest().body("Not able to find material flow: " + id);
        }
        return ResponseEntity.ok().body(ret);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_FLOW_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public EntityFlowSetting saveFlowSetting(@RequestBody EntityFlowSetting reqCmd) {
        try {
            return flowSettingService.saveFlowSetting(reqCmd);
        } catch (NullParameterException | WebServiceException | InvalidParameterException e) {
            throw new SystemErrorException(e.getMessage() + ": " + RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_FLOW_DELETE, method = {RequestMethod.POST, RequestMethod.GET})
    public EntityFlowSetting deleteFlowSetting(@RequestParam("id") Long id) {
        return flowSettingService.deleteFlowSetting(id);
    }
}
