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

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.AEBlockCapability;
import appeng.api.AEBlockCapabilityCache;
import appeng.platform.BlockCapabilityPlatform;

/**
 * Fabric side of {@link BlockCapabilityPlatform}, on {@code fabric-api-lookup-api-v1}. Registered in META-INF/services.
 * <p>
 * A handle resolves to {@link BlockApiLookup#get}, which interns by id, so AE2's registration code and anything that
 * looks the same id up meet on one lookup. Until providers are registered for AE2's own APIs every lookup answers null,
 * which is also what NeoForge answers where no provider exists.
 * <p>
 * Fabric has no counterpart to NeoForge's cache invalidation: {@link BlockApiCache} remembers the block entity and the
 * provider but asks the provider on every lookup, so an answer can never be stale and there is nothing to invalidate.
 * The invalidation listener is therefore never called here.
 */
public class FabricBlockCapabilityPlatform implements BlockCapabilityPlatform {
    @Override
    public <A, C> @Nullable A find(AEBlockCapability<A, C> capability, Level level, BlockPos pos,
            @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        return lookup(capability).find(level, pos, state, blockEntity, context);
    }

    @Override
    public <A, C> AEBlockCapabilityCache<A> createCache(AEBlockCapability<A, C> capability, ServerLevel level,
            BlockPos pos, C context, BooleanSupplier isValid, Runnable invalidationListener) {
        var cache = BlockApiCache.create(lookup(capability), level, pos);
        return () -> isValid.getAsBoolean() ? cache.find(context) : null;
    }

    public static <A, C> BlockApiLookup<A, C> lookup(AEBlockCapability<A, C> capability) {
        return capability.getOrCreatePlatformHandle(FabricBlockCapabilityPlatform::resolve);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <A, C> BlockApiLookup<A, C> resolve(AEBlockCapability<A, C> capability) {
        Class contextClass = switch (capability.context()) {
            case SIDED -> Direction.class;
            case NONE -> Void.class;
        };
        return (BlockApiLookup<A, C>) BlockApiLookup.get(capability.id(), capability.apiClass(), contextClass);
    }
}
