package nz.govt.natlib.dashboard.domain.repo;

import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component("RepoProducer")
public class RepoProducer extends RepoAbstract {
    public static final String SUB_FOLDER = "setting-producer";

    @PostConstruct
    public void init() {
        this.subStoragePath = this.systemStoragePath + File.separator + SUB_FOLDER;
        this.entityKey = EnumEntityKey.ProducerSetting;
    }

    public EntityProducer getById(Long id) {
        return (EntityProducer) getById(id, EntityProducer.class);
    }

    public List<EntityProducer> getAll() {
        List<EntityProducer> retVal = new ArrayList<>();

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

            EntityProducer obj = (EntityProducer) json2Object(json, EntityProducer.class);
            retVal.add(obj);
        }

        return retVal;
    }
}
