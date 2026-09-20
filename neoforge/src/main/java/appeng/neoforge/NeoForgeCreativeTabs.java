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

import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import appeng.core.MainCreativeTab;

/**
 * Adds AE2's items to other mods' and vanilla's creative tabs, from NeoForge's event. This lived on
 * {@link MainCreativeTab} itself, which is otherwise loader-agnostic; Fabric does the same from its item-group API in
 * {@code FabricItems}.
 */
public final class NeoForgeCreativeTabs {
    private NeoForgeCreativeTabs() {
    }

    public static void addExternalItems(BuildCreativeModeTabContentsEvent contents) {
        for (var itemDefinition : MainCreativeTab.externalItems(contents.getTabKey())) {
            contents.accept(itemDefinition);
        }
    }
}
