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

package appeng.neoforge;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

import appeng.api.AEBlockCapability;

/**
 * Converts between {@link AEBlockCapability} and NeoForge's {@link BlockCapability}.
 * <p>
 * NeoForge interns capabilities by name, so resolving a handle yields the very object that was there before AE2's
 * capabilities became handles -- registrations and lookups on either side still meet.
 */
public final class NeoForgeCapabilities {
    private NeoForgeCapabilities() {
    }

    /**
     * @return NeoForge's capability for this handle; what registration code registers providers against.
     */
    public static <A, C> BlockCapability<A, C> of(AEBlockCapability<A, C> capability) {
        return capability.getOrCreatePlatformHandle(NeoForgeCapabilities::resolve);
    }

    /**
     * A handle for a capability NeoForge (or another mod) defines, so AE2 code written against handles can use it --
     * the P2P tunnels forward NeoForge's own item, fluid and energy capabilities.
     */
    public static <A> AEBlockCapability<A, @Nullable Direction> handle(
            BlockCapability<A, @Nullable Direction> capability) {
        var handle = AEBlockCapability.sided(capability.name(), capability.typeClass());
        // Already known, so seed the slot rather than have the first lookup resolve it by name
        handle.getOrCreatePlatformHandle(h -> capability);
        return handle;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <A, C> BlockCapability<A, C> resolve(AEBlockCapability<A, C> capability) {
        // Matches what BlockCapability.createSided / createVoid pass, so an existing capability is found, not rejected
        // as a type mismatch
        Class contextClass = switch (capability.context()) {
            case SIDED -> Direction.class;
            case NONE -> void.class;
        };
        return (BlockCapability<A, C>) BlockCapability.create(capability.id(), capability.apiClass(), contextClass);
    }
}
