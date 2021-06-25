package nz.govt.natlib.dashboard.domain.service;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
import nz.govt.natlib.dashboard.common.core.RosettaWebService;
import nz.govt.natlib.dashboard.common.exception.WebServiceException;
import nz.govt.natlib.dashboard.common.metadata.EnumUserRole;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import nz.govt.natlib.dashboard.domain.entity.EntityWhiteList;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoGlobalSetting;
import nz.govt.natlib.dashboard.domain.repo.RepoWhiteList;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GlobalSettingService {
    private static final Logger log = LoggerFactory.getLogger(GlobalSettingService.class);
    private static final String MASKED_PASSWORD = "******";
    private static final Long DEFAULT_GLOBAL_ROW_ID=1L;

    @Autowired
    private RepoGlobalSetting repoGlobalSetting;
    @Autowired
    private RepoWhiteList repoWhiteList;
    @Autowired
    private RepoFlowSetting repoFlowSetting;
    @Autowired
    private RosettaWebService rosettaWebService;

    public EntityGlobalSetting getGlobalSettingInstance() {
        EntityGlobalSetting globalSetting = repoGlobalSetting.getById(DEFAULT_GLOBAL_ROW_ID);
        if (DashboardHelper.isNull(globalSetting)) {
            return new EntityGlobalSetting();
        }
        return globalSetting;
    }

    public String getDepositUserInstitute() {
        return getGlobalSettingInstance().getDepositUserInstitute();
    }

    public String getDepositUserName() {
        return getGlobalSettingInstance().getDepositUserName();
    }

    public String getDepositUserPassword() {
        return getGlobalSettingInstance().getDepositUserPassword();
    }

    public boolean isInitialed() {
        return repoWhiteList.getAll().size() > 0;
    }

    public boolean isInWhiteList(PdsUserInfo pdsUserInfo) {
        EntityWhiteList user = getUserFromWhiteList(pdsUserInfo);
        return !DashboardHelper.isNull(user);
    }

    private EntityWhiteList getUserFromWhiteList(PdsUserInfo pdsUserInfo) {
        if (DashboardHelper.isNull(pdsUserInfo)) {
            return null;
        }
        return getUserFromWhiteList(pdsUserInfo.getUserName());
    }

    private EntityWhiteList getUserFromWhiteList(String userName) {
        return repoWhiteList.getByUserName(userName);
    }

    public RestResponseCommand initialGlobalSetting(PdsUserInfo pdsUserInfo) {
        RestResponseCommand rstVal = new RestResponseCommand();

        EntityWhiteList userInfo = getUserFromWhiteList(pdsUserInfo);
        if (!DashboardHelper.isNull(userInfo)) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg("The dashboard could not be duplicated initialed.");
            return rstVal;
        }

        userInfo = new EntityWhiteList();
        userInfo.setUserName(pdsUserInfo.getUserName());
        userInfo.setRole(EnumUserRole.admin);

        repoWhiteList.save(userInfo);

        rstVal.setRspBody(userInfo);

        return rstVal;
    }

    public RestResponseCommand saveGlobalSettingWithoutWhiteList(EntityGlobalSetting globalSetting, PdsUserInfo pdsUserInfo) {
        RestResponseCommand rstVal = new RestResponseCommand();

        EntityWhiteList whiteUserInfo = getUserFromWhiteList(pdsUserInfo);
        if (DashboardHelper.isNull(whiteUserInfo)) {
            rstVal.setRspCode(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            return rstVal;
        }

        //Validate privilege
        if (whiteUserInfo.getRole() != EnumUserRole.admin) {
            rstVal.setRspCode(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            return rstVal;
        }

        EntityGlobalSetting oldGlobalSetting = repoGlobalSetting.getById(DEFAULT_GLOBAL_ROW_ID);
        if (oldGlobalSetting != null && StringUtils.equals(globalSetting.getDepositUserPassword(), MASKED_PASSWORD)) {
            globalSetting.setDepositUserPassword(oldGlobalSetting.getDepositUserPassword());
        }

        try {
            String handle = rosettaWebService.login(globalSetting.getDepositUserInstitute(), globalSetting.getDepositUserName(), globalSetting.getDepositUserPassword());
            if (StringUtils.isEmpty(handle)) {
                rstVal.setRspCode(RestResponseCommand.RSP_USER_NAME_PASSWORD_ERROR);
                rstVal.setRspMsg("Deposit user info is not valid.");
                return rstVal;
            }
        } catch (Exception e) {
            log.error("Failed to access to rosetta service.", e);
            rstVal.setRspCode(RestResponseCommand.RSP_USER_NAME_PASSWORD_ERROR);
            rstVal.setRspMsg(e.getMessage());
            return rstVal;
        }

        globalSetting.setAuditRst(true);
        globalSetting.setAuditMsg("OK");
        repoGlobalSetting.save(globalSetting);

        rstVal.setRspBody(globalSetting);

        return rstVal;
    }

    public RestResponseCommand getGlobalSetting() {
        RestResponseCommand rstVal = new RestResponseCommand();
        EntityGlobalSetting globalSetting = repoGlobalSetting.getById(DEFAULT_GLOBAL_ROW_ID);
        if (DashboardHelper.isNull(globalSetting)) {
            globalSetting = new EntityGlobalSetting();
        }
        globalSetting.setDepositUserPassword(MASKED_PASSWORD);

        List<EntityWhiteList> whiteList = repoWhiteList.getAll();
        List<EntityFlowSetting> flowSettings = repoFlowSetting.getAll();

        Map<String, Object> rspBody = new HashMap<>();
        rspBody.put("globalSetting", globalSetting);
        rspBody.put("whiteList", whiteList);
        rspBody.put("flowSettings", flowSettings);

        rstVal.setRspBody(rspBody);

        rspBody.clear();
        whiteList.clear();

        return rstVal;
    }

    public RestResponseCommand saveUser2WhiteList(EntityWhiteList dtoUserInfo, PdsUserInfo pdsUserInfo) {
        RestResponseCommand rstVal = new RestResponseCommand();

        EntityWhiteList whiteUserInfoMe = getUserFromWhiteList(pdsUserInfo);
        if (DashboardHelper.isNull(whiteUserInfoMe) || whiteUserInfoMe.getRole() != EnumUserRole.admin) {
            rstVal.setRspCode(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            return rstVal;
        }

        if (DashboardHelper.isNull(dtoUserInfo) || DashboardHelper.isNull(dtoUserInfo.getUserName()) || DashboardHelper.isNull(dtoUserInfo.getRole())) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg("Input parameter can not be empty.");
            return rstVal;
        }

        if (StringUtils.equals(dtoUserInfo.getUserName(), pdsUserInfo.getUserName())) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg("You could not change your self.");
            return rstVal;
        }

        EntityGlobalSetting globalSetting = repoGlobalSetting.getById(DEFAULT_GLOBAL_ROW_ID);
        if (DashboardHelper.isNull(globalSetting)) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg("Global setting is not initialed");
            return rstVal;
        }

        EntityWhiteList whiteUserInfo = getUserFromWhiteList(dtoUserInfo.getUserName());
        if (DashboardHelper.isNull(whiteUserInfo)) {
            whiteUserInfo = new EntityWhiteList();
        }
        whiteUserInfo.setUserName(dtoUserInfo.getUserName());
        whiteUserInfo.setRole(dtoUserInfo.getRole());

        repoWhiteList.save(whiteUserInfo);

        rstVal.setRspBody(whiteUserInfo);

        return rstVal;
    }

    public RestResponseCommand deleteUserFromWhiteList(EntityWhiteList dtoUserInfo, PdsUserInfo pdsUserInfo) {
        RestResponseCommand rstVal = new RestResponseCommand();

        EntityWhiteList whiteUserInfoMe = getUserFromWhiteList(pdsUserInfo);
        if (DashboardHelper.isNull(whiteUserInfoMe) || whiteUserInfoMe.getRole() != EnumUserRole.admin) {
            rstVal.setRspCode(RestResponseCommand.RSP_AUTH_NO_PRIVILEGE);
            return rstVal;
        }

        if (DashboardHelper.isNull(dtoUserInfo) || DashboardHelper.isNull(dtoUserInfo.getId()) || DashboardHelper.isNull(dtoUserInfo.getUserName()) || DashboardHelper.isNull(dtoUserInfo.getRole())) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg("Input parameter can not be empty.");
            return rstVal;
        }

        if (StringUtils.equals(dtoUserInfo.getUserName(), pdsUserInfo.getUserName())) {
            rstVal.setRspCode(RestResponseCommand.RSP_INVALID_INPUT_PARAMETERS);
            rstVal.setRspMsg("You could not delete your self.");
            return rstVal;
        }

        repoWhiteList.deleteById(dtoUserInfo.getId());

        rstVal.setRspBody(dtoUserInfo);

        return rstVal;
    }

    public void validateGlobalSetting() {
        EntityGlobalSetting globalSetting = getGlobalSettingInstance();

        if (DashboardHelper.isNull(globalSetting) || DashboardHelper.isNull(globalSetting.getDepositUserInstitute()) || DashboardHelper.isNull(globalSetting.getDepositUserName()) || DashboardHelper.isNull(globalSetting.getDepositUserPassword())) {
            log.warn("Global setting is not initialed.");
            return;
        }

        try {
            DashboardHelper.assertNotNull("Institution", globalSetting.getDepositUserInstitute());
            DashboardHelper.assertNotNull("Username", globalSetting.getDepositUserName());
            DashboardHelper.assertNotNull("Password", globalSetting.getDepositUserPassword());
            //Verify username and password
            String pdsHandle = rosettaWebService.login(globalSetting.getDepositUserInstitute(), globalSetting.getDepositUserName(), globalSetting.getDepositUserPassword());
            if (DashboardHelper.isNull(pdsHandle)) {
                throw new WebServiceException("Could not access platform with given institution, username and password.");
            }
            globalSetting.setAuditRst(true);
            globalSetting.setAuditMsg("OK");
        } catch (Exception e) {
            globalSetting.setAuditRst(false);
            globalSetting.setAuditMsg(e.getMessage());
            log.error("Failed to validate global setting", e);
        }

        repoGlobalSetting.save(globalSetting);
    }
}
