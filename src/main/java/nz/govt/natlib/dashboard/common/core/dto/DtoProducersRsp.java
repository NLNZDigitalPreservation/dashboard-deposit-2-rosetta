package nz.govt.natlib.dashboard.common.core.dto;

import java.util.List;

public class DtoProducersRsp {
    private List<DtoProducersRsp.Producer> producer;
    private int total_record_count;

    public List<DtoProducersRsp.Producer> getProducer() {
        return producer;
    }

    public void setProducer(List<DtoProducersRsp.Producer> producer) {
        this.producer = producer;
    }

    public int getTotal_record_count() {
        return total_record_count;
    }

    public void setTotal_record_count(int total_record_count) {
        this.total_record_count = total_record_count;
    }

    public static class Producer {
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
