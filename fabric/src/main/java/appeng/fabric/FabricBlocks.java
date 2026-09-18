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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import appeng.core.definitions.AECommonBlocks;

/**
 * Registers the blocks from {@link AECommonBlocks}, each with its {@code BlockItem}.
 * <p>
 * Declaration order is registration order, which matters: the stair, slab and wall variants copy the block state of the
 * block they are cut from, so that block has to exist first. {@link AECommonBlocks} keeps {@code AEBlocks}' ordering,
 * where it already does.
 */
public final class FabricBlocks {
    private FabricBlocks() {
    }

    /** In declaration order, which is the order they appear in the creative tab. */
    private static final Map<ResourceKey<Block>, Block> REGISTERED = new LinkedHashMap<>();

    public static void register() {
        if (!REGISTERED.isEmpty()) {
            throw new IllegalStateException("AE2 blocks were already registered");
        }
        for (var entry : AECommonBlocks.entries()) {
            registerOne(entry);
        }
    }

    /** The blocks that made it into the registry, for the startup log line and for tests. */
    public static List<ResourceKey<Block>> registered() {
        return List.copyOf(REGISTERED.keySet());
    }

    /** Their block items, in the same order, so the creative tab can show them. */
    public static List<Item> registeredItems() {
        return REGISTERED.keySet().stream()
                .map(key -> BuiltInRegistries.ITEM.getValue(key.identifier()))
                .filter(item -> item != Items.AIR)
                .toList();
    }

    private static <T extends Block> void registerOne(AECommonBlocks.Entry<T> entry) {
        var blockKey = ResourceKey.create(Registries.BLOCK, entry.id());
        // setId is mandatory in 26.2 and DeferredRegister normally supplies it
        var properties = BlockBehaviour.Properties.of().setId(blockKey);
        var block = entry.factory().apply(properties, FabricBlocks::resolve);
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        REGISTERED.put(blockKey, block);

        var itemKey = ResourceKey.create(Registries.ITEM, entry.id());
        var item = new BlockItem(block, new Item.Properties()
                .setId(itemKey)
                .useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    /**
     * Looks up a block registered earlier in this pass. Reads from the registry rather than the pending map so that a
     * variant naming a block that has not been registered yet fails loudly here, instead of quietly copying air's block
     * state.
     */
    private static Block resolve(Identifier id) {
        var block = BuiltInRegistries.BLOCK.getValue(id);
        if (block == null || block == Blocks.AIR) {
            throw new IllegalStateException("AE2 block " + id + " must be registered before the variants that "
                    + "copy its block state; check the declaration order in AECommonBlocks");
        }
        return block;
    }
}
