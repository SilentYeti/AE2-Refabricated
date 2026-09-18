/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

public class BlockDefinition<T extends Block> implements ItemLike {
    private final String englishName;
    private final ItemDefinition<BlockItem> item;
    private final Identifier id;
    private final Supplier<T> block;

    /**
     * The id and a supplier of the block, for the same reason {@link ItemDefinition} takes them that way.
     */
    public BlockDefinition(String englishName, Identifier id, Supplier<T> block, ItemDefinition<BlockItem> item) {
        this.englishName = englishName;
        this.item = Objects.requireNonNull(item, "item");
        this.id = Objects.requireNonNull(id, "id");
        this.block = Objects.requireNonNull(block, "block");
    }

    public String getEnglishName() {
        return englishName;
    }

    public Identifier id() {
        return id;
    }

    public final T block() {
        return this.block.get();
    }

    public ItemStack stack() {
        return item.stack();
    }

    public ItemStack stack(int stackSize) {
        return item.stack(stackSize);
    }

    public GenericStack genericStack(long stackSize) {
        return item.genericStack(stackSize);
    }

    public boolean is(ItemStack comparableStack) {
        return item.is(comparableStack);
    }

    public boolean is(AEKey key) {
        return item.is(key);
    }

    public ItemDefinition<BlockItem> item() {
        return item;
    }

    @Override
    public Item asItem() {
        return item.asItem();
    }
}
