package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import nz.govt.natlib.dashboard.domain.service.GlobalSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class GlobalSettingController {
    @Autowired
    private GlobalSettingService globalSettingService;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_GLOBAL_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand saveGlobalSetting(@RequestBody EntityGlobalSetting reqCmd, HttpServletRequest request, HttpServletResponse response) {
        return globalSettingService.saveGlobalSetting(reqCmd);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_GLOBAL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getGlobalSetting(HttpServletRequest request, HttpServletResponse response) {
        return globalSettingService.getGlobalSetting();
    }
}
