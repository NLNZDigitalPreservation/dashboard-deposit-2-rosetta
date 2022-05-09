package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.service.WhitelistSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class WhitelistController {
    @Autowired
    private WhitelistSettingService whitelistSettingService;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_ALL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getAllWhitelistSettings() {
        return whitelistSettingService.getAllWhitelistSettings();
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_DETAIL, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getWhitelistSettingDetail(@RequestParam("id") Long id) {
        return whitelistSettingService.getWhitelistDetail(id);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand saveWhitelistSetting(@RequestBody EntityWhitelistSetting reqCmd) {
        RestResponseCommand retVal = new RestResponseCommand();
        try {
            retVal = whitelistSettingService.saveWhitelistSetting(reqCmd);
        } catch (Exception e) {
            retVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            retVal.setRspMsg(e.getMessage());
        }
        return retVal;
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_DELETE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand deleteWhitelistSetting(@RequestParam("id") Long id) {
        return whitelistSettingService.deleteWhitelistSetting(id);
    }
}
