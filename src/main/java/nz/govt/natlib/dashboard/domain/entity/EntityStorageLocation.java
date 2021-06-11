package nz.govt.natlib.dashboard.domain.entity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


@Entity
public class EntityStorageLocation extends BaseStorageLocation {
    @PrimaryKey
    private Long id;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
