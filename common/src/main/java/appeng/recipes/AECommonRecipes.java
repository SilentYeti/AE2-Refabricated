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

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;

/**
 * The recipe types and serializers whose recipe classes are loader-agnostic.
 * <p>
 * The recipe counterpart of {@code AECommonItems} and {@code AECommonBlocks}, and there for the same reason:
 * {@link AERecipeTypes} and {@link AERecipeSerializers} stay the authority, but they name recipe classes that have not
 * reached {@code :common} yet, so they cannot be read from a loader that has not got those classes. This is the subset
 * that can, and it is what {@code :fabric} registers.
 * <p>
 * An entry belongs here once its recipe class compiles in {@code :common}. {@code AECommonRecipesTest} fails if
 * anything here has drifted from {@link AERecipeTypes}, and lists what is still missing.
 */
public final class AECommonRecipes {
    private AECommonRecipes() {
    }

    private static final Map<Identifier, RecipeType<?>> TYPES = new LinkedHashMap<>();
    private static final Map<Identifier, RecipeSerializer<?>> SERIALIZERS = new LinkedHashMap<>();

    static {
        entry(InscriberRecipe.TYPE_ID, InscriberRecipe.TYPE, InscriberRecipe.SERIALIZER);
        entry(EntropyRecipe.TYPE_ID, EntropyRecipe.TYPE, EntropyRecipe.SERIALIZER);
        entry(MatterCannonAmmo.TYPE_ID, MatterCannonAmmo.TYPE, MatterCannonAmmo.SERIALIZER);
        entry(ChargerRecipe.TYPE_ID, ChargerRecipe.TYPE, ChargerRecipe.SERIALIZER);
    }

    public static Map<Identifier, RecipeType<?>> types() {
        return Map.copyOf(TYPES);
    }

    public static Map<Identifier, RecipeSerializer<?>> serializers() {
        return Map.copyOf(SERIALIZERS);
    }

    /**
     * Why each remaining {@link AERecipeTypes} entry is not here yet, keyed by the recipe id. Read by
     * {@code AECommonRecipesTest}, which fails if a type is in neither this map nor {@link #types()}.
     */
    public static Map<String, String> notYetPortable() {
        return Map.of(
                "transform", "TransformRecipe: assigns quantum bridge frequencies",
                "quartz_cutting", "QuartzCuttingRecipe: needs neoforge.common and AEItems",
                "crafting_unit_transform", "CraftingUnitTransformRecipe: needs the crafting unit blocks",
                "storage_cell_disassembly", "StorageCellDisassemblyRecipe: needs the storage cell API");
    }

    private static void entry(Identifier id, RecipeType<?> type, RecipeSerializer<?> serializer) {
        TYPES.put(id, type);
        SERIALIZERS.put(id, serializer);
    }
}
