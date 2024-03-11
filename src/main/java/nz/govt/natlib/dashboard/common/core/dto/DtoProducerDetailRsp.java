package nz.govt.natlib.dashboard.common.core.dto;

public class DtoProducerDetailRsp {
    private String id;
    private String name;
    private String link;

    private ProducerProfile profile;

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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public ProducerProfile getProfile() {
        return profile;
    }

    public void setProfile(ProducerProfile profile) {
        this.profile = profile;
    }

    public static class ProducerProfile {
        private String id;
        private String name;
        private String link;

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

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }
}
