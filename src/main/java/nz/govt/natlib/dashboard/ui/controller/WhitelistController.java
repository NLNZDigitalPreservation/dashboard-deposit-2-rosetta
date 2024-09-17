package nz.govt.natlib.dashboard.ui.controller;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.auth.PrivilegeManagementHandler;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.service.WhitelistSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class WhitelistController {
    @Autowired
    private WhitelistSettingService whitelistSettingService;
    @Autowired
    private PrivilegeManagementHandler privilegeManagementHandler;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_ALL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> getAllWhitelistSettings() {
        try{
            List<EntityWhitelistSetting> ret= whitelistSettingService.getAllWhitelistSettings();
            return ResponseEntity.ok().body(ret);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_DETAIL, method = {RequestMethod.POST, RequestMethod.GET})
    public  ResponseEntity<?> getWhitelistSettingDetail(@RequestParam("id") Long id) {
        try{
            EntityWhitelistSetting ret= whitelistSettingService.getWhitelistDetail(id);
            return ResponseEntity.ok().body(ret);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> saveWhitelistSetting(@RequestBody EntityWhitelistSetting reqCmd, HttpServletRequest request, HttpServletResponse response) {
        try {
            PdsUserInfo loginUserInfo = privilegeManagementHandler.getPdsUserInfo(request, response);
            whitelistSettingService.saveWhitelistSetting(reqCmd, loginUserInfo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_WHITELIST_DELETE, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> deleteWhitelistSetting(@RequestParam("id") Long id, HttpServletRequest request, HttpServletResponse response) {
        try {
            PdsUserInfo loginUserInfo = privilegeManagementHandler.getPdsUserInfo(request, response);
            whitelistSettingService.deleteWhitelistSetting(id, loginUserInfo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
