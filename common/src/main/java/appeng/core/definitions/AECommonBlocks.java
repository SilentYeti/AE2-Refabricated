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

package appeng.core.definitions;

import static appeng.block.AEBaseBlock.defaultProps;
import static appeng.block.AEBaseBlock.glassProps;
import static appeng.block.AEBaseBlock.stoneProps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockBehaviour.StateArgumentPredicate;
import net.minecraft.world.level.material.MapColor;

import appeng.api.ids.AEBlockIds;
import appeng.decorative.AEDecorativeBlock;
import appeng.decorative.AEStairBlock;
import appeng.decorative.solid.CertusQuartzClusterBlock;
import appeng.decorative.solid.QuartzGlassBlock;

/**
 * The AE2 blocks whose block classes are loader-agnostic, declared once so a loader that cannot yet run
 * {@link AEBlocks} can still register them. The block counterpart of {@link AECommonItems}, and the same bargain:
 * {@link AEBlocks} stays the authority on NeoForge, this table is what {@code :fabric} registers, and
 * {@code AECommonBlocksTest} pins the two together so they cannot drift.
 * <p>
 * A block belongs here once its class compiles in {@code :common}. Half of AE2's blocks already do, because the
 * decorative family is either vanilla {@code StairBlock}/{@code SlabBlock}/{@code WallBlock} or
 * {@link AEDecorativeBlock}, none of which touch the grid. Everything that needs a block entity does not, and is listed
 * in {@link #notYetPortable()} with the class that holds it back.
 */
public final class AECommonBlocks {
    private AECommonBlocks() {
    }

    /**
     * @param englishName The name used for the generated en_us translation, matching {@link AEBlocks}.
     * @param id          The registry id, shared by the block and its {@code BlockItem}.
     * @param factory     Builds the block. {@code resolve} looks up a block registered earlier in this same table,
     *                    which the stair, slab and wall variants need to copy their base block state.
     */
    public record Entry<T extends Block>(String englishName,
            Identifier id,
            BiFunction<Properties, BlockResolver, T> factory) {
    }

    /** Resolves a block declared earlier in this table. */
    @FunctionalInterface
    public interface BlockResolver {
        Block get(Identifier id);
    }

    /** Matches AEBlocks: nothing spawns on AE2's glass. */
    private static final StateArgumentPredicate<EntityType<?>> NEVER_ALLOW_SPAWN = (state, level, pos, type) -> false;

    private static final List<Entry<?>> ENTRIES = new ArrayList<>();

    public static List<Entry<?>> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    // spotless:off

