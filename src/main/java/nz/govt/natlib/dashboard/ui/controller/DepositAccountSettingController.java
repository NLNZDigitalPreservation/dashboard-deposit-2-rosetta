package nz.govt.natlib.dashboard.ui.controller;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import nz.govt.natlib.dashboard.domain.service.DepositAccountSettingService;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class DepositAccountSettingController {
    @Autowired
    private DepositAccountSettingService depositAccountSettingService;

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_ALL_GET, method = {RequestMethod.POST, RequestMethod.GET})
    public List<EntityDepositAccountSetting> getAllProducerSettings() {
        return depositAccountSettingService.getAllDepositAccountSettings();
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_DETAIL, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand getProducerSettingDetail(@RequestParam("id") Long id) throws Exception {
        return depositAccountSettingService.getDepositAccountDetail(id);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand saveProducerSetting(@RequestBody EntityDepositAccountSetting reqCmd) {
        RestResponseCommand retVal = new RestResponseCommand();
        try {
            retVal = depositAccountSettingService.saveDepositAccountSetting(reqCmd);
        } catch (Exception e) {
            retVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            retVal.setRspMsg(e.getMessage());
        }
        return retVal;
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_DELETE, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand deleteProducerSetting(@RequestParam("id") Long id) {
        return depositAccountSettingService.deleteDepositAccountSetting(id);
    }

    @RequestMapping(path = DashboardConstants.PATH_SETTING_DEPOSIT_ACCOUNT_REFRESH, method = {RequestMethod.POST, RequestMethod.GET})
    public RestResponseCommand refreshSetting(@RequestParam("id") Long id) throws Exception {
        return depositAccountSettingService.refreshDepositAccountSetting(id);
    }
}
