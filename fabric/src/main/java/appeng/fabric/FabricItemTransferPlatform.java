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

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import appeng.api.inventories.ItemTransfer;
import appeng.fabric.resources.StorageItemTransfer;
import appeng.platform.ItemTransferPlatform;

/**
 * Fabric side of {@link ItemTransferPlatform}: the neighbour's {@code Storage<ItemVariant>} from
 * {@code ItemStorage.SIDED}, adapted by {@link StorageItemTransfer}. Registered in META-INF/services.
 * <p>
 * Its callers are the inscriber and the molecular assembler pushing their output into a neighbour, neither of which
 * registers on Fabric yet; the client gametest reaches it directly against a vanilla chest.
 */
public class FabricItemTransferPlatform implements ItemTransferPlatform {
    @Override
    public @Nullable ItemTransfer findExternal(Level level, BlockPos pos, Direction side) {
        var storage = ItemStorage.SIDED.find(level, pos, side);
        return storage != null ? new StorageItemTransfer(storage) : null;
    }
}
