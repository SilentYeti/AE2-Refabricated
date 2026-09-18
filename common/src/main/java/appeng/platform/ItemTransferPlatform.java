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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import appeng.api.inventories.ItemTransfer;

/**
 * Reaches item inventories that belong to other blocks, through whatever item-transfer API the running loader has.
 * <p>
 * Only the lookup is here. Adapting AE2's own inventories <em>outwards</em> -- exposing an {@code InternalInventory} to
 * the loader -- is left to each loader's module, because the adapter's type is the loader's own
 * ({@code ResourceHandler<ItemResource>} on NeoForge, {@code Storage<ItemVariant>} on Fabric) and has no vanilla
 * equivalent to put in a signature here.
 */
public interface ItemTransferPlatform {
    /**
     * @return The item inventory exposed by the block at {@code pos} on its {@code side} face, or null if there is
     *         none.
     */
    @Nullable
    ItemTransfer findExternal(Level level, BlockPos pos, Direction side);

    // --- lookup ---

    static ItemTransferPlatform get() {
        var instance = Holder0.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("No " + ItemTransferPlatform.class.getName()
                    + " implementation was found on the classpath. The :neoforge or :fabric module must be "
                    + "present and register one via META-INF/services.");
        }
        return instance;
    }

    final class Holder0 {
        private static final ItemTransferPlatform INSTANCE = ServiceLoader.load(ItemTransferPlatform.class)
                .findFirst()
                .orElse(null);

        private Holder0() {
        }
    }
}