    static {
        block("Small Certus Quartz Bud", AEBlockIds.SMALL_QUARTZ_BUD, (p, resolve) -> new CertusQuartzClusterBlock(3, 4, quartzClusterProperties(p).sound(SoundType.SMALL_AMETHYST_BUD).lightLevel(s -> 1)));
        block("Medium Certus Quartz Bud", AEBlockIds.MEDIUM_QUARTZ_BUD, (p, resolve) -> new CertusQuartzClusterBlock(4, 3, quartzClusterProperties(p).sound(SoundType.MEDIUM_AMETHYST_BUD).lightLevel(s -> 2)));
        block("Large Certus Quartz Bud", AEBlockIds.LARGE_QUARTZ_BUD, (p, resolve) -> new CertusQuartzClusterBlock(5, 3, quartzClusterProperties(p).sound(SoundType.LARGE_AMETHYST_BUD).lightLevel(s -> 4)));
        block("Certus Quartz Cluster", AEBlockIds.QUARTZ_CLUSTER, (p, resolve) -> new CertusQuartzClusterBlock(7, 3, quartzClusterProperties(p).sound(SoundType.AMETHYST_CLUSTER).lightLevel(s -> 5)));
        block("Certus Quartz Block", AEBlockIds.QUARTZ_BLOCK, (p, resolve) -> new AEDecorativeBlock(quartzProperties(p)));
        block("Cut Certus Quartz Block", AEBlockIds.CUT_QUARTZ_BLOCK, (p, resolve) -> new AEDecorativeBlock(quartzProperties(p)));
        block("Smooth Certus Quartz Block", AEBlockIds.SMOOTH_QUARTZ_BLOCK, (p, resolve) -> new AEDecorativeBlock(quartzProperties(p)));
        block("Certus Quartz Bricks", AEBlockIds.QUARTZ_BRICKS, (p, resolve) -> new AEDecorativeBlock(quartzProperties(p)));
        block("Certus Quartz Pillar", AEBlockIds.QUARTZ_PILLAR, (p, resolve) -> new RotatedPillarBlock(quartzProperties(p)));
        block("Chiseled Certus Quartz Block", AEBlockIds.CHISELED_QUARTZ_BLOCK, (p, resolve) -> new AEDecorativeBlock(quartzProperties(p)));
        block("Quartz Glass", AEBlockIds.QUARTZ_GLASS, (p, resolve) -> new QuartzGlassBlock(glassProps(p).noOcclusion().isValidSpawn(NEVER_ALLOW_SPAWN)));
        block("Fluix Block", AEBlockIds.FLUIX_BLOCK, (p, resolve) -> new AEDecorativeBlock(fluixProperties(p)));
        block("Sky Stone", AEBlockIds.SKY_STONE_BLOCK, (p, resolve) -> new AEDecorativeBlock(stoneProps(p).strength(50, 150).requiresCorrectToolForDrops()));
        block("Sky Stone Block", AEBlockIds.SMOOTH_SKY_STONE_BLOCK, (p, resolve) -> new AEDecorativeBlock(skystoneProperties(p)));
        block("Sky Stone Brick", AEBlockIds.SKY_STONE_BRICK, (p, resolve) -> new AEDecorativeBlock(skystoneProperties(p)));
        block("Sky Stone Small Brick", AEBlockIds.SKY_STONE_SMALL_BRICK, (p, resolve) -> new AEDecorativeBlock(skystoneProperties(p)));
        block("Sky Stone Stairs", AEBlockIds.SKY_STONE_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.SKY_STONE_BLOCK).defaultBlockState(), skystoneProperties(p)));
        block("Sky Stone Block Stairs", AEBlockIds.SMOOTH_SKY_STONE_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.SMOOTH_SKY_STONE_BLOCK).defaultBlockState(), skystoneProperties(p)));
        block("Sky Stone Brick Stairs", AEBlockIds.SKY_STONE_BRICK_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.SKY_STONE_BRICK).defaultBlockState(), skystoneProperties(p)));
        block("Sky Stone Small Brick Stairs", AEBlockIds.SKY_STONE_SMALL_BRICK_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.SKY_STONE_SMALL_BRICK).defaultBlockState(), skystoneProperties(p)));
        block("Fluix Stairs", AEBlockIds.FLUIX_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.FLUIX_BLOCK).defaultBlockState(), fluixProperties(p)));
        block("Certus Quartz Stairs", AEBlockIds.QUARTZ_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.QUARTZ_BLOCK).defaultBlockState(), quartzProperties(p)));
        block("Cut Certus Quartz Stairs", AEBlockIds.CUT_QUARTZ_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.CUT_QUARTZ_BLOCK).defaultBlockState(), quartzProperties(p)));
        block("Smooth Certus Quartz Stairs", AEBlockIds.SMOOTH_QUARTZ_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.SMOOTH_QUARTZ_BLOCK).defaultBlockState(), quartzProperties(p)));
        block("Certus Quartz Brick Stairs", AEBlockIds.QUARTZ_BRICK_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.QUARTZ_BRICKS).defaultBlockState(), quartzProperties(p)));
        block("Chiseled Certus Quartz Stairs", AEBlockIds.CHISELED_QUARTZ_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.CHISELED_QUARTZ_BLOCK).defaultBlockState(), quartzProperties(p)));
        block("Certus Quartz Pillar Stairs", AEBlockIds.QUARTZ_PILLAR_STAIRS, (p, resolve) -> new AEStairBlock(resolve.get(AEBlockIds.QUARTZ_PILLAR).defaultBlockState(), quartzProperties(p)));
        block("Sky Stone Wall", AEBlockIds.SKY_STONE_WALL, (p, resolve) -> new WallBlock(skystoneProperties(p)));
        block("Sky Stone Block Wall", AEBlockIds.SMOOTH_SKY_STONE_WALL, (p, resolve) -> new WallBlock(skystoneProperties(p)));
        block("Sky Stone Brick Wall", AEBlockIds.SKY_STONE_BRICK_WALL, (p, resolve) -> new WallBlock(skystoneProperties(p)));
        block("Sky Stone Small Brick Wall", AEBlockIds.SKY_STONE_SMALL_BRICK_WALL, (p, resolve) -> new WallBlock(skystoneProperties(p)));
        block("Fluix Wall", AEBlockIds.FLUIX_WALL, (p, resolve) -> new WallBlock(fluixProperties(p)));
        block("Certus Quartz Wall", AEBlockIds.QUARTZ_WALL, (p, resolve) -> new WallBlock(quartzProperties(p)));
        block("Cut Certus Quartz Wall", AEBlockIds.CUT_QUARTZ_WALL, (p, resolve) -> new WallBlock(quartzProperties(p)));
        block("Smooth Certus Quartz Wall", AEBlockIds.SMOOTH_QUARTZ_WALL, (p, resolve) -> new WallBlock(quartzProperties(p)));
        block("Certus Quartz Brick Wall", AEBlockIds.QUARTZ_BRICK_WALL, (p, resolve) -> new WallBlock(quartzProperties(p)));
        block("Chiseled Certus Quartz Wall", AEBlockIds.CHISELED_QUARTZ_WALL, (p, resolve) -> new WallBlock(quartzProperties(p)));
        block("Certus Quartz Pillar Wall", AEBlockIds.QUARTZ_PILLAR_WALL, (p, resolve) -> new WallBlock(quartzProperties(p)));
        block("Sky Stone Slab", AEBlockIds.SKY_STONE_SLAB, (p, resolve) -> new SlabBlock(skystoneProperties(p)));
        block("Sky Stone Block Slab", AEBlockIds.SMOOTH_SKY_STONE_SLAB, (p, resolve) -> new SlabBlock(skystoneProperties(p)));
        block("Sky Stone Brick Slab", AEBlockIds.SKY_STONE_BRICK_SLAB, (p, resolve) -> new SlabBlock(skystoneProperties(p)));
        block("Sky Stone Small Brick Slab", AEBlockIds.SKY_STONE_SMALL_BRICK_SLAB, (p, resolve) -> new SlabBlock(skystoneProperties(p)));
        block("Fluix Slab", AEBlockIds.FLUIX_SLAB, (p, resolve) -> new SlabBlock(fluixProperties(p)));
        block("Certus Quartz Slab", AEBlockIds.QUARTZ_SLAB, (p, resolve) -> new SlabBlock(quartzProperties(p)));
        block("Cut Certus Quartz Slab", AEBlockIds.CUT_QUARTZ_SLAB, (p, resolve) -> new SlabBlock(quartzProperties(p)));
        block("Smooth Certus Quartz Slab", AEBlockIds.SMOOTH_QUARTZ_SLAB, (p, resolve) -> new SlabBlock(quartzProperties(p)));
        block("Certus Quartz Brick Slab", AEBlockIds.QUARTZ_BRICK_SLAB, (p, resolve) -> new SlabBlock(quartzProperties(p)));
        block("Chiseled Certus Quartz Slab", AEBlockIds.CHISELED_QUARTZ_SLAB, (p, resolve) -> new SlabBlock(quartzProperties(p)));
        block("Certus Quartz Pillar Slab", AEBlockIds.QUARTZ_PILLAR_SLAB, (p, resolve) -> new SlabBlock(quartzProperties(p)));
    }

    // spotless:on

    private static Properties quartzProperties(Properties p) {
        return stoneProps(p).mapColor(MapColor.COLOR_CYAN)
                .strength(3, 5).requiresCorrectToolForDrops();
    }

    private static Properties skystoneProperties(Properties p) {
        return stoneProps(p).mapColor(MapColor.COLOR_BLACK)
                .strength(5, 150).requiresCorrectToolForDrops();
    }

    private static Properties fluixProperties(Properties p) {
        return stoneProps(p).mapColor(MapColor.COLOR_PURPLE)
                .strength(3, 5).requiresCorrectToolForDrops();
    }

    private static Properties quartzClusterProperties(Properties p) {
        return defaultProps(p, MapColor.COLOR_CYAN,
                SoundType.AMETHYST_CLUSTER).forceSolidOn().strength(1.5f).requiresCorrectToolForDrops();
    }

    /**
     * Why each remaining {@link AEBlocks} entry is not in the table yet, keyed by the {@code AEBlocks} field name. Read
     * by {@code AECommonBlocksTest}, which fails if a block is in neither this map nor {@link #entries()} -- so a newly
     * added block cannot be silently forgotten on Fabric.
     */
    public static Map<String, String> notYetPortable() {
        return NOT_YET_PORTABLE;
    }

    private static final Map<String, String> NOT_YET_PORTABLE = Map.ofEntries(
            Map.entry("QUARTZ_VIBRANT_GLASS", "needs QuartzLampBlock, which needs AEConfig and AE2's particle types"),
            Map.entry("CRAFTING_ACCELERATOR", "needs CraftingUnitBlock, which needs a block entity"),
            Map.entry("CRAFTING_STORAGE_1K", "needs CraftingUnitBlock, which needs a block entity"),
            Map.entry("CRAFTING_STORAGE_4K", "needs CraftingUnitBlock, which needs a block entity"),
            Map.entry("CRAFTING_STORAGE_16K", "needs CraftingUnitBlock, which needs a block entity"),
            Map.entry("CRAFTING_STORAGE_64K", "needs CraftingUnitBlock, which needs a block entity"),
            Map.entry("CRAFTING_STORAGE_256K", "needs CraftingUnitBlock, which needs a block entity"),
            Map.entry("CRAFTING_MONITOR", "needs CraftingMonitorBlock, which needs a block entity"),
            Map.entry("FLAWLESS_BUDDING_QUARTZ", "needs BuddingCertusQuartzBlock"),
            Map.entry("FLAWED_BUDDING_QUARTZ", "needs BuddingCertusQuartzBlock"),
            Map.entry("CHIPPED_BUDDING_QUARTZ", "needs BuddingCertusQuartzBlock"),
            Map.entry("DAMAGED_BUDDING_QUARTZ", "needs BuddingCertusQuartzBlock"),
            Map.entry("MATRIX_FRAME", "needs MatrixFrameBlock"),
            Map.entry("QUARTZ_FIXTURE", "needs QuartzFixtureBlock"),
            Map.entry("SKY_STONE_CHEST", "needs SkyStoneChestBlock"),
            Map.entry("SMOOTH_SKY_STONE_CHEST", "needs SkyStoneChestBlock"),
            Map.entry("SKY_STONE_TANK", "needs SkyStoneTankBlock"),
            Map.entry("MYSTERIOUS_CUBE", "needs MysteriousCubeBlock"),
            Map.entry("NOT_SO_MYSTERIOUS_CUBE", "needs NotSoMysteriousCubeBlock"),
            Map.entry("INSCRIBER", "needs InscriberBlock"),
            Map.entry("WIRELESS_ACCESS_POINT", "needs WirelessAccessPointBlock"),
            Map.entry("CHARGER", "needs ChargerBlock"),
            Map.entry("TINY_TNT", "needs TinyTNTBlock"),
            Map.entry("QUANTUM_RING", "needs QuantumRingBlock"),
            Map.entry("QUANTUM_LINK", "needs QuantumLinkChamberBlock"),
            Map.entry("SPATIAL_PYLON", "needs SpatialPylonBlock"),
            Map.entry("SPATIAL_IO_PORT", "needs SpatialIOPortBlock"),
            Map.entry("CONTROLLER", "needs ControllerBlock"),
            Map.entry("DRIVE", "needs DriveBlock"),
            Map.entry("ME_CHEST", "needs MEChestBlock"),
            Map.entry("INTERFACE", "needs InterfaceBlock"),
            Map.entry("CELL_WORKBENCH", "needs CellWorkbenchBlock"),
            Map.entry("IO_PORT", "needs IOPortBlock"),
            Map.entry("CONDENSER", "needs CondenserBlock"),
            Map.entry("ENERGY_ACCEPTOR", "needs EnergyAcceptorBlock"),
            Map.entry("CRYSTAL_RESONANCE_GENERATOR", "needs CrystalResonanceGeneratorBlock"),
            Map.entry("VIBRATION_CHAMBER", "needs VibrationChamberBlock"),
            Map.entry("GROWTH_ACCELERATOR", "needs GrowthAcceleratorBlock"),
            Map.entry("ENERGY_CELL", "needs EnergyCellBlock"),
            Map.entry("DENSE_ENERGY_CELL", "needs EnergyCellBlock"),
            Map.entry("CREATIVE_ENERGY_CELL", "needs CreativeEnergyCellBlock"),
            Map.entry("CRAFTING_UNIT", "needs CraftingUnitBlock"),
            Map.entry("PATTERN_PROVIDER", "needs PatternProviderBlock"),
            Map.entry("MOLECULAR_ASSEMBLER", "needs MolecularAssemblerBlock"),
            Map.entry("LIGHT_DETECTOR", "needs LightDetectorBlock"),
            Map.entry("PAINT", "needs PaintSplotchesBlock"),
            Map.entry("CABLE_BUS", "needs CableBusBlock"),
            Map.entry("SPATIAL_ANCHOR", "needs SpatialAnchorBlock"),
            Map.entry("DEBUG_ITEM_GEN", "needs ItemGenBlock"),
            Map.entry("DEBUG_PHANTOM_NODE", "needs PhantomNodeBlock"),
            Map.entry("DEBUG_CUBE_GEN", "needs CubeGeneratorBlock"),
            Map.entry("DEBUG_ENERGY_GEN", "needs EnergyGeneratorBlock"),
            Map.entry("CRANK", "needs CrankBlock"));

    private static <T extends Block> void block(String englishName, Identifier id,
            BiFunction<Properties, BlockResolver, T> factory) {
        ENTRIES.add(new Entry<>(englishName, id, factory));
    }
}
