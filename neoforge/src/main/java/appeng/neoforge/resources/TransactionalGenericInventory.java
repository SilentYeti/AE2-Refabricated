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

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import appeng.api.behaviors.GenericInternalInventory;

/**
 * A {@link GenericInternalInventory} that can take part in a NeoForge transfer transaction, which is what lets it be
 * exposed as NeoForge's item and fluid handlers: the handler snapshots it before each change, and an aborted
 * transaction rolls the change back.
 * <p>
 * This hook used to be {@code GenericInternalInventory.updateSnapshots}, on the API interface itself, which is the one
 * thing that kept the interface out of {@code :common}. Every inventory AE2 exposes this way is a
 * {@code GenericStackInv}, which already has the method through NeoForge's {@code SnapshotJournal}.
 * <p>
 * A generic inventory that does not implement this is not exposed to NeoForge's transfer API at all, rather than being
 * wrapped in a handler whose changes an aborted transaction could not undo.
 */
public interface TransactionalGenericInventory extends GenericInternalInventory {
    /**
     * Saves the inventory's state in the transaction, right before modifying it.
     */
    void updateSnapshots(TransactionContext transaction);
}
