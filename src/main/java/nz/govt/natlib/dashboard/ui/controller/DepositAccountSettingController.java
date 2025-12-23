package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.service.DepositAccountSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DepositAccountSettingController {
    @Autowired
    private DepositAccountSettingService depositAccountSettingService;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_ALL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> getAllProducerSettings() {
        List<EntityDepositAccountSetting> ret = depositAccountSettingService.getAllDepositAccountSettings();
        return ResponseEntity.ok().body(ret);
    }

//    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_DETAIL, method = {RequestMethod.POST, RequestMethod.GET})
//    public ResponseEntity<?> getProducerSettingDetail(@RequestParam("id") Long id) throws Exception {
//        EntityDepositAccountSetting ret = depositAccountSettingService.getDepositAccountDetail(id);
//        return ResponseEntity.ok().body(ret);
//    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> saveProducerSetting(@RequestBody EntityDepositAccountSetting reqCmd) {
        try {
            depositAccountSettingService.saveDepositAccountSetting(reqCmd);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_DELETE, method = {RequestMethod.DELETE})
    public ResponseEntity<?> deleteProducerSetting(@RequestParam("id") Long id) {
        try {
            depositAccountSettingService.deleteDepositAccountSetting(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_REFRESH, method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> refreshSetting(@RequestParam("id") Long id) throws Exception {
        try {
            depositAccountSettingService.refreshDepositAccountSetting(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
