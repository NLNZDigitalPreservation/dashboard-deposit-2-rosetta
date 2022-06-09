package nz.govt.natlib.dashboard.domain.repo;

import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositAccountSetting;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component("RepoProducer")
public class RepoDepositAccount extends RepoAbstract {
    public static final String SUB_FOLDER = "setting-deposit-account";

    @PostConstruct
    public void init() {
        this.subStoragePath = this.systemStoragePath + File.separator + SUB_FOLDER;
        this.entityKey = EnumEntityKey.DepositAccountSetting;
    }

    public EntityDepositAccountSetting getById(Long id) {
        return (EntityDepositAccountSetting) getById(id, EntityDepositAccountSetting.class);
    }

    public List<EntityDepositAccountSetting> getAll() {
        List<EntityDepositAccountSetting> retVal = new ArrayList<>();

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

            EntityDepositAccountSetting obj = (EntityDepositAccountSetting) json2Object(json, EntityDepositAccountSetting.class);
            retVal.add(obj);
        }

        return retVal;
    }
}
