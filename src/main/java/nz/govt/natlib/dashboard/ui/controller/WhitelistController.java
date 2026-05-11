package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.auth.HttpAccessManagementFilter;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.service.WhitelistSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class WhitelistController {
    @Autowired
    private WhitelistSettingService whitelistSettingService;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_ALL_GET, method = { RequestMethod.GET })
    public ResponseEntity<?> getAllWhitelistSettings() {
        try {
            List<EntityWhitelistSetting> ret = whitelistSettingService.getAllWhitelistSettings();
            return ResponseEntity.ok().body(ret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_DETAIL, method = { RequestMethod.GET })
    public ResponseEntity<?> getWhitelistSettingDetail(@RequestParam("id") Long id) {
        try {
            EntityWhitelistSetting ret = whitelistSettingService.getWhitelistDetail(id);
            return ResponseEntity.ok().body(ret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_SAVE, method = { RequestMethod.POST })
    public ResponseEntity<?> saveWhitelistSetting(@RequestBody EntityWhitelistSetting reqCmd,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            String currentUsername = (String) request.getAttribute(HttpAccessManagementFilter.ATTR_AUTH_USERNAME);
            whitelistSettingService.saveWhitelistSetting(reqCmd, currentUsername);
            return ResponseEntity.ok().body(true);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_DELETE, method = { RequestMethod.DELETE })
    public ResponseEntity<?> deleteWhitelistSetting(@RequestParam("id") Long id, HttpServletRequest request,
            HttpServletResponse response) {
        try {
            String currentUsername = (String) request.getAttribute(HttpAccessManagementFilter.ATTR_AUTH_USERNAME);
            whitelistSettingService.deleteWhitelistSetting(id, currentUsername);
            return ResponseEntity.ok().body(true);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
