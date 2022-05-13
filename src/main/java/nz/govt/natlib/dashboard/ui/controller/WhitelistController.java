package nz.govt.natlib.dashboard.ui.controller;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.auth.PrivilegeManagementHandler;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.service.WhitelistSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class WhitelistController {
    @Autowired
    private WhitelistSettingService whitelistSettingService;
    @Autowired
    private PrivilegeManagementHandler privilegeManagementHandler;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_ALL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getAllWhitelistSettings() {
        return whitelistSettingService.getAllWhitelistSettings();
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_DETAIL, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getWhitelistSettingDetail(@RequestParam("id") Long id) {
        return whitelistSettingService.getWhitelistDetail(id);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand saveWhitelistSetting(@RequestBody EntityWhitelistSetting reqCmd, HttpServletRequest request, HttpServletResponse response) {
        RestResponseCommand retVal = new RestResponseCommand();
        try {
            PdsUserInfo loginUserInfo = privilegeManagementHandler.getPdsUserInfo(request, response);
            retVal = whitelistSettingService.saveWhitelistSetting(reqCmd, loginUserInfo);
        } catch (Exception e) {
            retVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            retVal.setRspMsg(e.getMessage());
        }
        return retVal;
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_DELETE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand deleteWhitelistSetting(@RequestParam("id") Long id, HttpServletRequest request, HttpServletResponse response) {
        PdsUserInfo loginUserInfo = privilegeManagementHandler.getPdsUserInfo(request, response);
        return whitelistSettingService.deleteWhitelistSetting(id, loginUserInfo);
    }
}
