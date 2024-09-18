package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import nz.govt.natlib.dashboard.domain.service.GlobalSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class GlobalSettingController {
    @Autowired
    private GlobalSettingService globalSettingService;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_GLOBAL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> getGlobalSetting(HttpServletRequest request, HttpServletResponse response) {
        try {
            return ResponseEntity.ok().body(globalSettingService.getGlobalSetting());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_GLOBAL_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> saveGlobalSetting(@RequestBody EntityGlobalSetting reqCmd, HttpServletRequest request, HttpServletResponse response) {
        try {
            return ResponseEntity.ok().body(globalSettingService.saveGlobalSetting(reqCmd));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
