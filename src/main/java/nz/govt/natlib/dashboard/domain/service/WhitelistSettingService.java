package nz.govt.natlib.dashboard.domain.service;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.auth.Sessions;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.metadata.EnumUserRole;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoWhiteList;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.List;

@Service
public class WhitelistSettingService {
    private static final Logger log = LoggerFactory.getLogger(WhitelistSettingService.class);
    @Autowired
    private RepoWhiteList repoWhiteList;

    @Autowired
    private Sessions sessions;

    public boolean isEmptyWhiteList() {
        List<EntityWhitelistSetting> data = repoWhiteList.getAll();
        return data.isEmpty();
    }

    public List<EntityWhitelistSetting> getAllWhitelistSettings() {
        List<EntityWhitelistSetting> data = repoWhiteList.getAll();
        return data;
    }

    public void saveWhitelistSetting(EntityWhitelistSetting whitelist, String token) throws Exception {
        if (whitelist.getId() != null) {
            String currentUsername=sessions.getUsername(token);
            EntityWhitelistSetting toBeModifiedUser = repoWhiteList.getById(whitelist.getId());
            if (toBeModifiedUser != null && toBeModifiedUser.getWhiteUserName().equalsIgnoreCase(currentUsername)) {
                throw new InvalidParameterException("You could not change yourself: " + RestResponseCommand.RSP_WHITELIST_CHANGE_ERROR);
            }
        }
        saveWhitelistSetting(whitelist);
    }

    public void saveWhitelistSetting(EntityWhitelistSetting whitelist) throws Exception {
        //Validate the producer
        DashboardHelper.assertNotNull("Whitelist", whitelist);
        DashboardHelper.assertNotNull("WhitelistUsername", whitelist.getWhiteUserName());
        DashboardHelper.assertNotNull("WhitelistRole", whitelist.getWhiteUserRole());

        EntityWhitelistSetting existingUser = repoWhiteList.getByUserName(whitelist.getWhiteUserName());
        if (existingUser != null && !existingUser.getId().equals(whitelist.getId())) {
            throw new InvalidParameterException("The user exists in the white list: " + RestResponseCommand.RSP_PROCESS_SET_DUPLICATED);
        }

        repoWhiteList.save(whitelist);
    }

    public void deleteWhitelistSetting(Long id, String token) throws Exception {
        String currentUsername=sessions.getUsername(token);
        EntityWhitelistSetting toBeDeletedUser = repoWhiteList.getById(id);
        if (toBeDeletedUser != null && toBeDeletedUser.getWhiteUserName().equalsIgnoreCase(currentUsername)) {
            throw new InvalidParameterException("You could not delete yourself: " + RestResponseCommand.RSP_WHITELIST_CHANGE_ERROR);
        }
        repoWhiteList.deleteById(id);
    }


    public EntityWhitelistSetting getWhitelistDetail(Long id) {
        return repoWhiteList.getById(id);
    }

    public boolean isInWhiteList(PdsUserInfo pdsUserInfo) {
        EntityWhitelistSetting user = getUserFromWhiteList(pdsUserInfo);
        return !DashboardHelper.isNull(user);
    }

    public EntityWhitelistSetting getUserFromWhiteList(PdsUserInfo pdsUserInfo) {
        if (DashboardHelper.isNull(pdsUserInfo)) {
            return null;
        }
        return getUserFromWhiteList(pdsUserInfo.getUserName());
    }

    public EntityWhitelistSetting getUserFromWhiteList(String userName) {
        return repoWhiteList.getByUserName(userName);
    }

    public EntityWhitelistSetting initialWhiteListSetting(PdsUserInfo pdsUserInfo) {
        EntityWhitelistSetting userInfo = getUserFromWhiteList(pdsUserInfo);
        if (!DashboardHelper.isNull(userInfo)) {
            throw new InvalidParameterException("The dashboard could not be duplicated initialed:" + RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
        }

        userInfo = new EntityWhitelistSetting();
        userInfo.setWhiteUserName(pdsUserInfo.getUserName());
        userInfo.setWhiteUserRole(EnumUserRole.admin.name());

        repoWhiteList.save(userInfo);
        return userInfo;
    }
}
