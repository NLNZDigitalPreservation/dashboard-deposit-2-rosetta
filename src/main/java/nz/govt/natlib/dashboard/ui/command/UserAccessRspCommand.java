package nz.govt.natlib.dashboard.ui.command;


import nz.govt.natlib.dashboard.common.metadata.UserInfo;

import java.util.List;

public class UserAccessRspCommand {
    private String username;
    private String sessionId;
    private UserInfo userInfo;

    private List<RawProducerCommand> producers;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getsessionId() {
        return sessionId;
    }

    public void setsessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<RawProducerCommand> getProducers() {
        return producers;
    }

    public void setProducers(List<RawProducerCommand> producers) {
        this.producers = producers;
    }
}
