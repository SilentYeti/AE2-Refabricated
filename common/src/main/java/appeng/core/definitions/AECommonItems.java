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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import appeng.api.ids.AEItemIds;
import appeng.api.util.AEColor;
import appeng.core.ConventionTags;
import appeng.items.materials.MaterialItem;
import appeng.items.materials.NamePressItem;
import appeng.items.materials.StorageComponentItem;
import appeng.items.misc.MissingContentItem;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.fluix.FluixAxeItem;
import appeng.items.tools.fluix.FluixHoeItem;
import appeng.items.tools.fluix.FluixPickaxeItem;
import appeng.items.tools.fluix.FluixSmithingTemplateItem;
import appeng.items.tools.fluix.FluixSpadeItem;
import appeng.items.tools.fluix.FluixSwordItem;
import appeng.items.tools.quartz.QuartzAxeItem;
import appeng.items.tools.quartz.QuartzHoeItem;
import appeng.items.tools.quartz.QuartzSpadeItem;
import appeng.items.tools.quartz.QuartzWrenchItem;

/**
 * The AE2 items whose item classes are loader-agnostic, declared once so a loader that cannot yet run {@link AEItems}
 * can still register them.
 * <p>
 * {@link AEItems} remains the authority on NeoForge and is unchanged: it owns the typed {@code ItemDefinition} fields
 * the rest of the codebase references, and the order of its declarations is the order of AE2's creative tab. It cannot
 * move to {@code :common} while {@code ItemDefinition -> AEItemKey/GenericStack -> AEComponents -> AEItems} keeps every
 * item, block, part and menu in one cycle. Until that cycle is cut, this table is what {@code :fabric} registers, and
 * {@code AECommonItemsTest} pins every entry against {@link AEItems} so the two cannot drift.
 * <p>
 * An item belongs here once its class compiles in {@code :common}. Anything needing the grid, a menu, stored energy,
 * data components or a storage cell does not, and is listed in {@link #notYetPortable()} with the reason.
 */
public final class AECommonItems {
    private AECommonItems() {
    }

    /**
     * The vanilla tabs AE2 also puts some of its items into. Vanilla's own constants for them are {@code private} and
     * only widened by NeoForge, so they cannot be named from {@code :common}; {@code AECommonItemsTest} pins these keys
     * against them.
     */
    public static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = vanillaTab("tools_and_utilities");
    public static final ResourceKey<CreativeModeTab> COMBAT = vanillaTab("combat");
    public static final ResourceKey<CreativeModeTab> INGREDIENTS = vanillaTab("ingredients");

