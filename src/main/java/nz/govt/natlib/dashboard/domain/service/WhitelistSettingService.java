package nz.govt.natlib.dashboard.domain.service;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.metadata.EnumUserRole;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoWhiteList;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WhitelistSettingService {
    private static final Logger log = LoggerFactory.getLogger(WhitelistSettingService.class);
    @Autowired
    private RepoWhiteList repoWhiteList;

    public boolean isEmptyWhiteList() {
        List<EntityWhitelistSetting> data = repoWhiteList.getAll();
        return data.isEmpty();
    }

    public RestResponseCommand getAllWhitelistSettings() {
        RestResponseCommand rstVal = new RestResponseCommand();
        List<EntityWhitelistSetting> data = repoWhiteList.getAll();
        rstVal.setRspBody(data);
        return rstVal;
    }

    public RestResponseCommand saveWhitelistSetting(EntityWhitelistSetting whitelist, PdsUserInfo loginUserInfo) throws Exception {
        RestResponseCommand rstVal = new RestResponseCommand();
        if (whitelist.getId() != null) {
            EntityWhitelistSetting toBeModifiedUser = repoWhiteList.getById(whitelist.getId());
            if (toBeModifiedUser != null && toBeModifiedUser.getWhiteUserName().equalsIgnoreCase(loginUserInfo.getUserName())) {
                rstVal.setRspCode(RestResponseCommand.RSP_WHITELIST_CHANGE_ERROR);
                rstVal.setRspMsg("You could not change yourself.");
                return rstVal;
            }
        }
        return saveWhitelistSetting(whitelist);
    }

    public RestResponseCommand saveWhitelistSetting(EntityWhitelistSetting whitelist) throws Exception {
        //Validate the producer
        DashboardHelper.assertNotNull("Whitelist", whitelist);
        DashboardHelper.assertNotNull("WhitelistUsername", whitelist.getWhiteUserName());
        DashboardHelper.assertNotNull("WhitelistRole", whitelist.getWhiteUserRole());

        RestResponseCommand rstVal = new RestResponseCommand();

        EntityWhitelistSetting existingUser = repoWhiteList.getByUserName(whitelist.getWhiteUserName());
        if (existingUser != null && !existingUser.getId().equals(whitelist.getId())) {
            rstVal.setRspCode(RestResponseCommand.RSP_PROCESS_SET_DUPLICATED);
            rstVal.setRspMsg("The User exists in the white list.");
            return rstVal;
        }

        repoWhiteList.save(whitelist);
        return rstVal;
    }

    public RestResponseCommand deleteWhitelistSetting(Long id, PdsUserInfo loginUserInfo) {
        RestResponseCommand rstVal = new RestResponseCommand();
        EntityWhitelistSetting toBeDeletedUser = repoWhiteList.getById(id);
        if (toBeDeletedUser != null && toBeDeletedUser.getWhiteUserName().equalsIgnoreCase(loginUserInfo.getUserName())) {
            rstVal.setRspCode(RestResponseCommand.RSP_WHITELIST_CHANGE_ERROR);
            rstVal.setRspMsg("You could not delete yourself.");
            return rstVal;
        }
        repoWhiteList.deleteById(id);
        return rstVal;
    }

    public RestResponseCommand getWhitelistDetail(Long id) {
        RestResponseCommand rstVal = new RestResponseCommand();
        rstVal.setRspBody(repoWhiteList.getById(id));
        return rstVal;
    }

    public boolean isInWhiteList(PdsUserInfo pdsUserInfo) {
        EntityWhitelistSetting user = getUserFromWhiteList(pdsUserInfo);
        return !DashboardHelper.isNull(user);
    }

    private EntityWhitelistSetting getUserFromWhiteList(PdsUserInfo pdsUserInfo) {
        if (DashboardHelper.isNull(pdsUserInfo)) {
            return null;
        }
        return getUserFromWhiteList(pdsUserInfo.getUserName());
    }

    private EntityWhitelistSetting getUserFromWhiteList(String userName) {
        return repoWhiteList.getByUserName(userName);
    }

    public RestResponseCommand initialWhiteListSetting(PdsUserInfo pdsUserInfo) {
        RestResponseCommand rstVal = new RestResponseCommand();

        EntityWhitelistSetting userInfo = getUserFromWhiteList(pdsUserInfo);
        if (!DashboardHelper.isNull(userInfo)) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg("The dashboard could not be duplicated initialed.");
            return rstVal;
        }

        userInfo = new EntityWhitelistSetting();
        userInfo.setWhiteUserName(pdsUserInfo.getUserName());
        userInfo.setWhiteUserRole(EnumUserRole.admin.name());

        repoWhiteList.save(userInfo);

        rstVal.setRspBody(userInfo);

        return rstVal;
    }
}
