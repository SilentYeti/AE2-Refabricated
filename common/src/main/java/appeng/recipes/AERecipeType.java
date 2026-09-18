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

package appeng.recipes;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Creates an unregistered {@link RecipeType}, which vanilla offers no way to do.
 * <p>
 * Vanilla's {@code RecipeType.register} both creates the type and puts it in the registry under the {@code minecraft}
 * namespace, which is no use to a mod. NeoForge adds a {@code RecipeType.simple} that only creates one, leaving
 * registration to the caller -- but it adds it to the vanilla interface, so calling it compiles on NeoForge and not in
 * {@code :common}, with no import to give the game away.
 * <p>
 * There is nothing loader-specific in what it does: a {@code RecipeType} is a bare token whose only behaviour is its
 * {@code toString}. So this is the same three lines, somewhere both loaders can reach.
 */
public final class AERecipeType {
    private AERecipeType() {
    }

    public static <T extends Recipe<?>> RecipeType<T> simple(Identifier name) {
        final var toString = name.toString();
        return new RecipeType<T>() {
            @Override
            public String toString() {
                return toString;
            }
        };
    }
}
