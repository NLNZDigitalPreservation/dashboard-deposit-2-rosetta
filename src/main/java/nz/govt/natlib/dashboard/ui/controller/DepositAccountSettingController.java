package nz.govt.natlib.dashboard.ui.controller;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.service.DepositAccountSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class DepositAccountSettingController {
    @Autowired
    private DepositAccountSettingService depositAccountSettingService;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_ALL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getAllProducerSettings() {
        return depositAccountSettingService.getAllProducerSettings();
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_DETAIL, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getProducerSettingDetail(@RequestParam("id") Long id) {
        return depositAccountSettingService.getProducerDetail(id);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand saveProducerSetting(@RequestBody EntityDepositAccountSetting reqCmd) {
        RestResponseCommand retVal = new RestResponseCommand();
        try {
            retVal = depositAccountSettingService.saveProducerSetting(reqCmd);
        } catch (Exception e) {
            retVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            retVal.setRspMsg(e.getMessage());
        }
        return retVal;
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_DELETE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand deleteProducerSetting(@RequestParam("id") Long id) {
        return depositAccountSettingService.deleteProducerSetting(id);
    }
}
