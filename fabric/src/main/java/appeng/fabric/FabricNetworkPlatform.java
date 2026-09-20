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

package appeng.fabric;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import appeng.platform.NetworkPlatform;

/**
 * Fabric side of {@link NetworkPlatform}: the vanilla buffer, which is all Fabric has -- it adds nothing to the
 * connection the way NeoForge's connection type does. Registered in META-INF/services.
 */
public class FabricNetworkPlatform implements NetworkPlatform {
    @Override
    public RegistryFriendlyByteBuf createBuffer(ByteBuf backing, RegistryAccess registries) {
        return new RegistryFriendlyByteBuf(backing, registries);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    /**
     * Fabric has no counterpart to NeoForge's {@code sendToPlayersNear}, so this does what it does: everyone in the
     * level within the radius, except the one player to skip. Fabric refuses to send a payload whose type the receiver
     * cannot handle, so each is asked first -- NeoForge drops those itself.
     */
    @Override
    public void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer except, double x, double y, double z,
            double radius, CustomPacketPayload payload) {
        var radiusSquared = radius * radius;
        for (var player : level.players()) {
            if (player == except || player.distanceToSqr(x, y, z) > radiusSquared) {
                continue;
            }
            if (ServerPlayNetworking.canSend(player, payload.type())) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
