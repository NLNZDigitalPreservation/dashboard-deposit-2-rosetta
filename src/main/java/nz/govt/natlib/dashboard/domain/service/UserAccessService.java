package nz.govt.natlib.dashboard.domain.service;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoWhiteList;
import nz.govt.natlib.dashboard.ui.command.UserAccessRspCommand;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAccessService {
    private static final Logger log = LoggerFactory.getLogger(UserAccessService.class);

    @Autowired
    private RosettaWebService rosettaWebService;

    @Autowired
    private RepoWhiteList repoWhiteList;

    @Value("${User.Institution}")
    private String userInstitution;

    public RestResponseCommand login(String username, String password) {
        RestResponseCommand restResponseCommand = new RestResponseCommand();

        if (DashboardHelper.isNull(username) || DashboardHelper.isNull(password)) {
            restResponseCommand.setRspCode(RestResponseCommand.RSP_USER_CONFIDENTIAL_REQUIRED);
            return restResponseCommand;
        }

        String pdsHandle;
        PdsUserInfo pdsUserInfo;
        try {
            pdsHandle = rosettaWebService.login(userInstitution, username, password);
            if (DashboardHelper.isNull(pdsHandle)) {
                restResponseCommand.setRspCode(RestResponseCommand.RSP_USER_NAME_PASSWORD_ERROR);
                return restResponseCommand;
            }
            pdsUserInfo = rosettaWebService.getPdsUserByPdsHandle(pdsHandle);
            if (DashboardHelper.isNull(pdsUserInfo)) {
                restResponseCommand.setRspCode(RestResponseCommand.RSP_USER_QUERY_ERROR);
                return restResponseCommand;
            }
        } catch (Exception e) {
            restResponseCommand.setRspCode(RestResponseCommand.RSP_USER_OTHER_ERROR);
            restResponseCommand.setRspMsg(e.getMessage());
            return restResponseCommand;
        }

        // Validate the white list
        EntityWhitelistSetting whitelistSetting = repoWhiteList.getByUserName(username);
        List<EntityWhitelistSetting> whiteListAll = repoWhiteList.getAll();
        if (whitelistSetting == null) {
            if (whiteListAll.size() > 0) {
                restResponseCommand.setRspCode(RestResponseCommand.RSP_USER_OTHER_ERROR);
                restResponseCommand.setRspMsg("Please contact the administrator to privilege the access.");
                whiteListAll.clear();
                return restResponseCommand;
            } else {
                restResponseCommand.setRspMsg("You are added to the user list by default");
            }
        }

        UserAccessRspCommand userAccessRspCommand = new UserAccessRspCommand();
        userAccessRspCommand.setPdsHandle(pdsHandle);
        userAccessRspCommand.setUserInfo(pdsUserInfo);
        userAccessRspCommand.setUsername(username);

        restResponseCommand.setRspBody(userAccessRspCommand);
        return restResponseCommand;
    }

    public RestResponseCommand logout(String pdsHandle) {
        RestResponseCommand restResponseCommand = new RestResponseCommand();
        if (DashboardHelper.isNull(pdsHandle)) {
            restResponseCommand.setRspCode(RestResponseCommand.RSP_USER_PDS_HANDLE_REQUIRED);
            return restResponseCommand;
        }

        try {
            rosettaWebService.logout(pdsHandle);
        } catch (Exception e) {
            restResponseCommand.setRspCode(RestResponseCommand.RSP_USER_OTHER_ERROR);
            restResponseCommand.setRspMsg(e.getMessage());
            return restResponseCommand;
        }

        return restResponseCommand;
    }
}
