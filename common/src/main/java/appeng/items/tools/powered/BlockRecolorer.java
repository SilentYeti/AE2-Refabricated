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

package appeng.items.tools.powered;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import appeng.api.util.AEColor;

/**
 * Allows recoloring a variety of vanilla blocks.
 */
public final class BlockRecolorer {

    private BlockRecolorer() {
    }

    private static final BiMap<AEColor, Block> STAINED_GLASS_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.STAINED_GLASS.pick(DyeColor.WHITE))
            .put(AEColor.ORANGE, Blocks.STAINED_GLASS.pick(DyeColor.ORANGE))
            .put(AEColor.MAGENTA, Blocks.STAINED_GLASS.pick(DyeColor.MAGENTA))
            .put(AEColor.LIGHT_BLUE, Blocks.STAINED_GLASS.pick(DyeColor.LIGHT_BLUE))
            .put(AEColor.YELLOW, Blocks.STAINED_GLASS.pick(DyeColor.YELLOW))
            .put(AEColor.LIME, Blocks.STAINED_GLASS.pick(DyeColor.LIME))
            .put(AEColor.PINK, Blocks.STAINED_GLASS.pick(DyeColor.PINK))
            .put(AEColor.GRAY, Blocks.STAINED_GLASS.pick(DyeColor.GRAY))
            .put(AEColor.LIGHT_GRAY, Blocks.STAINED_GLASS.pick(DyeColor.LIGHT_GRAY))
            .put(AEColor.CYAN, Blocks.STAINED_GLASS.pick(DyeColor.CYAN))
            .put(AEColor.PURPLE, Blocks.STAINED_GLASS.pick(DyeColor.PURPLE))
            .put(AEColor.BLUE, Blocks.STAINED_GLASS.pick(DyeColor.BLUE))
            .put(AEColor.BROWN, Blocks.STAINED_GLASS.pick(DyeColor.BROWN))
            .put(AEColor.GREEN, Blocks.STAINED_GLASS.pick(DyeColor.GREEN))
            .put(AEColor.RED, Blocks.STAINED_GLASS.pick(DyeColor.RED))
            .put(AEColor.BLACK, Blocks.STAINED_GLASS.pick(DyeColor.BLACK)).build());

    private static final BiMap<AEColor, Block> STAINED_GLASS_PANE_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.STAINED_GLASS_PANE.pick(DyeColor.WHITE))
            .put(AEColor.ORANGE, Blocks.STAINED_GLASS_PANE.pick(DyeColor.ORANGE))
            .put(AEColor.MAGENTA, Blocks.STAINED_GLASS_PANE.pick(DyeColor.MAGENTA))
            .put(AEColor.LIGHT_BLUE, Blocks.STAINED_GLASS_PANE.pick(DyeColor.LIGHT_BLUE))
            .put(AEColor.YELLOW, Blocks.STAINED_GLASS_PANE.pick(DyeColor.YELLOW))
            .put(AEColor.LIME, Blocks.STAINED_GLASS_PANE.pick(DyeColor.LIME))
            .put(AEColor.PINK, Blocks.STAINED_GLASS_PANE.pick(DyeColor.PINK))
            .put(AEColor.GRAY, Blocks.STAINED_GLASS_PANE.pick(DyeColor.GRAY))
            .put(AEColor.LIGHT_GRAY, Blocks.STAINED_GLASS_PANE.pick(DyeColor.LIGHT_GRAY))
            .put(AEColor.CYAN, Blocks.STAINED_GLASS_PANE.pick(DyeColor.CYAN))
            .put(AEColor.PURPLE, Blocks.STAINED_GLASS_PANE.pick(DyeColor.PURPLE))
            .put(AEColor.BLUE, Blocks.STAINED_GLASS_PANE.pick(DyeColor.BLUE))
            .put(AEColor.BROWN, Blocks.STAINED_GLASS_PANE.pick(DyeColor.BROWN))
            .put(AEColor.GREEN, Blocks.STAINED_GLASS_PANE.pick(DyeColor.GREEN))
            .put(AEColor.RED, Blocks.STAINED_GLASS_PANE.pick(DyeColor.RED))
            .put(AEColor.BLACK, Blocks.STAINED_GLASS_PANE.pick(DyeColor.BLACK)).build());

    private static final BiMap<AEColor, Block> WOOL_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WOOL.pick(DyeColor.WHITE))
            .put(AEColor.ORANGE, Blocks.WOOL.pick(DyeColor.ORANGE))
            .put(AEColor.MAGENTA, Blocks.WOOL.pick(DyeColor.MAGENTA))
            .put(AEColor.LIGHT_BLUE, Blocks.WOOL.pick(DyeColor.LIGHT_BLUE))
            .put(AEColor.YELLOW, Blocks.WOOL.pick(DyeColor.YELLOW)).put(AEColor.LIME, Blocks.WOOL.pick(DyeColor.LIME))
            .put(AEColor.PINK, Blocks.WOOL.pick(DyeColor.PINK)).put(AEColor.GRAY, Blocks.WOOL.pick(DyeColor.GRAY))
            .put(AEColor.LIGHT_GRAY, Blocks.WOOL.pick(DyeColor.LIGHT_GRAY))
            .put(AEColor.CYAN, Blocks.WOOL.pick(DyeColor.CYAN))
            .put(AEColor.PURPLE, Blocks.WOOL.pick(DyeColor.PURPLE)).put(AEColor.BLUE, Blocks.WOOL.pick(DyeColor.BLUE))
            .put(AEColor.BROWN, Blocks.WOOL.pick(DyeColor.BROWN)).put(AEColor.GREEN, Blocks.WOOL.pick(DyeColor.GREEN))
            .put(AEColor.RED, Blocks.WOOL.pick(DyeColor.RED)).put(AEColor.BLACK, Blocks.WOOL.pick(DyeColor.BLACK))
            .build());

    private static final BiMap<AEColor, Block> BANNER_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.BANNER.pick(DyeColor.WHITE))
            .put(AEColor.ORANGE, Blocks.BANNER.pick(DyeColor.ORANGE))
            .put(AEColor.MAGENTA, Blocks.BANNER.pick(DyeColor.MAGENTA))
            .put(AEColor.LIGHT_BLUE, Blocks.BANNER.pick(DyeColor.LIGHT_BLUE))
            .put(AEColor.YELLOW, Blocks.BANNER.pick(DyeColor.YELLOW))
            .put(AEColor.LIME, Blocks.BANNER.pick(DyeColor.LIME))
            .put(AEColor.PINK, Blocks.BANNER.pick(DyeColor.PINK)).put(AEColor.GRAY, Blocks.BANNER.pick(DyeColor.GRAY))
            .put(AEColor.LIGHT_GRAY, Blocks.BANNER.pick(DyeColor.LIGHT_GRAY))
            .put(AEColor.CYAN, Blocks.BANNER.pick(DyeColor.CYAN))
            .put(AEColor.PURPLE, Blocks.BANNER.pick(DyeColor.PURPLE))
            .put(AEColor.BLUE, Blocks.BANNER.pick(DyeColor.BLUE))
            .put(AEColor.BROWN, Blocks.BANNER.pick(DyeColor.BROWN))
            .put(AEColor.GREEN, Blocks.BANNER.pick(DyeColor.GREEN))
            .put(AEColor.RED, Blocks.BANNER.pick(DyeColor.RED)).put(AEColor.BLACK, Blocks.BANNER.pick(DyeColor.BLACK))
            .build());

    private static final BiMap<AEColor, Block> WALL_BANNER_BY_COLOR = EnumHashBiMap
            .create(ImmutableMap.<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WALL_BANNER.pick(DyeColor.WHITE))
                    .put(AEColor.ORANGE, Blocks.WALL_BANNER.pick(DyeColor.ORANGE))
                    .put(AEColor.MAGENTA, Blocks.WALL_BANNER.pick(DyeColor.MAGENTA))
                    .put(AEColor.LIGHT_BLUE, Blocks.WALL_BANNER.pick(DyeColor.LIGHT_BLUE))
                    .put(AEColor.YELLOW, Blocks.WALL_BANNER.pick(DyeColor.YELLOW))
                    .put(AEColor.LIME, Blocks.WALL_BANNER.pick(DyeColor.LIME))
                    .put(AEColor.PINK, Blocks.WALL_BANNER.pick(DyeColor.PINK))
                    .put(AEColor.GRAY, Blocks.WALL_BANNER.pick(DyeColor.GRAY))
                    .put(AEColor.LIGHT_GRAY, Blocks.WALL_BANNER.pick(DyeColor.LIGHT_GRAY))
                    .put(AEColor.CYAN, Blocks.WALL_BANNER.pick(DyeColor.CYAN))
                    .put(AEColor.PURPLE, Blocks.WALL_BANNER.pick(DyeColor.PURPLE))
                    .put(AEColor.BLUE, Blocks.WALL_BANNER.pick(DyeColor.BLUE))
                    .put(AEColor.BROWN, Blocks.WALL_BANNER.pick(DyeColor.BROWN))
                    .put(AEColor.GREEN, Blocks.WALL_BANNER.pick(DyeColor.GREEN))
                    .put(AEColor.RED, Blocks.WALL_BANNER.pick(DyeColor.RED))
                    .put(AEColor.BLACK, Blocks.WALL_BANNER.pick(DyeColor.BLACK)).build());

    private static final BiMap<AEColor, Block> CARPET_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.CARPET.pick(DyeColor.WHITE))
            .put(AEColor.ORANGE, Blocks.CARPET.pick(DyeColor.ORANGE))
            .put(AEColor.MAGENTA, Blocks.CARPET.pick(DyeColor.MAGENTA))
            .put(AEColor.LIGHT_BLUE, Blocks.CARPET.pick(DyeColor.LIGHT_BLUE))
            .put(AEColor.YELLOW, Blocks.CARPET.pick(DyeColor.YELLOW))
            .put(AEColor.LIME, Blocks.CARPET.pick(DyeColor.LIME))
            .put(AEColor.PINK, Blocks.CARPET.pick(DyeColor.PINK)).put(AEColor.GRAY, Blocks.CARPET.pick(DyeColor.GRAY))
            .put(AEColor.LIGHT_GRAY, Blocks.CARPET.pick(DyeColor.LIGHT_GRAY))
            .put(AEColor.CYAN, Blocks.CARPET.pick(DyeColor.CYAN))
            .put(AEColor.PURPLE, Blocks.CARPET.pick(DyeColor.PURPLE))
            .put(AEColor.BLUE, Blocks.CARPET.pick(DyeColor.BLUE))
            .put(AEColor.BROWN, Blocks.CARPET.pick(DyeColor.BROWN))
            .put(AEColor.GREEN, Blocks.CARPET.pick(DyeColor.GREEN))
            .put(AEColor.RED, Blocks.CARPET.pick(DyeColor.RED)).put(AEColor.BLACK, Blocks.CARPET.pick(DyeColor.BLACK))
            .build());

    private static final BiMap<AEColor, Block> TERRACOTTA_BY_COLOR = EnumHashBiMap
            .create(ImmutableMap.<AEColor, Block>builder()
                    .put(AEColor.WHITE, Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE))
                    .put(AEColor.ORANGE, Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE))
                    .put(AEColor.MAGENTA, Blocks.DYED_TERRACOTTA.pick(DyeColor.MAGENTA))
                    .put(AEColor.LIGHT_BLUE, Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE))
                    .put(AEColor.YELLOW, Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW))
                    .put(AEColor.LIME, Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME))
                    .put(AEColor.PINK, Blocks.DYED_TERRACOTTA.pick(DyeColor.PINK))
                    .put(AEColor.GRAY, Blocks.DYED_TERRACOTTA.pick(DyeColor.GRAY))
                    .put(AEColor.LIGHT_GRAY, Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY))
                    .put(AEColor.CYAN, Blocks.DYED_TERRACOTTA.pick(DyeColor.CYAN))
                    .put(AEColor.PURPLE, Blocks.DYED_TERRACOTTA.pick(DyeColor.PURPLE))
                    .put(AEColor.BLUE, Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE))
                    .put(AEColor.BROWN, Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN))
                    .put(AEColor.GREEN, Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN))
                    .put(AEColor.RED, Blocks.DYED_TERRACOTTA.pick(DyeColor.RED))
                    .put(AEColor.BLACK, Blocks.DYED_TERRACOTTA.pick(DyeColor.BLACK)).build());

    private static final BiMap<AEColor, Block> GLAZED_TERRACOTTA_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.WHITE))
            .put(AEColor.ORANGE, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.ORANGE))
            .put(AEColor.MAGENTA, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.MAGENTA))
            .put(AEColor.LIGHT_BLUE, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE))
            .put(AEColor.YELLOW, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.YELLOW))
            .put(AEColor.LIME, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.LIME))
            .put(AEColor.PINK, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.PINK))
            .put(AEColor.GRAY, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.GRAY))
            .put(AEColor.LIGHT_GRAY, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY))
            .put(AEColor.CYAN, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.CYAN))
            .put(AEColor.PURPLE, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.PURPLE))
            .put(AEColor.BLUE, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.BLUE))
            .put(AEColor.BROWN, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.BROWN))
            .put(AEColor.GREEN, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.GREEN))
            .put(AEColor.RED, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.RED))
            .put(AEColor.BLACK, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.BLACK)).build());

    private static final BiMap<AEColor, Block> CONCRETE_BY_COLOR = EnumHashBiMap
            .create(ImmutableMap.<AEColor, Block>builder().put(AEColor.WHITE, Blocks.CONCRETE.pick(DyeColor.WHITE))
                    .put(AEColor.ORANGE, Blocks.CONCRETE.pick(DyeColor.ORANGE))
                    .put(AEColor.MAGENTA, Blocks.CONCRETE.pick(DyeColor.MAGENTA))
                    .put(AEColor.LIGHT_BLUE, Blocks.CONCRETE.pick(DyeColor.LIGHT_BLUE))
                    .put(AEColor.YELLOW, Blocks.CONCRETE.pick(DyeColor.YELLOW))
                    .put(AEColor.LIME, Blocks.CONCRETE.pick(DyeColor.LIME))
                    .put(AEColor.PINK, Blocks.CONCRETE.pick(DyeColor.PINK))
                    .put(AEColor.GRAY, Blocks.CONCRETE.pick(DyeColor.GRAY))
                    .put(AEColor.LIGHT_GRAY, Blocks.CONCRETE.pick(DyeColor.LIGHT_GRAY))
                    .put(AEColor.CYAN, Blocks.CONCRETE.pick(DyeColor.CYAN))
                    .put(AEColor.PURPLE, Blocks.CONCRETE.pick(DyeColor.PURPLE))
                    .put(AEColor.BLUE, Blocks.CONCRETE.pick(DyeColor.BLUE))
                    .put(AEColor.BROWN, Blocks.CONCRETE.pick(DyeColor.BROWN))
                    .put(AEColor.GREEN, Blocks.CONCRETE.pick(DyeColor.GREEN))
                    .put(AEColor.RED, Blocks.CONCRETE.pick(DyeColor.RED))
                    .put(AEColor.BLACK, Blocks.CONCRETE.pick(DyeColor.BLACK)).build());

    private static final List<RecolorableBlockGroup> BLOCK_GROUPS = ImmutableList.of(
            new RecolorableBlockGroup(Blocks.GLASS, STAINED_GLASS_BY_COLOR),
            new RecolorableBlockGroup(Blocks.GLASS_PANE, STAINED_GLASS_PANE_BY_COLOR),
            new RecolorableBlockGroup(Blocks.WOOL.pick(DyeColor.WHITE), WOOL_BY_COLOR),
            new RecolorableBlockGroup(Blocks.BANNER.pick(DyeColor.WHITE), BANNER_BY_COLOR),
            new RecolorableBlockGroup(Blocks.WALL_BANNER.pick(DyeColor.WHITE), WALL_BANNER_BY_COLOR),
            new RecolorableBlockGroup(Blocks.CARPET.pick(DyeColor.WHITE), CARPET_BY_COLOR),
            new RecolorableBlockGroup(Blocks.TERRACOTTA, TERRACOTTA_BY_COLOR),
            new RecolorableBlockGroup(null, GLAZED_TERRACOTTA_BY_COLOR),
            new RecolorableBlockGroup(null, CONCRETE_BY_COLOR));

    public static Block recolor(Block block, AEColor newColor) {
        Objects.requireNonNull(block);

        for (RecolorableBlockGroup group : BLOCK_GROUPS) {
            if (group.uncoloredVariant == block || group.coloredVariants.containsValue(block)) {
                Block newBlock = group.coloredVariants.get(newColor);
                if (newBlock == null) {
                    if (group.uncoloredVariant != null) {
                        newBlock = group.uncoloredVariant;
                    } else {
                        newBlock = block;
                    }
                }
                return newBlock;
            }
        }

        return block;
    }

    private static class RecolorableBlockGroup {

        final Block uncoloredVariant;

        final BiMap<AEColor, Block> coloredVariants;

        public RecolorableBlockGroup(Block uncoloredVariant, BiMap<AEColor, Block> coloredVariants) {
            this.uncoloredVariant = uncoloredVariant;
            this.coloredVariants = coloredVariants;
        }

    }

}
