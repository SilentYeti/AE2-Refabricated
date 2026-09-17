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

package appeng.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.Tags;

/**
 * {@link ConventionTags} spells its {@code c:} tag ids out by hand rather than taking them from NeoForge's {@link Tags}
 * constants, because referencing {@code net.neoforged} kept the class -- and transitively every AE2 tool and material
 * item -- out of {@code :common}.
 * <p>
 * That trades a compile-time guarantee for a hand-copied string, so this test puts the guarantee back: it is the only
 * place the NeoForge constants are still named, and it fails if any id drifts from the one AE2 used to inherit. If
 * NeoForge renames a tag, this breaks rather than AE2 silently losing recipe compatibility.
 */
class ConventionTagsTest {
    @TestFactory
    Stream<DynamicTest> matchesNeoForgeConventionTags() {
        return Stream.of(
                pin("DUSTS", ConventionTags.DUSTS, Tags.Items.DUSTS),
                pin("GEMS", ConventionTags.GEMS, Tags.Items.GEMS),
                pin("NETHER_QUARTZ", ConventionTags.NETHER_QUARTZ, Tags.Items.GEMS_QUARTZ),
                pin("COPPER_INGOT", ConventionTags.COPPER_INGOT, Tags.Items.INGOTS_COPPER),
                pin("GOLD_NUGGET", ConventionTags.GOLD_NUGGET, Tags.Items.NUGGETS_GOLD),
                pin("GOLD_INGOT", ConventionTags.GOLD_INGOT, Tags.Items.INGOTS_GOLD),
                pin("IRON_NUGGET", ConventionTags.IRON_NUGGET, Tags.Items.NUGGETS_IRON),
                pin("IRON_INGOT", ConventionTags.IRON_INGOT, Tags.Items.INGOTS_IRON),
                pin("DIAMOND", ConventionTags.DIAMOND, Tags.Items.GEMS_DIAMOND),
                pin("REDSTONE", ConventionTags.REDSTONE, Tags.Items.DUSTS_REDSTONE),
                pin("GLOWSTONE", ConventionTags.GLOWSTONE, Tags.Items.DUSTS_GLOWSTONE),
                pin("ENDER_PEARL", ConventionTags.ENDER_PEARL, Tags.Items.ENDER_PEARLS),
                pin("WOOD_STICK", ConventionTags.WOOD_STICK, Tags.Items.RODS_WOODEN),
                pin("CHEST", ConventionTags.CHEST, Tags.Items.CHESTS_WOODEN),
                pin("STONE", ConventionTags.STONE, Tags.Items.STONES),
                pin("GLASS", ConventionTags.GLASS, Tags.Items.GLASS_BLOCKS),
                pin("GLASS_CHEAP", ConventionTags.GLASS_CHEAP, Tags.Items.GLASS_BLOCKS_CHEAP),
                pin("BUDDING_BLOCKS", ConventionTags.BUDDING_BLOCKS, Tags.Items.BUDDING_BLOCKS),
                pin("BUDS", ConventionTags.BUDS, Tags.Items.BUDS),
                pin("CLUSTERS", ConventionTags.CLUSTERS, Tags.Items.CLUSTERS),
                pin("GLASS_BLOCK", ConventionTags.GLASS_BLOCK, Tags.Blocks.GLASS_BLOCKS),
                pin("BUDDING_BLOCKS_BLOCKS", ConventionTags.BUDDING_BLOCKS_BLOCKS, Tags.Blocks.BUDDING_BLOCKS),
                pin("BUDS_BLOCKS", ConventionTags.BUDS_BLOCKS, Tags.Blocks.BUDS),
                pin("CLUSTERS_BLOCKS", ConventionTags.CLUSTERS_BLOCKS, Tags.Blocks.CLUSTERS),
                pin("IMMOVABLE_BLOCKS", ConventionTags.IMMOVABLE_BLOCKS, Tags.Blocks.RELOCATION_NOT_SUPPORTED),
                pin("METEORITE_OCEAN", ConventionTags.METEORITE_OCEAN, Tags.Biomes.IS_OCEAN));
    }

    private static DynamicTest pin(String name, TagKey<?> ours, TagKey<?> neoForge) {
        return DynamicTest.dynamicTest(name, () -> {
            assertThat(ours.registry()).isEqualTo(neoForge.registry());
            assertThat(ours.location()).isEqualTo(neoForge.location());
        });
    }
}
