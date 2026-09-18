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

import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.client.AEModelData;

/**
 * Tells the loader that a block entity's {@link AEModelData} has changed and its model has to be asked again.
 * <p>
 * The counterpart to {@code AEModelData} itself: AE2 owns the data, but each loader owns when a block gets redrawn. On
 * NeoForge this is {@code BlockEntity.requestModelDataUpdate}, which NeoForge patches onto the vanilla class -- one of
 * the couplings that is invisible to an import, since calling it needs no import at all.
 * <p>
 * Takes the block entity rather than being a method on AE2's own base class, because two of the callers have only a
 * vanilla {@link BlockEntity} to hand: a block reacting to a neighbour, and the plane connection helper.
 */
public interface ModelDataPlatform {
    void requestModelDataUpdate(BlockEntity blockEntity);

    // --- lookup ---

    static ModelDataPlatform get() {
        var instance = Holder0.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("No " + ModelDataPlatform.class.getName()
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
        private static final ModelDataPlatform INSTANCE = ServiceLoader
                .load(ModelDataPlatform.class, ModelDataPlatform.class.getClassLoader())
                .findFirst()
                .orElse(null);

        private Holder0() {
        }
    }
}
