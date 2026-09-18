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

import org.jetbrains.annotations.Nullable;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.helpers.externalstorage.GenericStackInv;

/**
 * Exposes AE2's {@link GenericInternalInventory} to NeoForge's transfer API.
 * <p>
 * Only an inventory that can take part in a transaction may be exposed, because NeoForge's handlers snapshot before
 * every change and expect an aborted transaction to undo it. {@link #journal} answers which:
 * <ul>
 * <li>an inventory that keeps its own journal says so by implementing {@link TransactionalGenericInventory}, which is
 * how an addon's own transactional inventory gets exposed, and which is checked first so that saying so wins;</li>
 * <li>a {@link GenericStackInv} -- every generic inventory AE2 itself exposes -- gets a {@link GenericStackInvJournal},
 * kept on the inventory so it is the same one for the whole of a transaction;</li>
 * <li>anything else is not exposed at all, rather than wrapped in a handler whose changes an aborted transaction could
 * not undo.</li>
 * </ul>
 */
public final class NeoForgeGenericInventories {
    private NeoForgeGenericInventories() {
    }

    @Nullable
    public static TransactionJournal journal(GenericInternalInventory inventory) {
        // Before GenericStackInv, so that an inventory saying it keeps its own journal is taken at its word even if
        // it is a GenericStackInv subclass
        if (inventory instanceof TransactionalGenericInventory transactional) {
            return transactional;
        }
        if (inventory instanceof GenericStackInv inv) {
            return inv.getOrCreatePlatformJournal(() -> new GenericStackInvJournal(inv));
        }
        return null;
    }
}
