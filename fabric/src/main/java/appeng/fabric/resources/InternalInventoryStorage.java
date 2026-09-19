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

import java.util.Collections;
import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import appeng.api.ids.AEItemIds;
import appeng.api.inventories.InternalInventory;

/**
 * Exposes an {@link InternalInventory} as Fabric's {@code SlottedStorage<ItemVariant>}.
 * <p>
 * The Fabric twin of NeoForge's {@code InternalInventoryResourceHandler}, and deliberately the same in every decision:
 * inserting into the whole storage goes through {@link InternalInventory#addItems}, so AE2's own stacking order
 * applies; extracting goes through {@link InternalInventory#removeItems}; a slot goes through
 * {@code insertItem}/{@code extractItem}; wrapped generic stacks can never be extracted, since they are an internal
 * detail of AE2's menus; and the whole inventory is one snapshot, restored by count as well as by stack because an
 * inventory may change a stack's count in place.
 * <p>
 * <b>Change notifications are sent once, when the outermost transaction commits</b>, and only for the slots that differ
 * from how they were before it started -- which is what the NeoForge adapter does from {@code onRootCommit}. Fabric's
 * final-commit hook is not given the original state, so this remembers it itself: the first time it is modified inside
 * an outer transaction it records the state and asks to be told when that transaction closes. Committed, it compares
 * and notifies; aborted, it forgets. The rollback itself is Fabric's usual snapshotting.
 * <p>
 * One thing is intentionally <em>not</em> copied from the NeoForge adapter: a slot's extract there ignores which
 * resource it was asked for. Here a slot only gives up the resource it holds, which is what Fabric's contract for a
 * view requires.
 */
final class InternalInventoryStorage extends SnapshotParticipant<InternalInventoryStorage.Snapshot>
        implements SlottedStorage<ItemVariant> {
    private final InternalInventory inventory;

    /** The state before the current outer transaction first touched this, or null outside of one. */
    @Nullable
    private Snapshot beforeOuter;

    InternalInventoryStorage(InternalInventory inventory) {
        this.inventory = inventory;
    }

    // --- the whole storage ---

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        int amount = saturatedInt(maxAmount);
        if (amount == 0) {
            return 0;
        }

        prepareToModify(transaction);
        var overflow = inventory.addItems(resource.toStack(amount));
        return amount - overflow.getCount();
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        int amount = saturatedInt(maxAmount);
        if (amount == 0 || isWrappedGenericStack(resource)) {
            return 0;
        }

        prepareToModify(transaction);
        return inventory.removeItems(amount, resource.toStack(), null).getCount();
    }

    @Override
    public int getSlotCount() {
        return inventory.size();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return new Slot(slot);
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return Collections.<StorageView<ItemVariant>>unmodifiableList(getSlots()).iterator();
    }

    // --- one slot ---

    private final class Slot implements SingleSlotStorage<ItemVariant> {
        private final int index;

        Slot(int index) {
            this.index = index;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            int amount = saturatedInt(maxAmount);
            if (amount == 0) {
                return 0;
            }

            prepareToModify(transaction);
            var overflow = inventory.insertItem(index, resource.toStack(amount), false);
            return amount - overflow.getCount();
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            int amount = saturatedInt(maxAmount);
            if (amount == 0 || isWrappedGenericStack(resource) || !resource.matches(current())) {
                return 0;
            }

            prepareToModify(transaction);
            return inventory.extractItem(index, amount, false).getCount();
        }

        @Override
        public boolean isResourceBlank() {
            return current().isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(current());
        }

        @Override
        public long getAmount() {
            return current().getCount();
        }

        @Override
        public long getCapacity() {
            return inventory.getSlotLimit(index);
        }

        private ItemStack current() {
            return inventory.getStackInSlot(index);
        }
    }

    // --- transactions ---

    private void prepareToModify(TransactionContext transaction) {
        if (beforeOuter == null) {
            beforeOuter = createSnapshot();
            transaction.getOpenTransaction(0).addOuterCloseCallback(result -> {
                var before = beforeOuter;
                beforeOuter = null;
                if (result.wasCommitted() && before != null) {
                    notifyChangedSlots(before);
                }
            });
        }
        updateSnapshots(transaction);
    }

    private void notifyChangedSlots(Snapshot before) {
        int size = Math.min(before.items.length, inventory.size());
        for (int i = 0; i < size; i++) {
            var current = inventory.getStackInSlot(i);
            if (current != before.items[i] || current.getCount() != before.counts[i]) {
                inventory.sendChangeNotification(i);
            }
        }
    }

    @Override
    protected Snapshot createSnapshot() {
        var snapshot = new Snapshot(inventory.size());
        for (int i = 0; i < snapshot.items.length; i++) {
            var stack = inventory.getStackInSlot(i);
            snapshot.items[i] = stack;
            snapshot.counts[i] = stack.getCount();
        }
        return snapshot;
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        for (int i = 0; i < snapshot.items.length; i++) {
            var stack = snapshot.items[i];
            // The inventory may have changed this very stack's count in place, so the count is restored as well
            if (stack.getCount() != snapshot.counts[i]) {
                stack.setCount(snapshot.counts[i]);
            }
            inventory.setItemDirect(i, stack);
        }
    }

    static final class Snapshot {
        final ItemStack[] items;
        final int[] counts;

        Snapshot(int size) {
            this.items = new ItemStack[size];
            this.counts = new int[size];
        }
    }

    // --- helpers ---

    /**
     * By id rather than against the item itself: the wrapped-stack item does not register on Fabric yet, and looking it
     * up before it does would answer with the registry's default item.
     */
    private static boolean isWrappedGenericStack(ItemVariant resource) {
        return BuiltInRegistries.ITEM.getKey(resource.getItem()).equals(AEItemIds.WRAPPED_GENERIC_STACK);
    }

    private static int saturatedInt(long amount) {
        return (int) Math.min(amount, Integer.MAX_VALUE);
    }

    @Override
    public String toString() {
        return "InternalInventoryStorage[" + inventory + "]";
    }
}
