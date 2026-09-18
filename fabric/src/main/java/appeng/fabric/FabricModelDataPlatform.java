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

import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.platform.ModelDataPlatform;

/**
 * Fabric side of {@link ModelDataPlatform}.
 * <p>
 * <b>Unreachable today, deliberately loud.</b> Every caller is an AE2 block entity or the block next to one, and no AE2
 * block entity registers on Fabric yet, so nothing can reach this. It throws rather than doing nothing, because a
 * redraw that silently never happens looks like a rendering bug in the model, a long way from here.
 * <p>
 * The implementation belongs with the block entities (stage 6), and is the other half of whatever carries
 * {@code AEModelData} across on Fabric: Fabric's block entities supply render data of their own, and asking for it
 * again means marking the position dirty on the client -- {@code level.setBlocksDirty(pos, state, state)} -- with the
 * server side instead sending the block entity's update packet.
 */
public class FabricModelDataPlatform implements ModelDataPlatform {
    @Override
    public void requestModelDataUpdate(BlockEntity blockEntity) {
        throw new UnsupportedOperationException(
                "Asking a block entity to rebuild its model data is not implemented on Fabric yet (stage 6, the "
                        + "block entities): " + blockEntity);
    }
}
