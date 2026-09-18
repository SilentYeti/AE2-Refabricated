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

package appeng.fabric.gametest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import appeng.api.ids.AECreativeTabIds;
import appeng.core.definitions.AECommonBlocks;
import appeng.core.definitions.AECommonItems;

/**
 * Checks what AE2 actually registered on Fabric, in a running client with a loaded world.
 * <p>
 * The unit tests on the NeoForge side cannot cover this: they check that {@link AECommonItems} agrees with
 * {@code AEItems}, not that Fabric's registration works. And loading a world is the whole point of doing it here rather
 * than on the title screen -- the integrated server is what parses AE2's recipes, tags and loot tables, so this is the
 * only place they get exercised on Fabric at all.
 */
public class AE2ClientGameTest implements FabricClientGameTest {
    private static final Logger LOG = LoggerFactory.getLogger("AE2GameTest");

    /** One of each kind of item that made it across: a tool, a material, a print, a component. */
    private static final List<String> HOTBAR_SHOWCASE = List.of(
            "ae2:certus_quartz_axe",
            "ae2:fluix_pickaxe",
            "ae2:fluix_sword",
            "ae2:certus_quartz_crystal",
            "ae2:logic_processor",
            "ae2:cell_component_64k",
            "ae2:wireless_receiver",
            "ae2:sky_dust",
            "ae2:singularity",
            "ae2:quartz_block",
            "ae2:sky_stone_block",
            "ae2:quartz_glass");

    @Override
    public void runTest(ClientGameTestContext context) {
        assertItemsRegistered(context);
        assertBlocksRegistered(context);
        assertCreativeTabRegistered(context);
        assertItemModelsResolved(context);

        try (var singleplayer = context.worldBuilder().create()) {
            var server = singleplayer.getServer();

            // Reaching here means the integrated server started, which is what parses a data pack.
            var recipeCount = server.computeOnServer(s -> {
                var count = 0;
                for (var holder : s.getRecipeManager().getRecipes()) {
                    if (holder.id().identifier().getNamespace().equals("ae2")) {
                        count++;
                    }
                }
                return count;
            });
            LOG.info("AE2 game test: world loaded, {} ae2 recipes available", recipeCount);

            // Put real AE2 stacks in the hotbar so the screenshot shows the item models actually
            // rendering, not just that they resolved to something.
            for (var id : HOTBAR_SHOWCASE) {
                server.runCommand("give @a " + id + " 1");
            }
            context.waitTicks(20);
            context.takeScreenshot("ae2_items_in_hotbar");
        }

        LOG.info("AE2 game test: passed");
    }

    /** Blocks, and the BlockItem each one needs to be placeable from the creative tab. */
    private static void assertBlocksRegistered(ClientGameTestContext context) {
        var missing = new ArrayList<Identifier>();
        var withoutItem = new ArrayList<Identifier>();
        for (var entry : AECommonBlocks.entries()) {
            if (!BuiltInRegistries.BLOCK.containsKey(entry.id())) {
                missing.add(entry.id());
            } else if (!BuiltInRegistries.ITEM.containsKey(entry.id())) {
                withoutItem.add(entry.id());
            }
        }
        if (!missing.isEmpty()) {
            throw new AssertionError("AE2 blocks declared in AECommonBlocks but not registered: " + missing);
        }
        if (!withoutItem.isEmpty()) {
            throw new AssertionError("AE2 blocks registered without a BlockItem: " + withoutItem);
        }
        LOG.info("AE2 game test: {} blocks registered, each with a block item", AECommonBlocks.entries().size());
    }

    private static void assertItemsRegistered(ClientGameTestContext context) {
        var missing = new ArrayList<Identifier>();
        for (var entry : AECommonItems.entries()) {
            if (!BuiltInRegistries.ITEM.containsKey(entry.id())) {
                missing.add(entry.id());
            }
        }
        if (!missing.isEmpty()) {
            throw new AssertionError("AE2 items declared in AECommonItems but not registered: " + missing);
        }

        // Block items share the item registry, so they count towards the ae2 namespace total.
        var expected = AECommonItems.entries().size() + AECommonBlocks.entries().size();
        List<ResourceKey<Item>> ae2Items = BuiltInRegistries.ITEM.registryKeySet().stream()
                .filter(key -> key.identifier().getNamespace().equals("ae2"))
                .toList();
        if (ae2Items.size() != expected) {
            throw new AssertionError("expected exactly " + expected + " ae2 items in the registry, found "
                    + ae2Items.size() + ": " + ae2Items);
        }
        LOG.info("AE2 game test: {} items registered", expected);
    }

    /**
     * Every registered item must have a real model, not the missing-model placeholder.
     * <p>
     * Worth asserting rather than eyeballing: the item model definitions under {@code assets/ae2/items} are datagen
     * output, and while they lived in {@code :neoforge} the Fabric jar shipped none of them. Minecraft only whispers
     * about that -- one {@code Missing item model for location} warning per id -- so the items looked registered and
     * rendered as the purple-and-black placeholder.
     */
    private static void assertItemModelsResolved(ClientGameTestContext context) {
        var unmodelled = context.computeOnClient(client -> {
            var models = client.getModelManager();
            // getItemModel falls back to a single shared placeholder, so ask for an id that cannot
            // exist to get hold of it, then compare.
            var missing = models.getItemModel(Identifier.fromNamespaceAndPath("ae2", "missing_model_probe"));
            var bad = new ArrayList<Identifier>();
            for (var entry : AECommonItems.entries()) {
                if (models.getItemModel(entry.id()) == missing) {
                    bad.add(entry.id());
                }
            }
            // Block items are datagen output too, and were exactly as invisible when they went missing.
            for (var entry : AECommonBlocks.entries()) {
                if (models.getItemModel(entry.id()) == missing) {
                    bad.add(entry.id());
                }
            }
            return bad;
        });

        if (!unmodelled.isEmpty()) {
            throw new AssertionError(unmodelled.size() + " AE2 items have no item model: " + unmodelled);
        }
        LOG.info("AE2 game test: all {} items and block items have a resolved model",
                AECommonItems.entries().size() + AECommonBlocks.entries().size());
    }

    private static void assertCreativeTabRegistered(ClientGameTestContext context) {
        var tab = BuiltInRegistries.CREATIVE_MODE_TAB.getOptional(AECreativeTabIds.MAIN.identifier());
        if (tab.isEmpty()) {
            throw new AssertionError("AE2's creative tab was not registered under " + AECreativeTabIds.MAIN);
        }
        // The tab's contents are built lazily from the display parameters, so this only checks it exists;
        // the item count above is what pins what should end up in it.
        LOG.info("AE2 game test: creative tab {} registered", AECreativeTabIds.MAIN.identifier());
    }
}
