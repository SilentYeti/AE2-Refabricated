package appeng.client;

import net.minecraft.client.KeyMapping;

import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.HotkeyPacket;
import appeng.platform.NetworkPlatform;

public record Hotkey(String name, KeyMapping mapping) {
    public void check() {
        while (mapping().consumeClick()) {
            ServerboundPacket message = new HotkeyPacket(this);
            NetworkPlatform.get().sendToServer(message);
        }
    }
}
