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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Tells the loader that AE2 did something to a player's items that other mods expect to hear about: a machine finished
 * a craft, or an item was used up.
 * <p>
 * Nothing in AE2 depends on the answer -- these are announcements, and a loader with nowhere to announce them does
 * nothing, which is what Fabric does: it has no counterpart to either NeoForge event.
 */
public interface PlayerEventPlatform {
    /**
     * A machine crafted something on a player's behalf. On NeoForge this is {@code PlayerEvent.ItemCraftedEvent},
     * posted for the level's fake player, which is how another mod sees AE2's auto-crafting output.
     */
    void autoCrafted(ServerLevel level, ItemStack crafted, Container craftingGrid);

    /**
     * An item in a player's inventory was used up. On NeoForge this is {@code PlayerDestroyItemEvent}.
     */
    void itemDestroyed(Player player, ItemStack destroyed);

    // --- lookup ---

    static PlayerEventPlatform get() {
        var instance = Holder0.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("No " + PlayerEventPlatform.class.getName()
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
        private static final PlayerEventPlatform INSTANCE = ServiceLoader
                .load(PlayerEventPlatform.class, PlayerEventPlatform.class.getClassLoader())
                .findFirst()
                .orElse(null);

        private Holder0() {
        }
    }
}
