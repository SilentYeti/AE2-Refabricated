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

package appeng.fabric.resources;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.inventories.ItemTransfer;
import appeng.util.helpers.ItemComparisonHelper;

/**
 * AE2's {@link ItemTransfer} over another mod's {@code Storage<ItemVariant>} -- what
 * {@code FabricItemTransferPlatform.findExternal} returns.
 * <p>
 * On NeoForge the external handler is wrapped as an {@code InternalInventory}, whose default methods implement these by
 * walking its slots. A Fabric storage need not have slots, so this walks its views instead, and each method keeps the
 * rules the slot walk has:
 * <ul>
 * <li>an empty filter takes anything; after the first extraction, only more of <em>that</em> exact item, so a single
 * call never hands back two different items as one stack;</li>
 * <li>the destination is asked about each view's extraction before it is kept, and a view it refuses is left
 * alone;</li>
 * <li>a fuzzy removal only ever takes from one view, so that two stacks of different damage are never merged;</li>
 * <li>a simulation changes nothing.</li>
 * </ul>
 * Every call is its own outer transaction, as {@code Transaction.open(null)} is in the NeoForge wrapper: AE2 calls
 * these from machine ticks, never from inside someone else's transaction. Each view's extraction is nested inside it,
 * so a refused one can be rolled back on its own -- the pattern Fabric's own {@code StorageUtil.move} uses, and what
 * makes it safe to modify the storage while walking its views.
 */
public final class StorageItemTransfer implements ItemTransfer {
    private final Storage<ItemVariant> storage;

    public StorageItemTransfer(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    @Override
    public ItemStack removeItems(int amount, ItemStack filter, @Nullable Predicate<ItemStack> destination) {
        return remove(amount, filter, destination, false);
    }

    @Override
    public ItemStack simulateRemove(int amount, ItemStack filter, Predicate<ItemStack> destination) {
        return remove(amount, filter, destination, true);
    }

    @Override
    public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode,
            Predicate<ItemStack> destination) {
        return removeSimilar(amount, filter, fuzzyMode, destination, false);
    }

    @Override
    public ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode,
            Predicate<ItemStack> destination) {
        return removeSimilar(amount, filter, fuzzyMode, destination, true);
    }

    @Override
    public ItemStack addItems(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (var transaction = Transaction.openOuter()) {
            long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return inserted == 0 ? stack : stack.copyWithCount(stack.getCount() - (int) inserted);
        }
    }

    private ItemStack remove(int amount, ItemStack filter, @Nullable Predicate<ItemStack> destination,
            boolean simulate) {
        var result = ItemStack.EMPTY;

        try (var outer = Transaction.openOuter()) {
            for (var view : storage.nonEmptyViews()) {
                if (amount <= 0) {
                    break;
                }
                var resource = view.getResource();
                if (!filter.isEmpty() && !resource.matches(filter)) {
                    continue;
                }

                try (var nested = outer.openNested()) {
                    long got = view.extract(resource, amount, nested);
                    if (got <= 0) {
                        continue;
                    }
                    var extracted = resource.toStack((int) got);
                    if (destination != null && !destination.test(extracted)) {
                        continue; // not committed, so this view is left as it was
                    }
                    nested.commit();

                    if (result.isEmpty()) {
                        // The first extraction decides the item; after it, only more of the same
                        result = extracted;
                        filter = extracted;
                    } else {
                        result.grow(extracted.getCount());
                    }
                    amount -= extracted.getCount();
                }
            }

            if (!simulate) {
                outer.commit();
            }
        }

        return result;
    }

    private ItemStack removeSimilar(int amount, ItemStack filter, FuzzyMode fuzzyMode,
            @Nullable Predicate<ItemStack> destination, boolean simulate) {
        try (var outer = Transaction.openOuter()) {
            for (var view : storage.nonEmptyViews()) {
                var resource = view.getResource();
                if (!filter.isEmpty() && !ItemComparisonHelper.isFuzzyEqualItem(
                        resource.toStack((int) Math.min(view.getAmount(), Integer.MAX_VALUE)), filter, fuzzyMode)) {
                    continue;
                }

                try (var nested = outer.openNested()) {
                    long got = view.extract(resource, amount, nested);
                    if (got <= 0) {
                        continue;
                    }
                    var extracted = resource.toStack((int) got);
                    if (destination != null && !destination.test(extracted)) {
                        continue; // keep looking, as the slot walk does
                    }
                    nested.commit();
                    if (!simulate) {
                        outer.commit();
                    }
                    // Only ever one view: two stacks that are merely similar must not become one
                    return extracted;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
