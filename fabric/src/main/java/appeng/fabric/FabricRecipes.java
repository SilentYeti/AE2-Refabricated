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

import appeng.recipes.AECommonRecipes;

/**
 * Registers the recipe types and serializers from {@link AECommonRecipes}.
 * <p>
 * A recipe whose serializer is not registered is logged and skipped rather than failing the data pack, so each one
 * registered here is a set of AE2 recipes that starts loading.
 */
public final class FabricRecipes {
    private FabricRecipes() {
    }

    public static void register() {
        AECommonRecipes.types()
                .forEach((id, type) -> Registry.register(BuiltInRegistries.RECIPE_TYPE, id, type));
        AECommonRecipes.serializers()
                .forEach((id, serializer) -> Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer));
    }

    public static int registeredCount() {
        return AECommonRecipes.types().size();
    }
}
