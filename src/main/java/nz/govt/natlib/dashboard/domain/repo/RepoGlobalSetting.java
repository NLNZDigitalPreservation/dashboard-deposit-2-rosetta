package nz.govt.natlib.dashboard.domain.repo;

import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component("RepoGlobalSetting")
public class RepoGlobalSetting extends RepoAbstract {
    private static final String SUB_FOLDER = "global-settings";

    @PostConstruct
    public void init() {
        this.subStoragePath = this.systemStoragePath + File.separator + SUB_FOLDER;
        this.entityKey = EnumEntityKey.GlobalSetting;
    }

    public EntityGlobalSetting getGlobalSetting() {
        List<EntityGlobalSetting> all = getAll();
        if (all.size() > 0) {
            return all.get(0);
        }
        return null;
    }


    public List<EntityGlobalSetting> getAll() {
        List<EntityGlobalSetting> retVal = new ArrayList<>();

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

            EntityGlobalSetting obj = (EntityGlobalSetting) json2Object(json, EntityGlobalSetting.class);
            retVal.add(obj);
        }

        return retVal;
    }
}
