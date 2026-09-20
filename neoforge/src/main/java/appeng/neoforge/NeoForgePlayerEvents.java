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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import appeng.platform.PlayerEventPlatform;
import appeng.util.Platform;

/**
 * NeoForge side of {@link PlayerEventPlatform}: the two events AE2 posted directly before. Registered in
 * META-INF/services.
 */
public class NeoForgePlayerEvents implements PlayerEventPlatform {
    @Override
    public void autoCrafted(ServerLevel level, ItemStack crafted, Container craftingGrid) {
        var fakePlayer = Platform.getFakePlayer(level, null);
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(fakePlayer, crafted, craftingGrid));
    }

    @Override
    public void itemDestroyed(Player player, ItemStack destroyed) {
        NeoForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, destroyed, null));
    }
}
