package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.exception.InvalidParameterException;
import nz.govt.natlib.dashboard.common.exception.NullParameterException;
import nz.govt.natlib.dashboard.common.exception.WebServiceException;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.service.FlowSettingService;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class FlowSettingController {
    @Autowired
    private FlowSettingService flowSettingService;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_FLOW_ALL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getAllFlowSettings() {
        return flowSettingService.getAllFlowSettings();
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_FLOW_DETAIL, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getFlowSettingDetail(@RequestParam("id") Long id) {
        return flowSettingService.getFlowSettingDetail(id);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_FLOW_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand saveFlowSetting(@RequestBody EntityFlowSetting reqCmd) {
        RestResponseCommand retVal = new RestResponseCommand();

        try {
            EntityFlowSetting rspCmd = flowSettingService.saveFlowSetting(reqCmd);
            retVal.setRspBody(rspCmd);
        } catch (NullParameterException | WebServiceException | InvalidParameterException e) {
            retVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            retVal.setRspMsg(e.getMessage());
        }

        return retVal;
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_FLOW_DELETE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand deleteFlowSetting(@RequestParam("id") Long id) {
        return flowSettingService.deleteFlowSetting(id);
    }
}
