package nz.govt.natlib.dashboard.domain.entity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import nz.govt.natlib.dashboard.common.metadata.EnumUserRole;

@Entity
public class EntityWhiteList {
    @PrimaryKey
    private Long id;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private String userName;
    private EnumUserRole role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public EnumUserRole getRole() {
        return role;
    }

    public void setRole(EnumUserRole role) {
        this.role = role;
    }
}
