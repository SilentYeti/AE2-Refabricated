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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

import appeng.util.BootstrapMinecraft;

/**
 * {@link AECommonRecipes} is the subset of {@link AERecipeTypes} that {@code :fabric} can register, so it will drift
 * from the full list unless something stops it. Same ratchet as {@code AECommonItemsTest}.
 */
@BootstrapMinecraft
class AECommonRecipesTest {

    @Test
    void everyRecipeTypeIsEitherPortedOrExplained() {
        var ported = AECommonRecipes.types().keySet();
        var explained = AECommonRecipes.notYetPortable();

        var unaccounted = new LinkedHashSet<String>();
        AERecipeTypes.all().keySet().forEach(id -> {
            if (!ported.contains(id) && !explained.containsKey(id.getPath())) {
                unaccounted.add(id.toString());
            }
        });

        assertThat(unaccounted)
                .as("every AERecipeTypes entry must be in AECommonRecipes.types() or explained in notYetPortable()")
                .isEmpty();
    }

    @Test
    void notYetPortableDoesNotListPortedOrUnknownTypes() {
        var ported = AECommonRecipes.types().keySet().stream().map(id -> id.getPath()).toList();
        var known = AERecipeTypes.all().keySet().stream().map(id -> id.getPath()).toList();

        assertThat(AECommonRecipes.notYetPortable().keySet())
                .as("notYetPortable() must not list types that are already ported")
                .doesNotContainAnyElementsOf(ported);
        assertThat(known)
                .as("notYetPortable() must only name types AERecipeTypes declares")
                .containsAll(AECommonRecipes.notYetPortable().keySet());
    }

    @Test
    void everyPortedTypeHasItsSerializer() {
        assertThat(AECommonRecipes.serializers().keySet())
                .as("a recipe type without its serializer registers a type nothing can parse")
                .containsExactlyInAnyOrderElementsOf(AECommonRecipes.types().keySet());
    }

    @Test
    void portedTypesMatchTheOnesAERecipeTypesDeclares() {
        AECommonRecipes.types().forEach((id, type) -> assertThat(AERecipeTypes.all())
                .as("AECommonRecipes declares %s, which AERecipeTypes does not", id)
                .containsEntry(id, type));
    }
}
