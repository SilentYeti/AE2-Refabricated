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
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.AEBlockCapability;
import appeng.api.AEBlockCapabilityCache;

/**
 * Resolves {@link AEBlockCapability} lookups against the running loader: NeoForge's block capabilities, or Fabric's
 * {@code BlockApiLookup}. Code outside the loader modules goes through {@link AEBlockCapability} rather than calling
 * this directly.
 * <p>
 * Only lookups are here. <em>Providing</em> a capability -- registering what a block entity or part exposes -- is done
 * by each loader's own registration code, which has the loader object to hand.
 */
public interface BlockCapabilityPlatform {
    <A, C> @Nullable A find(AEBlockCapability<A, C> capability, Level level, BlockPos pos, @Nullable BlockState state,
            @Nullable BlockEntity blockEntity, C context);

    <A, C> AEBlockCapabilityCache<A> createCache(AEBlockCapability<A, C> capability, ServerLevel level, BlockPos pos,
            C context, BooleanSupplier isValid, Runnable invalidationListener);

    // --- lookup ---

    static BlockCapabilityPlatform get() {
        var instance = Holder0.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("No " + BlockCapabilityPlatform.class.getName()
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
        private static final BlockCapabilityPlatform INSTANCE = ServiceLoader
                .load(BlockCapabilityPlatform.class, BlockCapabilityPlatform.class.getClassLoader())
                .findFirst()
                .orElse(null);

        private Holder0() {
        }
    }
}
