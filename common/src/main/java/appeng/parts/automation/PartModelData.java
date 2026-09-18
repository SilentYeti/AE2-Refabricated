package appeng.parts.automation;

import appeng.api.client.AEModelProperty;

public final class PartModelData {
    private PartModelData() {
    }

    public static final AEModelProperty<StatusIndicatorState> STATUS_INDICATOR = new AEModelProperty<>();
    public static final AEModelProperty<PlaneConnections> CONNECTIONS = new AEModelProperty<>();
    public static final AEModelProperty<Long> P2P_FREQUENCY = new AEModelProperty<>();
    public static final AEModelProperty<Boolean> LEVEL_EMITTER_ON = new AEModelProperty<>();
    public static final AEModelProperty<Boolean> CABLE_ANCHOR_SHORT = new AEModelProperty<>();
    public static final AEModelProperty<Boolean> MONITOR_LOCKED = new AEModelProperty<>();

    public enum StatusIndicatorState {
        ACTIVE,
        POWERED,
        UNPOWERED
    }
}
