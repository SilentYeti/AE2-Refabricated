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

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.fabric.resources.FabricInventories;
import appeng.platform.ItemTransferPlatform;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;

/**
 * Item transfer on Fabric, checked in a running world, in both directions: AE2's inventories exposed as Fabric storages
 * ({@link FabricInventories}), and another block's inventory reached through {@link ItemTransferPlatform#findExternal}
 * -- here a vanilla chest.
 * <p>
 * Here rather than in unit tests because {@code :fabric} has none, and because the part worth checking is exactly what
 * only a real Fabric runtime does: its transaction manager, its snapshot participants, its chest storage. The NeoForge
 * side pins the same decisions in {@code NeoForgeInventoriesTest} and {@code NeoForgeGenericInventoriesTest}.
 */
final class TransferChecks {
    private TransferChecks() {
    }

    /** Runs every check on the server thread; returns how many ran, or throws on the first that fails. */
    static int run(MinecraftServer server) {
        var checks = new ArrayList<Runnable>();
        checks.add(TransferChecks::anInventoryHandsOutTheSameStorageEveryTime);
        checks.add(TransferChecks::anAbortedInsertLeavesNothingBehind);
        checks.add(TransferChecks::aCommittedInsertStays);
        checks.add(TransferChecks::anAbortedNestedTransactionIsUndoneInsideACommittedOne);
        checks.add(TransferChecks::aSlotOnlyGivesUpWhatItHolds);
        checks.add(TransferChecks::changesAreAnnouncedOnceAtTheEndAndOnlyForChangedSlots);
        checks.add(TransferChecks::anAbortedTransactionAnnouncesNothing);
        checks.add(TransferChecks::aCombinedInventoryIsOneStorageOverItsParts);
        checks.add(TransferChecks::theEmptyInventoryIsAnEmptyStorage);
        checks.add(() -> aChestIsReachedAndFollowsTheSlotWalkRules(server));
        checks.forEach(Runnable::run);
        return checks.size();
    }

    // --- AE2's inventories, outwards ---

    private static void anInventoryHandsOutTheSameStorageEveryTime() {
        var inv = new AppEngInternalInventory(3);
        check(FabricInventories.storage(inv) == FabricInventories.storage(inv),
                "the same inventory should hand out the same storage, as other mods may cache it by identity");
    }

    private static void anAbortedInsertLeavesNothingBehind() {
        var inv = new AppEngInternalInventory(3);
        var storage = FabricInventories.storage(inv);

        try (var tx = Transaction.openOuter()) {
            long inserted = storage.insert(ItemVariant.of(Items.DIAMOND), 10, tx);
            check(inserted == 10, "expected 10 diamonds inserted, got " + inserted);
            check(count(inv, Items.DIAMOND) == 10, "the insert should be visible inside the transaction");
        }

        check(count(inv, Items.DIAMOND) == 0, "an aborted insert must leave nothing behind");
    }

    private static void aCommittedInsertStays() {
        var inv = new AppEngInternalInventory(3);
        var storage = FabricInventories.storage(inv);

        try (var tx = Transaction.openOuter()) {
            storage.insert(ItemVariant.of(Items.DIAMOND), 10, tx);
            tx.commit();
        }

        check(count(inv, Items.DIAMOND) == 10, "a committed insert must stay");
    }

    private static void anAbortedNestedTransactionIsUndoneInsideACommittedOne() {
        var inv = new AppEngInternalInventory(3);
        var storage = FabricInventories.storage(inv);

        try (var outer = Transaction.openOuter()) {
            storage.insert(ItemVariant.of(Items.DIAMOND), 4, outer);
            try (var nested = outer.openNested()) {
                storage.insert(ItemVariant.of(Items.EMERALD), 5, nested);
            }
            outer.commit();
        }

        check(count(inv, Items.DIAMOND) == 4, "the outer transaction's insert should stay");
        check(count(inv, Items.EMERALD) == 0, "the aborted nested insert should be gone");
    }

    private static void aSlotOnlyGivesUpWhatItHolds() {
        var inv = new AppEngInternalInventory(1);
        inv.setItemDirect(0, new ItemStack(Items.DIAMOND, 8));
        var slot = FabricInventories.storage(inv).getSlot(0);

        try (var tx = Transaction.openOuter()) {
            long wrong = slot.extract(ItemVariant.of(Items.EMERALD), 8, tx);
            check(wrong == 0, "a slot holding diamonds must not give up 'emeralds', gave " + wrong);
            long right = slot.extract(ItemVariant.of(Items.DIAMOND), 3, tx);
            check(right == 3, "expected 3 diamonds from the slot, got " + right);
            tx.commit();
        }
        check(count(inv, Items.DIAMOND) == 5, "5 diamonds should remain");
    }

    private static void changesAreAnnouncedOnceAtTheEndAndOnlyForChangedSlots() {
        var inv = new RecordingInventory(3);
        var storage = FabricInventories.storage(inv);

        try (var tx = Transaction.openOuter()) {
            storage.getSlot(1).insert(ItemVariant.of(Items.DIAMOND), 2, tx);
            storage.getSlot(1).insert(ItemVariant.of(Items.DIAMOND), 3, tx);
            check(inv.notified.isEmpty(), "nothing should be announced before the transaction ends");
            tx.commit();
        }

        check(inv.notified.equals(List.of(1)),
                "exactly slot 1 should be announced, once, at commit; got " + inv.notified);
    }

