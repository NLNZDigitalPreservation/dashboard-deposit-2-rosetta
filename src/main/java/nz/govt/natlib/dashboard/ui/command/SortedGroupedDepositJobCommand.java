package nz.govt.natlib.dashboard.ui.command;

import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;

import java.util.List;
import java.util.Map;

public class SortedGroupedDepositJobCommand {
    private List<String> keys;
    private Map<String,List<EntityDepositJob>> payload;

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public Map<String, List<EntityDepositJob>> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, List<EntityDepositJob>> payload) {
        this.payload = payload;
    }
}
