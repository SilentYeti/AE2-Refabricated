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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import appeng.api.inventories.ItemTransfer;
import appeng.platform.ItemTransferPlatform;

/**
 * Fabric side of {@link ItemTransferPlatform}.
 * <p>
 * <b>Not implemented yet, deliberately.</b> Its only callers are the inscriber and the molecular assembler pushing
 * their output into a neighbour, and neither block registers on Fabric -- so this is unreachable, and failing loudly is
 * safer than answering "no inventory there", which would look like a working machine that silently never pushes its
 * output.
 * <p>
 * The intended shape, for stage 4: look the neighbour up with {@code ItemStorage.SIDED.find(level, pos, side)} and
 * adapt the {@code Storage<ItemVariant>} it returns to {@link ItemTransfer}, as {@code PlatformInventoryWrapper} does
 * for NeoForge's handler. Insertions and extractions each open and commit their own {@code Transaction}; simulations
 * open one and let it abort.
 */
public class FabricItemTransferPlatform implements ItemTransferPlatform {
    @Override
    public @Nullable ItemTransfer findExternal(Level level, BlockPos pos, Direction side) {
        throw new UnsupportedOperationException("External item inventories cannot be reached on Fabric yet (stage 4 "
                + "of PORTING.md). See FabricItemTransferPlatform's javadoc for the intended implementation.");
    }
}
