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

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;

/**
 * Takes part in a NeoForge transfer transaction on a {@link GenericStackInv}'s behalf, so that an aborted transaction
 * rolls its changes back.
 * <p>
 * {@code GenericStackInv} used to <em>be</em> a {@link SnapshotJournal}, which was the one thing keeping a class the
 * whole of AE2's configuration and interface storage is built on inside {@code :neoforge}. The three bodies it overrode
 * are now three methods on the inventory, and this is the NeoForge shape around them.
 * <p>
 * Reached through {@link NeoForgeGenericInventories#journal}, which keeps one of these per inventory: a journal that
 * was not the same object throughout a transaction would snapshot a state that had already been modified, and roll back
 * to it.
 */
final class GenericStackInvJournal extends SnapshotJournal<GenericStack[]> implements TransactionJournal {
    private final GenericStackInv inv;

    GenericStackInvJournal(GenericStackInv inv) {
        this.inv = inv;
    }

    @Override
    protected GenericStack[] createSnapshot() {
        return inv.copySlots();
    }

    @Override
    protected void revertToSnapshot(GenericStack[] snapshot) {
        inv.restoreSlots(snapshot);
    }

    @Override
    protected void onRootCommit(GenericStack[] originalState) {
        inv.onCommitted(originalState);
    }
}
