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

    public RestResponseCommand saveWhitelistSetting(EntityWhitelistSetting whitelist) throws Exception {
        //Validate the producer
        DashboardHelper.assertNotNull("Whitelist", whitelist);
        DashboardHelper.assertNotNull("WhitelistUsername", whitelist.getWhiteUserName());
        DashboardHelper.assertNotNull("WhitelistRole", whitelist.getWhiteUserRole());

        RestResponseCommand rstVal = new RestResponseCommand();
        repoWhiteList.save(whitelist);
        return rstVal;
    }

    public RestResponseCommand deleteWhitelistSetting(Long id) {
        RestResponseCommand rstVal = new RestResponseCommand();
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
