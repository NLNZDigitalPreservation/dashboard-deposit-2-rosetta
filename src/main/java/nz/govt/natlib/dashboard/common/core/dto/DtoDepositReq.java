package nz.govt.natlib.dashboard.common.core.dto;

public class DtoDepositReq {
    private String link = "string";
    private String subdirectory;
    private Producer producer;
    private MaterialFlow material_flow;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSubdirectory() {
        return subdirectory;
    }

    public void setSubdirectory(String subdirectory) {
        this.subdirectory = subdirectory;
    }

    public DtoDepositReq.Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public MaterialFlow getMaterial_flow() {
        return material_flow;
    }

    public void setMaterial_flow(MaterialFlow material_flow) {
        this.material_flow = material_flow;
    }

    public static class Producer {
        private String value;

        public Producer() {
        }

        public Producer(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class MaterialFlow {
        private String value;

        public MaterialFlow() {
        }

        public MaterialFlow(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

