package nz.govt.natlib.dashboard.domain.repo;


import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityFlowSetting;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component("RepoFlowSetting")
public class RepoFlowSetting extends RepoAbstract {
    private static final String SUB_FOLDER = "setting-flow";

    @PostConstruct
    public void init() {
        this.subStoragePath = this.systemStoragePath + File.separator + SUB_FOLDER;
        this.entityKey = EnumEntityKey.FlowSetting;
    }

    public EntityFlowSetting getById(Long id) {
        return (EntityFlowSetting) getById(id, EntityFlowSetting.class);
    }

    public List<EntityFlowSetting> getAll() {
        List<EntityFlowSetting> retVal = new ArrayList<>();

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

            EntityFlowSetting obj = (EntityFlowSetting) json2Object(json, EntityFlowSetting.class);
            retVal.add(obj);
        }

        return retVal;
    }
}
