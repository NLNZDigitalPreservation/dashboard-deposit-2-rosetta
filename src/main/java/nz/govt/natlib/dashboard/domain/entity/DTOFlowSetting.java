package nz.govt.natlib.dashboard.domain.entity;

import com.sleepycat.persist.model.Persistent;

@Persistent
public class DTOFlowSetting extends BaseFlowSetting  {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
