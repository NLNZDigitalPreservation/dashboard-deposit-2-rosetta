package nz.govt.natlib.dashboard.domain.repo;

import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component("RepoWhiteList")
public class RepoWhiteList extends RepoAbstract {
    private static final String SUB_FOLDER = "setting-white-list";

    @PostConstruct
    public void init() {
        this.subStoragePath = this.systemStoragePath + File.separator + SUB_FOLDER;
        this.entityKey = EnumEntityKey.WhiteListSetting;
    }

    public EntityWhitelistSetting getById(Long id) {
        return (EntityWhitelistSetting) getById(id, EntityWhitelistSetting.class);
    }

    public EntityWhitelistSetting getByUserName(String userName) {
        List<EntityWhitelistSetting> allUsers = getAll();
        for (EntityWhitelistSetting user : allUsers) {
            if (user.getWhiteUserName().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    public List<EntityWhitelistSetting> getAll() {
        List<EntityWhitelistSetting> retVal = new ArrayList<>();

        File rootDir = new File(this.subStoragePath);
        if (!rootDir.exists()) {
            return retVal;
        }
        File[] files = rootDir.listFiles();
        if (files == null) {
            return retVal;
        }

        for (File f : files) {
            String json = read(f);
            if (StringUtils.isEmpty(json)) {
                continue;
            }

            EntityWhitelistSetting obj = (EntityWhitelistSetting) json2Object(json, EntityWhitelistSetting.class);
            retVal.add(obj);
        }

        return retVal;
    }
}
