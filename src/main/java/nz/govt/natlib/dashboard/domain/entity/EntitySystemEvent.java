package nz.govt.natlib.dashboard.domain.entity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import nz.govt.natlib.dashboard.common.metadata.EnumSystemEventLevel;
import nz.govt.natlib.dashboard.common.metadata.EnumSystemEventModule;

@Entity
public class EntitySystemEvent {
    @PrimaryKey
    private Long id;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private Long createdTime;
    private EnumSystemEventLevel level;
    private EnumSystemEventModule module;
    private String eventMsg;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private boolean readFlag = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public EnumSystemEventLevel getLevel() {
        return level;
    }

    public void setLevel(EnumSystemEventLevel level) {
        this.level = level;
    }

    public EnumSystemEventModule getModule() {
        return module;
    }

    public void setModule(EnumSystemEventModule module) {
        this.module = module;
    }

    public String getEventMsg() {
        return eventMsg;
    }

    public void setEventMsg(String eventMsg) {
        this.eventMsg = eventMsg;
    }

    public boolean isReadFlag() {
        return readFlag;
    }

    public void setReadFlag(boolean readFlag) {
        this.readFlag = readFlag;
    }
}
