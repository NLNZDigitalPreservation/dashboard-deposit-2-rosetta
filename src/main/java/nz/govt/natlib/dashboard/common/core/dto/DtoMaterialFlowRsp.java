package nz.govt.natlib.dashboard.common.core.dto;

import java.util.List;

public class DtoMaterialFlowRsp {
    private int total_record_count;
    private List<MaterialFlow> profile_material_flow;

    public int getTotal_record_count() {
        return total_record_count;
    }

    public void setTotal_record_count(int total_record_count) {
        this.total_record_count = total_record_count;
    }

    public List<MaterialFlow> getProfile_material_flow() {
        return profile_material_flow;
    }

    public void setProfile_material_flow(List<MaterialFlow> profile_material_flow) {
        this.profile_material_flow = profile_material_flow;
    }

    public static class MaterialFlow {
        private boolean active;
        private String id;
        private String name;

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
