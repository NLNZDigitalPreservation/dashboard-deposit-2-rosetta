package nz.govt.natlib.dashboard.ui.command;

import com.exlibris.dps.sdk.pds.PdsUserInfo;

import java.util.List;

public class UserAccessRspCommand {
    private String username;
    private String pdsHandle;
    private PdsUserInfo userInfo;

    private List<RawProducerCommand> producers;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPdsHandle() {
        return pdsHandle;
    }

    public void setPdsHandle(String pdsHandle) {
        this.pdsHandle = pdsHandle;
    }

    public PdsUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(PdsUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<RawProducerCommand> getProducers() {
        return producers;
    }

    public void setProducers(List<RawProducerCommand> producers) {
        this.producers = producers;
    }
}
