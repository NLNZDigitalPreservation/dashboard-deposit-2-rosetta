package nz.govt.natlib.dashboard.domain.repo;

import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityWhiteList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("RepoWhiteList")
public class RepoWhiteList extends RepoAbstract {
    private static final String SUB_FOLDER = "setting-white-list";

    @PostConstruct
    public void init() {
        this.subStoragePath = this.systemStoragePath + File.separator + SUB_FOLDER;
        this.entityKey = EnumEntityKey.WhiteList;
    }

    public EntityWhiteList getById(Long id) {
        return (EntityWhiteList) getById(id, EntityWhiteList.class);
    }

    public EntityWhiteList getByUserName(String userName) {
        List<EntityWhiteList> allUsers = getAll();
        for (EntityWhiteList user : allUsers) {
            if (user.getUserName().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    public List<EntityWhiteList> getAll() {
        List<EntityWhiteList> retVal = new ArrayList<>();

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

            EntityWhiteList obj = (EntityWhiteList) json2Object(json, EntityWhiteList.class);
            retVal.add(obj);
        }

        return retVal;
    }
}
