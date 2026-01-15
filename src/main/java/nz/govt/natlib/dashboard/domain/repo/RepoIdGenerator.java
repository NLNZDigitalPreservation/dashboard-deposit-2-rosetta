package nz.govt.natlib.dashboard.domain.repo;


import nz.govt.natlib.dashboard.common.metadata.EnumEntityKey;
import nz.govt.natlib.dashboard.domain.entity.EntityID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Component("RepoIdGenerator")
public class RepoIdGenerator extends RepoAbstract {
    private static final String SUB_FOLDER = "id-generator";

    @PostConstruct
    public void init() {
        this.subStoragePath = this.systemStoragePath + File.separator + SUB_FOLDER;
    }

    synchronized public Long nextId(EnumEntityKey key) {
        String fileName = String.format("%s.json", key.name());
        String json = read(this.subStoragePath, fileName);

        EntityID obj;

        if (StringUtils.isEmpty(json)){
            obj=new EntityID();
            obj.setKey(key.name());
            obj.setNumber(1L);
        }else{
            obj=(EntityID) json2Object(json,EntityID.class);
            obj.setNumber(obj.getNumber() + 1);
        }

        save(this.subStoragePath, fileName, obj);
        return obj.getNumber();
    }
}
