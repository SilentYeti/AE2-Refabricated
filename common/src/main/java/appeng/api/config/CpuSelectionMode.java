package appeng.api.config;

/**
 * Controls for which types of crafting requests a crafting CPU is available to be automatically selected.
 */
public enum CpuSelectionMode {
    /**
     * Use for all types of auto-crafting requests.
     */
    ANY,
    /**
     * Only use for auto-crafting initiated by players ({@code IActionSource#player()}).
     */
    PLAYER_ONLY,
    /**
     * Only use for auto-crafting initiated by machines ({@code IActionSource#machine()}).
     */
    MACHINE_ONLY
}
