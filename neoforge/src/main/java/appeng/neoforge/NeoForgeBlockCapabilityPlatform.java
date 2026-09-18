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

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import appeng.api.AEBlockCapability;
import appeng.api.AEBlockCapabilityCache;
import appeng.platform.BlockCapabilityPlatform;

/**
 * NeoForge side of {@link BlockCapabilityPlatform}: exactly the calls AE2 made before its capabilities became handles.
 * Registered in META-INF/services.
 */
public class NeoForgeBlockCapabilityPlatform implements BlockCapabilityPlatform {
    @Override
    public <A, C> @Nullable A find(AEBlockCapability<A, C> capability, Level level, BlockPos pos,
            @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        return level.getCapability(NeoForgeCapabilities.of(capability), pos, state, blockEntity, context);
    }

    @Override
    public <A, C> AEBlockCapabilityCache<A> createCache(AEBlockCapability<A, C> capability, ServerLevel level,
            BlockPos pos, C context, BooleanSupplier isValid, Runnable invalidationListener) {
        var cache = BlockCapabilityCache.create(NeoForgeCapabilities.of(capability), level, pos, context, isValid,
                invalidationListener);
        return cache::getCapability;
    }
}
