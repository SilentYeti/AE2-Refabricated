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

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import appeng.api.ids.AEComponents;

/**
 * Registers AE2's data component types from {@link AEComponents}.
 * <p>
 * All of them, not a subset like the items and recipes: a component type is a codec and a stream codec over vanilla
 * types, so none of them needs anything that has not crossed. The items that <em>store</em> them are another matter,
 * and are still gated in {@code AECommonItems.notYetPortable()}.
 * <p>
 * The ids are the ones NeoForge registers, because {@link AEComponents} is now one table read by both loaders. That
 * matters more here than it looks: a component type registered under a different id would make every saved stack
 * carrying it unreadable.
 */
public final class FabricComponents {
    private FabricComponents() {
    }

    public static void register() {
        AEComponents.all()
                .forEach((id, type) -> Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, type));
    }

    public static int registeredCount() {
        return AEComponents.all().size();
    }
}