    private static void anAbortedTransactionAnnouncesNothing() {
        var inv = new RecordingInventory(2);
        var storage = FabricInventories.storage(inv);

        try (var tx = Transaction.openOuter()) {
            storage.insert(ItemVariant.of(Items.DIAMOND), 5, tx);
        }
        check(inv.notified.isEmpty(), "an aborted transaction should announce nothing; got " + inv.notified);

        // The aborted transaction's "before" must not leak into the next one: change slot 1 behind the storage's
        // back, then commit a change to slot 0. Comparing against a stale "before" would also announce slot 1.
        inv.setItemDirect(1, new ItemStack(Items.EMERALD, 4));
        try (var tx = Transaction.openOuter()) {
            storage.getSlot(0).insert(ItemVariant.of(Items.DIAMOND), 1, tx);
            tx.commit();
        }
        check(inv.notified.equals(List.of(0)), "only slot 0 changed in that transaction; got " + inv.notified);
    }

    private static void aCombinedInventoryIsOneStorageOverItsParts() {
        var combined = new CombinedInternalInventory(new AppEngInternalInventory(1), new AppEngInternalInventory(2));
        var storage = FabricInventories.storage(combined);

        check(storage.getSlotCount() == 3, "expected 3 slots, got " + storage.getSlotCount());
        check(FabricInventories.storage(combined) == storage, "the combined storage should be kept");
    }

    private static void theEmptyInventoryIsAnEmptyStorage() {
        var storage = FabricInventories.storage(InternalInventory.empty());
        check(storage.getSlotCount() == 0, "the empty inventory should have no slots");
        try (var tx = Transaction.openOuter()) {
            check(storage.insert(ItemVariant.of(Items.DIAMOND), 1, tx) == 0, "nothing fits in the empty inventory");
        }
    }

    // --- another block's inventory, inwards ---

    private static void aChestIsReachedAndFollowsTheSlotWalkRules(MinecraftServer server) {
        var level = server.overworld();
        var pos = new BlockPos(0, level.getMaxY() - 2, 0);
        var above = pos.above();
        level.setBlockAndUpdate(above, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        try {
            var chest = (Container) level.getBlockEntity(pos);
            check(chest != null, "the chest should have a block entity");
            chest.setItem(0, new ItemStack(Items.COBBLESTONE, 5));
            chest.setItem(1, new ItemStack(Items.DIRT, 3));
            chest.setItem(2, new ItemStack(Items.COBBLESTONE, 7));

            var transfer = ItemTransferPlatform.get().findExternal(level, pos, Direction.UP);
            check(transfer != null, "Fabric should find the chest's item storage");
            check(ItemTransferPlatform.get().findExternal(level, above, Direction.DOWN) == null,
                    "air has no item storage");

            var simulated = transfer.simulateRemove(10, ItemStack.EMPTY, null);
            check(simulated.is(Items.COBBLESTONE) && simulated.getCount() == 10,
                    "a simulation should find 10 cobblestone across two slots, found " + simulated);
            check(chest.getItem(0).getCount() == 5 && chest.getItem(2).getCount() == 7,
                    "a simulation must change nothing");

            var removed = transfer.removeItems(10, ItemStack.EMPTY, null);
            check(removed.is(Items.COBBLESTONE) && removed.getCount() == 10,
                    "expected 10 cobblestone and nothing else, got " + removed);
            check(countIn(chest, Items.COBBLESTONE) == 2 && countIn(chest, Items.DIRT) == 3,
                    "2 cobblestone and all the dirt should remain");

            var refused = transfer.removeItems(64, ItemStack.EMPTY, stack -> !stack.is(Items.COBBLESTONE));
            check(refused.is(Items.DIRT) && refused.getCount() == 3,
                    "the destination refused cobblestone, so only the dirt should come out; got " + refused);
            check(countIn(chest, Items.COBBLESTONE) == 2, "the refused cobblestone must be left in place");

            var overflow = transfer.simulateAdd(new ItemStack(Items.STONE, 64));
            check(overflow.isEmpty(), "64 stone should fit in the chest, overflow " + overflow);
            check(countIn(chest, Items.STONE) == 0, "a simulated add must change nothing");

            overflow = transfer.addItems(new ItemStack(Items.STONE, 64));
            check(overflow.isEmpty() && countIn(chest, Items.STONE) == 64, "64 stone should be added");
        } finally {
            if (level.getBlockEntity(pos) instanceof Container chest) {
                chest.clearContent();
            }
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    // --- helpers ---

    private static int count(InternalInventory inv, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getStackInSlot(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static int countIn(Container container, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            var stack = container.getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("AE2 transfer check failed: " + message);
        }
    }

    /**
     * An inventory that records its change notifications and never sends any of its own, so that the only ones seen are
     * the storage adapter's.
     */
    private static final class RecordingInventory extends BaseInternalInventory {
        private final ItemStack[] stacks;
        final List<Integer> notified = new ArrayList<>();

        RecordingInventory(int size) {
            this.stacks = new ItemStack[size];
            java.util.Arrays.fill(stacks, ItemStack.EMPTY);
        }

        @Override
        public int size() {
            return stacks.length;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return stacks[slotIndex];
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            stacks[slotIndex] = stack;
        }

        @Override
        public void sendChangeNotification(int slot) {
            notified.add(slot);
        }
    }
}
