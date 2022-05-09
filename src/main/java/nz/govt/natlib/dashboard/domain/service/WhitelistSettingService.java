package nz.govt.natlib.dashboard.domain.service;

import nz.govt.natlib.dashboard.common.core.RestResponseCommand;
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

    public RestResponseCommand getAllWhitelistSettings() {
        RestResponseCommand rstVal = new RestResponseCommand();
        List<EntityWhitelistSetting> data = repoWhiteList.getAll();
        rstVal.setRspBody(data);
        return rstVal;
    }

    public RestResponseCommand saveWhitelistSetting(EntityWhitelistSetting whitelist) throws Exception {
        //Validate the producer
        DashboardHelper.assertNotNull("Whitelist", whitelist);
        DashboardHelper.assertNotNull("WhitelistUsername", whitelist.getUserName());
        DashboardHelper.assertNotNull("WhitelistRole",whitelist.getRole());

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
}
