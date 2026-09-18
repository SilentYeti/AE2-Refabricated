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

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.util.helpers.ItemComparisonHelper;

public class ItemDefinition<T extends Item> implements ItemLike, Supplier<T> {
    private final String englishName;
    private final Identifier id;
    private final Supplier<T> item;

    /**
     * The id and a supplier of the item, rather than one of the loader's deferred-registry handles: those are the same
     * two things behind one type, and naming the type here would put every definitions table -- and the hundreds of
     * files that reach one -- on a single loader's side of the build.
     */
    public ItemDefinition(String englishName, Identifier id, Supplier<T> item) {
        this.englishName = englishName;
        this.id = id;
        this.item = item;
    }

    public String getEnglishName() {
        return englishName;
    }

    public Identifier id() {
        return this.id;
    }

    public ItemStack stack() {
        return stack(1);
    }

    public ItemStack stack(int stackSize) {
        return new ItemStack(asItem(), stackSize);
    }

    public ItemStackTemplate template() {
        return template(1);
    }

    public ItemStackTemplate template(int stackSize) {
        return new ItemStackTemplate(asItem(), stackSize);
    }

    public ItemStackTemplate template(Consumer<DataComponentPatch.Builder> customizer) {
        return template(1, customizer);
    }

    public ItemStackTemplate template(int stackSize, Consumer<DataComponentPatch.Builder> customizer) {
        var patch = DataComponentPatch.builder();
        customizer.accept(patch);
        return new ItemStackTemplate(holder(), stackSize, patch.build());
    }

    public GenericStack genericStack(long stackSize) {
        return new GenericStack(AEItemKey.of(asItem()), stackSize);
    }

    public Holder<Item> holder() {
        return BuiltInRegistries.ITEM.wrapAsHolder(asItem());
    }

    public Component getName() {
        return item.get().getName(item.get().getDefaultInstance());
    }

    /**
     * Compare {@link ItemStack} with this
     *
     * @param comparableStack compared item
     * @return true if the item stack is a matching item.
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public final boolean isSameAs(ItemStack comparableStack) {
        return is(comparableStack);
    }

    /**
     * Compare {@link ItemStack} with this
     *
     * @param comparableStack compared item
     * @return true if the item stack is a matching item.
     */
    public final boolean is(ItemStack comparableStack) {
        return ItemComparisonHelper.isEqualItemType(comparableStack, this.stack());
    }

    /**
     * @return True if this item is represented by the given key.
     */
    public final boolean is(AEKey key) {
        if (key instanceof AEItemKey itemKey) {
            return asItem() == itemKey.getItem();
        }
        return false;
    }

    /**
     * @return True if this item is represented by the given key.
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public final boolean isSameAs(AEKey key) {
        return is(key);
    }

    @Override
    public T get() {
        return item.get();
    }

    @Override
    public T asItem() {
        return item.get();
    }
}
