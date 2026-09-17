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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.ids.AECreativeTabIds;
import appeng.api.ids.AEItemIds;
import appeng.core.definitions.AECommonItems;
import appeng.core.definitions.CreativeTabSink;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;

/**
 * Registers the items from {@link AECommonItems} and AE2's creative tab.
 * <p>
 * NeoForge builds its items through {@code DeferredRegister} during the registry event; Fabric has no deferred phase,
 * so these go straight into {@link BuiltInRegistries} while the mod initializer runs. Everything else -- the models,
 * textures, tags and translations -- already ships in the {@code :common} resources that this jar bundles, so a
 * registered item is a finished item.
 */
public final class FabricItems {
    private FabricItems() {
    }

    private static final CreativeModeTab.TabVisibility TAB_AND_SEARCH = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;

    /** In declaration order, which is the order they appear in the creative tab. */
    private static final Map<ResourceKey<Item>, Item> REGISTERED = new LinkedHashMap<>();

    public static void register() {
        if (!REGISTERED.isEmpty()) {
            throw new IllegalStateException("AE2 items were already registered");
        }

        for (var entry : AECommonItems.entries()) {
            registerOne(entry);
        }

        registerCreativeTab();
        registerExternalTabEntries();
    }

    /** The items that made it into the registry, for the startup log line and for tests. */
    public static List<ResourceKey<Item>> registered() {
        return List.copyOf(REGISTERED.keySet());
    }

    private static <T extends Item> void registerOne(AECommonItems.Entry<T> entry) {
        var key = ResourceKey.create(Registries.ITEM, entry.id());
        // setId is mandatory in 26.2 and DeferredRegister normally supplies it
        var item = entry.factory().apply(new Item.Properties().setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        REGISTERED.put(key, item);
    }

    private static void registerCreativeTab() {
        var tab = FabricCreativeModeTab.builder()
                .title(GuiText.CreativeTab.text())
                // NeoForge uses the controller block; no blocks are registered on Fabric yet, so the tab is
                // iconed with an item that exists. Switch to AEBlocks.CONTROLLER once blocks follow.
                .icon(() -> new ItemStack(REGISTERED.get(ResourceKey.create(Registries.ITEM, AEItemIds.FLUIX_CRYSTAL))))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, AECreativeTabIds.MAIN, tab);

        // Filled through the event rather than Builder.displayItems, because that callback is typed on
        // CreativeModeTab.Output, which is protected in vanilla and would need an access widener here.
        CreativeModeTabEvents.modifyOutputEvent(AECreativeTabIds.MAIN).register(output -> {
            CreativeTabSink sink = stack -> output.accept(stack, TAB_AND_SEARCH);
            for (var item : REGISTERED.values()) {
                // Same dispatch as MainCreativeTab: the item decides how it shows up, which is what gives
                // the paint balls and the pre-charged tool variants their own entries.
                if (item instanceof AEBaseItem baseItem) {
                    baseItem.addToMainCreativeTab(output.getContext(), sink);
                } else {
                    sink.accept(item);
                }
            }
        });
    }

    private static void registerExternalTabEntries() {
        var byTab = new LinkedHashMap<ResourceKey<CreativeModeTab>, List<Item>>();
        for (var entry : AECommonItems.entries()) {
            if (entry.externalTab() == null) {
                continue;
            }
            var item = REGISTERED.get(ResourceKey.create(Registries.ITEM, entry.id()));
            byTab.computeIfAbsent(entry.externalTab(), k -> new ArrayList<>()).add(item);
        }

        byTab.forEach((tab, items) -> CreativeModeTabEvents.modifyOutputEvent(tab)
                .register(output -> items.forEach(item -> output.accept(new ItemStack(item), TAB_AND_SEARCH))));
    }
}
