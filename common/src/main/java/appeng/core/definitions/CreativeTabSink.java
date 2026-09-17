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

package appeng.core.definitions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Where an item puts the stacks it wants to appear in AE2's creative tab.
 * <p>
 * This exists because {@code CreativeModeTab.Output} is {@code protected} in vanilla and only widened by an access
 * transformer, which makes it unusable from {@code :common} -- referencing it was enough on its own to pin every
 * {@link appeng.items.AEBaseItem} subclass to {@code :neoforge}. Each loader adapts its own tab callback to this
 * interface.
 */
@FunctionalInterface
public interface CreativeTabSink {
    void accept(ItemStack stack);

    default void accept(ItemLike item) {
        accept(new ItemStack(item));
    }
}
