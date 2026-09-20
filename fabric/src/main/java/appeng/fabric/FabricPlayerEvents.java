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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.platform.PlayerEventPlatform;

/**
 * Fabric side of {@link PlayerEventPlatform}.
 * <p>
 * <b>Does nothing, and that is the whole of it.</b> Fabric has no counterpart to either NeoForge event: nothing there
 * listens for "a machine crafted this" or "this item was used up". Both are announcements for other mods, and no part
 * of AE2 reads them back, so a loader with nowhere to announce them has nothing to do -- unlike a seam AE2 itself
 * depends on, which fails loudly instead. If Fabric ever grows such events, this is where they go.
 */
public class FabricPlayerEvents implements PlayerEventPlatform {
    @Override
    public void autoCrafted(ServerLevel level, ItemStack crafted, Container craftingGrid) {
    }

    @Override
    public void itemDestroyed(Player player, ItemStack destroyed) {
    }
}
