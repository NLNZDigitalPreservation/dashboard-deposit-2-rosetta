package nz.govt.natlib.dashboard.ui.command;

import java.util.ArrayList;
import java.util.List;

public class RawProducerCommand {
    private String name;
    private String id;
    private List<RawMaterialFlowCommand> materialFlows = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<RawMaterialFlowCommand> getMaterialFlows() {
        return materialFlows;
    }

    public void setMaterialFlows(List<RawMaterialFlowCommand> materialFlows) {
        this.materialFlows = materialFlows;
    }

    public void clear() {
        this.materialFlows.clear();
    }
}
