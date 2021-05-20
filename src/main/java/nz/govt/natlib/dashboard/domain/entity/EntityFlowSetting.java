package nz.govt.natlib.dashboard.domain.entity;

import com.sleepycat.persist.model.*;

@Entity
public class EntityFlowSetting  extends BaseFlowSetting {
    @PrimaryKey
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