    private static ResourceKey<CreativeModeTab> vanillaTab(String id) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace(id));
    }

    /**
     * @param englishName The name used for the generated en_us translation, matching {@link AEItems}.
     * @param id          The registry id.
     * @param factory     Builds the item. The caller has already applied {@code setId}.
     * @param externalTab A vanilla creative tab this item additionally appears in, or null for AE2's tab only.
     */
    public record Entry<T extends Item>(String englishName,
            Identifier id,
            Function<Item.Properties, T> factory,
            @Nullable ResourceKey<CreativeModeTab> externalTab) {
    }

    private static final List<Entry<?>> ENTRIES = new ArrayList<>();

    // spotless:off

    ///
    /// CERTUS QUARTZ TOOLS
    ///
    static {
        item("Certus Quartz Axe", AEItemIds.CERTUS_QUARTZ_AXE, p -> new QuartzAxeItem(p.axe(ToolMaterial.IRON, 6.0F, -3.1F).repairable(ConventionTags.CERTUS_QUARTZ)), TOOLS_AND_UTILITIES);
        item("Certus Quartz Hoe", AEItemIds.CERTUS_QUARTZ_HOE, p -> new QuartzHoeItem(p.hoe(ToolMaterial.IRON, -2, -1.0F).repairable(ConventionTags.CERTUS_QUARTZ)), TOOLS_AND_UTILITIES);
        item("Certus Quartz Shovel", AEItemIds.CERTUS_QUARTZ_SHOVEL, p -> new QuartzSpadeItem(p.shovel(ToolMaterial.IRON, 1.5F, -3.0F).repairable(ConventionTags.CERTUS_QUARTZ)), TOOLS_AND_UTILITIES);
        standardItem("Certus Quartz Pickaxe", AEItemIds.CERTUS_QUARTZ_PICK, p -> p.pickaxe(ToolMaterial.IRON, 1, -2.8F).repairable(ConventionTags.CERTUS_QUARTZ), TOOLS_AND_UTILITIES);
        standardItem("Certus Quartz Sword", AEItemIds.CERTUS_QUARTZ_SWORD, p -> p.sword(ToolMaterial.IRON, 3, -2.4F).repairable(ConventionTags.CERTUS_QUARTZ), COMBAT);
        item("Certus Quartz Wrench", AEItemIds.CERTUS_QUARTZ_WRENCH, p -> new QuartzWrenchItem(p.stacksTo(1)), TOOLS_AND_UTILITIES);

        ///
        /// NETHER QUARTZ TOOLS
        ///
        item("Nether Quartz Axe", AEItemIds.NETHER_QUARTZ_AXE, p -> new QuartzAxeItem(p.axe(ToolMaterial.IRON, 6.0F, -3.1F).repairable(ConventionTags.NETHER_QUARTZ)), TOOLS_AND_UTILITIES);
        item("Nether Quartz Hoe", AEItemIds.NETHER_QUARTZ_HOE, p -> new QuartzHoeItem(p.hoe(ToolMaterial.IRON, -2, -1.0F).repairable(ConventionTags.NETHER_QUARTZ)), TOOLS_AND_UTILITIES);
        item("Nether Quartz Shovel", AEItemIds.NETHER_QUARTZ_SHOVEL, p -> new QuartzSpadeItem(p.shovel(ToolMaterial.IRON, 1.5F, -3.0F).repairable(ConventionTags.NETHER_QUARTZ)), TOOLS_AND_UTILITIES);
        standardItem("Nether Quartz Pickaxe", AEItemIds.NETHER_QUARTZ_PICK, p -> p.pickaxe(ToolMaterial.IRON, 1, -2.8F).repairable(ConventionTags.NETHER_QUARTZ), TOOLS_AND_UTILITIES);
        standardItem("Nether Quartz Sword", AEItemIds.NETHER_QUARTZ_SWORD, p -> p.sword(ToolMaterial.IRON, 3, -2.4F).repairable(ConventionTags.NETHER_QUARTZ), COMBAT);
        item("Nether Quartz Wrench", AEItemIds.NETHER_QUARTZ_WRENCH, p -> new QuartzWrenchItem(p.stacksTo(1)), TOOLS_AND_UTILITIES);

        ///
        /// FLUIX TOOLS
        ///
        item("Fluix Upgrade", AEItemIds.FLUIX_UPGRADE_SMITHING_TEMPLATE, FluixSmithingTemplateItem::new, INGREDIENTS);
        item("Fluix Axe", AEItemIds.FLUIX_AXE, FluixAxeItem::new, TOOLS_AND_UTILITIES);
        item("Fluix Hoe", AEItemIds.FLUIX_HOE, FluixHoeItem::new, TOOLS_AND_UTILITIES);
        item("Fluix Shovel", AEItemIds.FLUIX_SHOVEL, FluixSpadeItem::new, TOOLS_AND_UTILITIES);
        item("Fluix Pickaxe", AEItemIds.FLUIX_PICK, FluixPickaxeItem::new, TOOLS_AND_UTILITIES);
        item("Fluix Sword", AEItemIds.FLUIX_SWORD, FluixSwordItem::new, COMBAT);

        ///
        /// MISC
        ///
        item("Blank Pattern", AEItemIds.BLANK_PATTERN, MaterialItem::new);
        // Used to represent missing content if a mod got uninstalled
        item("Missing Content", AEItemIds.MISSING_CONTENT, MissingContentItem::new);

        coloredItems("Paint Ball", AEItemIds.COLORED_PAINT_BALL, (p, color) -> new PaintBallItem(p, color, false));
        coloredItems("Lumen Paint Ball", AEItemIds.COLORED_LUMEN_PAINT_BALL, (p, color) -> new PaintBallItem(p, color, true));

        ///
        /// MATERIALS
        ///
        item("Certus Quartz Crystal", AEItemIds.CERTUS_QUARTZ_CRYSTAL, MaterialItem::new);
        item("Charged Certus Quartz Crystal", AEItemIds.CERTUS_QUARTZ_CRYSTAL_CHARGED, MaterialItem::new);
        item("Certus Quartz Dust", AEItemIds.CERTUS_QUARTZ_DUST, MaterialItem::new);
        item("Silicon", AEItemIds.SILICON, MaterialItem::new);
        item("Matter Ball", AEItemIds.MATTER_BALL, MaterialItem::new);
        item("Fluix Crystal", AEItemIds.FLUIX_CRYSTAL, MaterialItem::new);
        item("Fluix Dust", AEItemIds.FLUIX_DUST, MaterialItem::new);
        item("Fluix Pearl", AEItemIds.FLUIX_PEARL, MaterialItem::new);
        item("Inscriber Calculation Press", AEItemIds.CALCULATION_PROCESSOR_PRESS, MaterialItem::new);
        item("Inscriber Engineering Press", AEItemIds.ENGINEERING_PROCESSOR_PRESS, MaterialItem::new);
        item("Inscriber Logic Press", AEItemIds.LOGIC_PROCESSOR_PRESS, MaterialItem::new);
        item("Printed Calculation Circuit", AEItemIds.CALCULATION_PROCESSOR_PRINT, MaterialItem::new);
        item("Printed Engineering Circuit", AEItemIds.ENGINEERING_PROCESSOR_PRINT, MaterialItem::new);
        item("Printed Logic Circuit", AEItemIds.LOGIC_PROCESSOR_PRINT, MaterialItem::new);
        item("Inscriber Silicon Press", AEItemIds.SILICON_PRESS, MaterialItem::new);
        item("Printed Silicon", AEItemIds.SILICON_PRINT, MaterialItem::new);
        item("Inscriber Name Press", AEItemIds.NAME_PRESS, NamePressItem::new);
        item("Logic Processor", AEItemIds.LOGIC_PROCESSOR, MaterialItem::new);
        item("Calculation Processor", AEItemIds.CALCULATION_PROCESSOR, MaterialItem::new);
        item("Engineering Processor", AEItemIds.ENGINEERING_PROCESSOR, MaterialItem::new);
        item("Basic Card", AEItemIds.BASIC_CARD, MaterialItem::new);
        item("Advanced Card", AEItemIds.ADVANCED_CARD, MaterialItem::new);
        item("2³ Spatial Component", AEItemIds.SPATIAL_2_CELL_COMPONENT, MaterialItem::new);
        item("16³ Spatial Component", AEItemIds.SPATIAL_16_CELL_COMPONENT, MaterialItem::new);
        item("128³ Spatial Component", AEItemIds.SPATIAL_128_CELL_COMPONENT, MaterialItem::new);
        item("1k ME Storage Component", AEItemIds.CELL_COMPONENT_1K, p -> new StorageComponentItem(p, 1));
        item("4k ME Storage Component", AEItemIds.CELL_COMPONENT_4K, p -> new StorageComponentItem(p, 4));
        item("16k ME Storage Component", AEItemIds.CELL_COMPONENT_16K, p -> new StorageComponentItem(p, 16));
        item("64k ME Storage Component", AEItemIds.CELL_COMPONENT_64K, p -> new StorageComponentItem(p, 64));
        item("256k ME Storage Component", AEItemIds.CELL_COMPONENT_256K, p -> new StorageComponentItem(p, 256));
        item("ME Item Cell Housing", AEItemIds.ITEM_CELL_HOUSING, MaterialItem::new);
        item("ME Fluid Cell Housing", AEItemIds.FLUID_CELL_HOUSING, MaterialItem::new);
        item("Wireless Receiver", AEItemIds.WIRELESS_RECEIVER, MaterialItem::new);
        item("Wireless Booster", AEItemIds.WIRELESS_BOOSTER, MaterialItem::new);
        item("Formation Core", AEItemIds.FORMATION_CORE, MaterialItem::new);
        item("Annihilation Core", AEItemIds.ANNIHILATION_CORE, MaterialItem::new);
        item("Sky Stone Dust", AEItemIds.SKY_DUST, MaterialItem::new);
        item("Ender Dust", AEItemIds.ENDER_DUST, MaterialItem::new);
        item("Singularity", AEItemIds.SINGULARITY, MaterialItem::new);
        item("Quantum Entangled Singularity", AEItemIds.QUANTUM_ENTANGLED_SINGULARITY, MaterialItem::new);
    }

    // spotless:on

    /**
     * Why each remaining {@link AEItems} entry is not in the table yet, keyed by the {@code AEItems} field name. Read
     * by {@code AECommonItemsTest}, which fails if an item is in neither this map nor {@link #entries()} -- so a newly
     * added item cannot be silently forgotten on Fabric.
     */
    public static Map<String, String> notYetPortable() {
        return Map.ofEntries(
                Map.entry("CRAFTING_PATTERN", "PatternDetailsHelper: encoded patterns need the crafting API"),
                Map.entry("PROCESSING_PATTERN", "PatternDetailsHelper: encoded patterns need the crafting API"),
                Map.entry("SMITHING_TABLE_PATTERN", "PatternDetailsHelper: encoded patterns need the crafting API"),
                Map.entry("STONECUTTING_PATTERN", "PatternDetailsHelper: encoded patterns need the crafting API"),
                Map.entry("METEORITE_COMPASS",
                        "the ae2:meteorite_compass item model type: MeteoriteCompassModel needs NeoForge's "
                                + "quad transform API and the client CompassManager, and the vanilla item "
                                + "model registry is private so Fabric cannot register the type"),
                Map.entry("WRAPPED_GENERIC_STACK", "WrappedGenericStack: needs AEKey and ContainerItemStrategies"),
                Map.entry("REDSTONE_CARD", "Upgrades.createUpgradeCardItem -> UpgradeCardItem -> IPartHost"),
                Map.entry("CAPACITY_CARD", "Upgrades.createUpgradeCardItem -> UpgradeCardItem -> IPartHost"),
                Map.entry("VOID_CARD", "Upgrades.createUpgradeCardItem -> UpgradeCardItem -> IPartHost"),
                Map.entry("FUZZY_CARD", "Upgrades.createUpgradeCardItem -> UpgradeCardItem -> IPartHost"),
                Map.entry("SPEED_CARD", "Upgrades.createUpgradeCardItem -> UpgradeCardItem -> IPartHost"),
                Map.entry("INVERTER_CARD", "Upgrades.createUpgradeCardItem -> UpgradeCardItem -> IPartHost"),
                Map.entry("CRAFTING_CARD", "Upgrades.createUpgradeCardItem -> UpgradeCardItem -> IPartHost"),
                Map.entry("EQUAL_DISTRIBUTION_CARD", "Upgrades.createUpgradeCardItem -> UpgradeCardItem -> IPartHost"),
                Map.entry("ENERGY_CARD", "EnergyCardItem extends UpgradeCardItem -> IPartHost"),
                Map.entry("GUIDE", "GuideItem: needs guideme.GuidesCommon"),
                Map.entry("FACADE", "FacadeItem: needs the parts API"),
                Map.entry("MEMORY_CARD", "MemoryCardItem: needs AEComponents and the parts API"),
                Map.entry("NETWORK_TOOL", "NetworkToolItem: opens a menu"),
                Map.entry("CERTUS_QUARTZ_KNIFE", "QuartzCuttingKnifeItem: opens a menu"),
                Map.entry("NETHER_QUARTZ_KNIFE", "QuartzCuttingKnifeItem: opens a menu"),
                Map.entry("VIEW_CELL", "ViewCellItem: needs the storage API"),
                Map.entry("CREATIVE_CELL", "CreativeCellItem: needs the storage API"),
                Map.entry("ITEM_CELL_1K", "BasicStorageCell: needs the storage API"),
                Map.entry("ITEM_CELL_4K", "BasicStorageCell: needs the storage API"),
                Map.entry("ITEM_CELL_16K", "BasicStorageCell: needs the storage API"),
                Map.entry("ITEM_CELL_64K", "BasicStorageCell: needs the storage API"),
                Map.entry("ITEM_CELL_256K", "BasicStorageCell: needs the storage API"),
                Map.entry("FLUID_CELL_1K", "BasicStorageCell: needs the storage API"),
                Map.entry("FLUID_CELL_4K", "BasicStorageCell: needs the storage API"),
                Map.entry("FLUID_CELL_16K", "BasicStorageCell: needs the storage API"),
                Map.entry("FLUID_CELL_64K", "BasicStorageCell: needs the storage API"),
                Map.entry("FLUID_CELL_256K", "BasicStorageCell: needs the storage API"),
                Map.entry("SPATIAL_CELL2", "SpatialStorageCellItem: needs the spatial storage service"),
                Map.entry("SPATIAL_CELL16", "SpatialStorageCellItem: needs the spatial storage service"),
                Map.entry("SPATIAL_CELL128", "SpatialStorageCellItem: needs the spatial storage service"),
                Map.entry("CHARGED_STAFF", "ChargedStaffItem crossed, but nothing on Fabric can charge it -- AE2's "
                        + "charger is a block entity, other mods' chargers need the energy interop -- and its "
                        + "lightning particles need AERenderTypes (stage 7)"),
                Map.entry("ENTROPY_MANIPULATOR", "EntropyManipulatorItem: TinyTNTBlock, AERecipeTypes, "
                        + "InteractionUtil and Platform, and NeoForge's Block.onCaughtFire"),
                Map.entry("MATTER_CANNON", "MatterCannonItem: the storage API, upgrades, a packet and NeoForge's "
                        + "block-break event"),
                Map.entry("COLOR_APPLICATOR", "ColorApplicatorItem: the storage API, upgrades and the cable bus"),
                Map.entry("WIRELESS_TERMINAL", "WirelessTerminalItem: the grid, upgrades and a menu"),
                Map.entry("WIRELESS_CRAFTING_TERMINAL", "WirelessTerminalItem: the grid, upgrades and a menu"),
                Map.entry("PORTABLE_ITEM_CELL1K", "PortableCellItem: needs the energy and storage APIs"),
                Map.entry("PORTABLE_ITEM_CELL4K", "PortableCellItem: needs the energy and storage APIs"),
                Map.entry("PORTABLE_ITEM_CELL16K", "PortableCellItem: needs the energy and storage APIs"),
                Map.entry("PORTABLE_ITEM_CELL64K", "PortableCellItem: needs the energy and storage APIs"),
                Map.entry("PORTABLE_ITEM_CELL256K", "PortableCellItem: needs the energy and storage APIs"),
                Map.entry("PORTABLE_FLUID_CELL1K", "PortableCellItem: needs the energy and storage APIs"),
                Map.entry("PORTABLE_FLUID_CELL4K", "PortableCellItem: needs the energy and storage APIs"),
                Map.entry("PORTABLE_FLUID_CELL16K", "PortableCellItem: needs the energy and storage APIs"),
                Map.entry("PORTABLE_FLUID_CELL64K", "PortableCellItem: needs the energy and storage APIs"),
                Map.entry("PORTABLE_FLUID_CELL256K", "PortableCellItem: needs the energy and storage APIs"),
                Map.entry("DEBUG_ERASER", "debug tool: needs AEConfig"),
                Map.entry("DEBUG_METEORITE_PLACER", "debug tool: needs AEConfig and worldgen"),
                Map.entry("DEBUG_CARD", "debug tool: needs the grid"),
                Map.entry("DEBUG_REPLICATOR_CARD", "debug tool: needs the grid"));
    }

    public static List<Entry<?>> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    private static <T extends Item> void item(String name, Identifier id, Function<Item.Properties, T> factory) {
        item(name, id, factory, null);
    }

    private static <T extends Item> void item(String name, Identifier id, Function<Item.Properties, T> factory,
            @Nullable ResourceKey<CreativeModeTab> externalTab) {
        ENTRIES.add(new Entry<>(name, id, factory, externalTab));
    }

    private static void standardItem(String name, Identifier id, Consumer<Item.Properties> customizer,
            @Nullable ResourceKey<CreativeModeTab> externalTab) {
        item(name, id, p -> {
            customizer.accept(p);
            return new Item(p);
        }, externalTab);
    }

    private static <T extends Item> void coloredItems(String name, Map<AEColor, Identifier> ids,
            BiFunction<Item.Properties, AEColor, T> factory) {
        for (var entry : ids.entrySet()) {
            var color = entry.getKey();
            var fullName = color == AEColor.TRANSPARENT ? name : color.getEnglishName() + " " + name;
            item(fullName, entry.getValue(), p -> factory.apply(p, color));
        }
    }
}
