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

package appeng.items.misc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

import appeng.util.BootstrapMinecraft;

/**
 * {@code CustomData.contains} is NeoForge's addition to the vanilla class, so {@link MissingContentItem} asks
 * {@code copyTag().contains(...)} instead, which is reachable from {@code :common} as well.
 * <p>
 * This is the assumption that substitution rests on. It is here rather than beside the item because only the NeoForge
 * side has the patched class to compare against -- which is the point: if NeoForge ever gave {@code contains} a
 * different meaning, this fails and names the reason, instead of the tooltip of a broken stack quietly going blank.
 */
@BootstrapMinecraft
class CustomDataTest {
    @Test
    void containsIsTheSameQuestionAsTheCopiedTagsContains() {
        var tag = new CompoundTag();
        tag.putString("id", "minecraft:stone");
        tag.putLong("count", 3);
        var data = CustomData.of(tag);

        for (var key : new String[] { "id", "count", "absent", "" }) {
            assertThat(data.contains(key)).as("contains(%s)", key).isEqualTo(data.copyTag().contains(key));
        }
    }

    @Test
    void neitherSeesAKeyThatIsNotAtTheTopLevel() {
        var nested = new CompoundTag();
        nested.putString("id", "minecraft:stone");
        var tag = new CompoundTag();
        tag.put("components", nested);
        var data = CustomData.of(tag);

        assertThat(data.contains("id")).isFalse();
        assertThat(data.copyTag().contains("id")).isFalse();
    }
}
