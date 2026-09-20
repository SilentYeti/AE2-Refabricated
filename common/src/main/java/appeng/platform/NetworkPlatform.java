/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.platform;

import java.util.ServiceLoader;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * The loader's packet buffers, and sending a packet through it.
 * <p>
 * Vanilla's {@link RegistryFriendlyByteBuf} takes only the registries; NeoForge's takes a connection type as well, and
 * its own stream codecs read it to decide how much they may write. AE2 writes block entity update data through such a
 * buffer, so the buffer has to be the loader's own rather than the vanilla one -- on NeoForge a buffer that claimed to
 * be a vanilla connection could quietly write less than it read back.
 */
public interface NetworkPlatform {
    RegistryFriendlyByteBuf createBuffer(ByteBuf backing, RegistryAccess registries);

    /**
     * Sends a payload from the client to the server. Only called on the client, where a connection exists.
     */
    void sendToServer(CustomPacketPayload payload);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /**
     * Sends to everyone within {@code radius} of the point who can see it, skipping {@code except} -- how AE2 tells
     * nearby players about something a machine did.
     */
    void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer except, double x, double y, double z,
            double radius, CustomPacketPayload payload);

    // --- lookup ---

    static NetworkPlatform get() {
        var instance = Holder0.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("No " + NetworkPlatform.class.getName()
                    + " implementation was found on the classpath. The :neoforge or :fabric module must be "
                    + "present and register one via META-INF/services.");
        }
        return instance;
    }

    /**
     * Loads through this interface's own class loader, not the thread's context loader, which is what
     * {@code ServiceLoader.load(Class)} would use. Under a mod loader the context loader is not reliably the one that
     * loaded AE2: when it is not, the implementation gets defined a second time by the wrong loader and its first
     * reference back into AE2 fails with a {@code LinkageError} -- depending only on which thread happened to touch
     * this first.
     */
    final class Holder0 {
        private static final NetworkPlatform INSTANCE = ServiceLoader
                .load(NetworkPlatform.class, NetworkPlatform.class.getClassLoader())
                .findFirst()
                .orElse(null);

        private Holder0() {
        }
    }
}
