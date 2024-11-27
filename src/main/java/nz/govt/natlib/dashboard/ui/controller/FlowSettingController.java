package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.exception.InvalidParameterException;
import nz.govt.natlib.dashboard.common.exception.NullParameterException;
import nz.govt.natlib.dashboard.common.exception.WebServiceException;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.domain.service.FlowSettingService;
import nz.govt.natlib.dashboard.common.DashboardConstants;
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
    public ResponseEntity<?> saveFlowSetting(@RequestBody EntityFlowSetting reqCmd) {
        try {
            EntityFlowSetting ret = flowSettingService.saveFlowSetting(reqCmd);
            return ResponseEntity.ok().body(ret);
        } catch (NullParameterException | WebServiceException | InvalidParameterException e) {
            return ResponseEntity.badRequest().body(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS + ": " + e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_FLOW_DELETE, method = {RequestMethod.DELETE})
    public ResponseEntity<?> deleteFlowSetting(@RequestParam("id") Long id) {
        try {
            EntityFlowSetting ret = flowSettingService.deleteFlowSetting(id);
            return ResponseEntity.ok().body(ret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
