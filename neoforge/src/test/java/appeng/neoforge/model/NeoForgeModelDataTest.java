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

package appeng.neoforge.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.neoforged.neoforge.model.data.ModelData;

import appeng.api.client.AEModelData;
import appeng.api.client.AEModelProperty;

/**
 * AE2 declares its model data with its own {@link AEModelProperty} now, and {@link NeoForgeModelData} is the single
 * NeoForge property that carries it. Nothing else automated looks at rendering, so these pin the two things the models
 * rely on: that what a block entity put in comes back out, and that asking for a property nobody set answers null
 * rather than throwing -- which is what the models check for, several of them through
 * {@code Objects.requireNonNullElse}.
 */
class NeoForgeModelDataTest {
    private static final AEModelProperty<String> NAME = new AEModelProperty<>();
    private static final AEModelProperty<Integer> COUNT = new AEModelProperty<>();

    @Test
    void whatGoesInComesBackOut() {
        var data = AEModelData.builder().with(NAME, "drive").with(COUNT, 7).build();

        var unwrapped = NeoForgeModelData.unwrap(NeoForgeModelData.of(data));

        assertThat(unwrapped).isSameAs(data);
        assertThat(unwrapped.get(NAME)).isEqualTo("drive");
        assertThat(unwrapped.get(COUNT)).isEqualTo(7);
        assertThat(unwrapped.has(NAME)).isTrue();
    }

    @Test
    void emptyDataStaysNeoForgesEmpty() {
        // What a block entity with no extra state returned before there was a wrapper at all
        assertThat(NeoForgeModelData.of(AEModelData.EMPTY)).isSameAs(ModelData.EMPTY);
    }

    @Test
    void modelDataThatIsNotAe2sReadsAsEmpty() {
        // Another mod's block, or one of AE2's with no extra state: every lookup answers null, as it did when the
        // properties were simply absent
        assertThat(NeoForgeModelData.unwrap(ModelData.EMPTY)).isSameAs(AEModelData.EMPTY);
        assertThat(NeoForgeModelData.unwrap(ModelData.EMPTY).get(NAME)).isNull();
        assertThat(NeoForgeModelData.unwrap(ModelData.EMPTY).has(NAME)).isFalse();
    }

    @Test
    void aPropertyNobodySetIsNullNotAThrow() {
        var data = AEModelData.of(NAME, "cable");

        assertThat(data.get(COUNT)).isNull();
        assertThat(data.has(COUNT)).isFalse();
    }

    @Test
    void twoPropertiesOfTheSameTypeAreDifferentKeys() {
        // Identity keys, as ModelProperty was: declare them static final and hand them out
        AEModelProperty<String> other = new AEModelProperty<>();
        var data = AEModelData.of(NAME, "cable");

        assertThat(data.get(other)).isNull();
    }

    @Test
    void derivingKeepsWhatWasThereAndAddsToIt() {
        var data = AEModelData.of(NAME, "monitor").derive().with(COUNT, 1).build();

        assertThat(data.get(NAME)).isEqualTo("monitor");
        assertThat(data.get(COUNT)).isEqualTo(1);
    }
}
