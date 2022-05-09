package nz.govt.natlib.dashboard.ui.controller;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import nz.govt.natlib.dashboard.domain.service.GlobalSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class GlobalSettingController {
    private static final Logger log = LoggerFactory.getLogger(GlobalSettingController.class);
    private static final String MASKED_PASSWORD = "*";

    @Autowired
    private GlobalSettingService globalSettingService;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_GLOBAL_INITIAL, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand initialGlobalSetting(@RequestBody EntityGlobalSetting globalSetting, HttpServletRequest req, HttpServletResponse rsp) {
        PdsUserInfo pdsUserInfo = (PdsUserInfo) req.getSession().getAttribute(DashboardConstants.KEY_USER_INFO);

        return globalSettingService.initialGlobalSetting(pdsUserInfo);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_GLOBAL_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand saveGlobalSetting(@RequestBody EntityGlobalSetting globalSetting, HttpServletRequest req, HttpServletResponse rsp) {
        PdsUserInfo pdsUserInfo = (PdsUserInfo) req.getSession().getAttribute(DashboardConstants.KEY_USER_INFO);

        return globalSettingService.saveGlobalSettingWithoutWhiteList(globalSetting, pdsUserInfo);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_GLOBAL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getGlobalSetting() {
        return globalSettingService.getGlobalSetting();
    }


    @RequestMapping(path = DashboardConstants.PATH_SETTING_GLOBAL_WHITE_USER_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand saveWhiteListUser(@RequestBody EntityWhitelistSetting userInfo, HttpServletRequest req, HttpServletResponse rsp) {
        PdsUserInfo pdsUserInfo = (PdsUserInfo) req.getSession().getAttribute(DashboardConstants.KEY_USER_INFO);

        return globalSettingService.saveUser2WhiteList(userInfo, pdsUserInfo);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_GLOBAL_WHITE_USER_DELETE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand deleteWhiteListUser(@RequestBody EntityWhitelistSetting userInfo, HttpServletRequest req, HttpServletResponse rsp) {
        PdsUserInfo pdsUserInfo = (PdsUserInfo) req.getSession().getAttribute(DashboardConstants.KEY_USER_INFO);

        return globalSettingService.deleteUserFromWhiteList(userInfo, pdsUserInfo);
    }
}
