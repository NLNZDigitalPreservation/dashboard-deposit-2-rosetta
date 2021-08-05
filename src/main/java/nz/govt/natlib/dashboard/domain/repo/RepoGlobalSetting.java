package nz.govt.natlib.dashboard.domain.repo;


import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityGlobalSetting;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;



@Component("RepoGlobalSetting")
public class RepoGlobalSetting extends RepoAbstract {
    private static final String SUB_FOLDER = "setting-global";

    @PostConstruct
    public void init() {
        this.subStoragePath = this.systemStoragePath + File.separator + SUB_FOLDER;
        this.entityKey = EnumEntityKey.GlobalSetting;
    }

    public EntityGlobalSetting getById(Long id) {
        return (EntityGlobalSetting) getById(id, EntityGlobalSetting.class);
    }
}
