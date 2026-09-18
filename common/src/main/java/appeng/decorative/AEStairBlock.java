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

package appeng.decorative;

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A plain {@link StairBlock} that can be constructed from loader-agnostic code.
 * <p>
 * {@code StairBlock}'s constructor is {@code protected} in vanilla and only public because AE2's access transformer
 * widens it, which makes it unreachable from {@code :common} -- the same wall {@code CreativeModeTab.Output} put up. A
 * subclass may call a protected super constructor, so this adds nothing but reach: no state, no overrides, and
 * {@code instanceof StairBlock} still holds.
 */
public class AEStairBlock extends StairBlock {
    public AEStairBlock(BlockState baseState, Properties properties) {
        super(baseState, properties);
    }
}
