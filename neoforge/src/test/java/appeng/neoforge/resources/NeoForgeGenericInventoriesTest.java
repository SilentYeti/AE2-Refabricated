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

package appeng.neoforge.resources;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.externalstorage.GenericStackItemHandler;
import appeng.util.BootstrapMinecraft;

/**
 * {@code GenericStackInv} was a NeoForge {@code SnapshotJournal}, which is what kept the class AE2's configuration and
 * interface storage is built on inside {@code :neoforge}. The journal is now NeoForge's own
 * {@link GenericStackInvJournal} over it, reached through {@link NeoForgeGenericInventories#journal}.
 * <p>
 * These pin the part that only shows itself under a transaction, and that a wrong answer would leave looking fine until
 * an aborted transfer duplicated or destroyed something: which inventories may be exposed at all, that the journal is
 * the same object throughout a transaction, and that rollback and commit still do what the three overridden methods
 * did.
 */
@BootstrapMinecraft
// Item resources carry data components, which are only bound once a server is running; the tests that touch
// them take it as a parameter, which is what starts it
@ExtendWith(EphemeralTestServerProvider.class)
class NeoForgeGenericInventoriesTest {

    private static GenericStackInv inv(int size, Runnable listener) {
        return new GenericStackInv(listener, GenericStackInv.Mode.STORAGE, size);
    }

    @Test
    void theJournalIsTheSameOneForTheWholeOfATransaction() {
        // A journal that were not would snapshot a state that had already been modified, and roll back to it
        var inv = inv(2, () -> {
        });

        var first = NeoForgeGenericInventories.journal(inv);

        assertThat(first).isNotNull();
        assertThat(NeoForgeGenericInventories.journal(inv)).isSameAs(first);
    }

    @Test
    void differentInventoriesGetDifferentJournals() {
        assertThat(NeoForgeGenericInventories.journal(inv(1, () -> {
        }))).isNotSameAs(NeoForgeGenericInventories.journal(inv(1, () -> {
        })));
    }

    @Test
    void anInventoryThatSaysItKeepsItsOwnJournalIsTakenAtItsWord() {
        // The addon extension point, and it wins even over GenericStackInv's own journal
        var inv = new OwnJournalInventory();

        assertThat(NeoForgeGenericInventories.journal(inv)).isSameAs(inv);
    }

    @Test
    void anAbortedTransactionPutsTheContentsBackAndSaysNothing(MinecraftServer server) {
        var notifications = new int[1];
        var inv = inv(2, () -> notifications[0]++);
        inv.setStack(0, new GenericStack(AEItemKey.of(Items.DIAMOND), 4));
        notifications[0] = 0;

        var handler = new GenericStackItemHandler(inv, NeoForgeGenericInventories.journal(inv));
        try (var tx = Transaction.openRoot()) {
            assertThat(handler.insert(ItemResource.of(Items.DIAMOND), 3, tx)).isEqualTo(3);
            assertThat(inv.getAmount(0)).isEqualTo(7);
            // and then no commit
        }

        assertThat(inv.getAmount(0)).isEqualTo(4);
        assertThat(notifications[0]).as("a rollback restores what the listener was last told, so it is not told again")
                .isZero();
    }

    @Test
    void aCommittedTransactionNotifiesOnceForAllOfItsChanges(MinecraftServer server) {
        var notifications = new int[1];
        var inv = inv(2, () -> notifications[0]++);

        var handler = new GenericStackItemHandler(inv, NeoForgeGenericInventories.journal(inv));
        try (var tx = Transaction.openRoot()) {
            handler.insert(ItemResource.of(Items.DIAMOND), 3, tx);
            handler.insert(ItemResource.of(Items.EMERALD), 5, tx);
            assertThat(notifications[0]).as("changes inside a transaction are not announced as they happen").isZero();
            tx.commit();
        }

        assertThat(inv.getStack(0)).isEqualTo(new GenericStack(AEItemKey.of(Items.DIAMOND), 3));
        assertThat(inv.getStack(1)).isEqualTo(new GenericStack(AEItemKey.of(Items.EMERALD), 5));
        assertThat(notifications[0]).isEqualTo(1);
    }

    @Test
    void aCommittedTransactionThatChangedNothingInTheEndSaysNothing(MinecraftServer server) {
        var notifications = new int[1];
        var inv = inv(2, () -> notifications[0]++);

        var handler = new GenericStackItemHandler(inv, NeoForgeGenericInventories.journal(inv));
        try (var tx = Transaction.openRoot()) {
            var resource = ItemResource.of(Items.DIAMOND);
            assertThat(handler.insert(resource, 3, tx)).isEqualTo(3);
            assertThat(handler.extract(resource, 3, tx)).isEqualTo(3);
            tx.commit();
        }

        assertThat(inv.isEmpty()).isTrue();
        assertThat(notifications[0]).isZero();
    }

    private static class OwnJournalInventory extends GenericStackInv implements TransactionalGenericInventory {
        OwnJournalInventory() {
            super(null, Mode.STORAGE, 1);
        }

        @Override
        public void updateSnapshots(net.neoforged.neoforge.transfer.transaction.TransactionContext transaction) {
        }
    }
}
