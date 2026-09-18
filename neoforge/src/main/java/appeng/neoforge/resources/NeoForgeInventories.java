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

import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;

/**
 * Exposes AE2's {@link InternalInventory} to NeoForge's transfer API.
 * <p>
 * This is what {@code InternalInventory.toResourceHandler()} did before the interface moved to {@code :common}, and it
 * gives the same answer in every case:
 * <ul>
 * <li>an inventory that knows its own adapter ({@link ResourceHandlerProvider}) supplies it;</li>
 * <li>the shared empty inventory is NeoForge's empty handler;</li>
 * <li>a {@link BaseInternalInventory} gets the generic wrapper, created once and then handed out every time -- NeoForge
 * caches capability results by identity, so this has to be the same object on each call.</li>
 * </ul>
 */
public final class NeoForgeInventories {
    private NeoForgeInventories() {
    }

    public static ResourceHandler<ItemResource> resourceHandler(InternalInventory inventory) {
        if (inventory instanceof ResourceHandlerProvider provider) {
            return provider.toResourceHandler();
        }
        if (inventory == InternalInventory.empty()) {
            return EmptyResourceHandler.instance();
        }
        if (inventory instanceof BaseInternalInventory base) {
            return base.getOrCreatePlatformAdapter(() -> new InternalInventoryResourceHandler(base));
        }
        // Every AE2 inventory is one of the above. This is for an addon that implements InternalInventory directly:
        // before the move it had to supply its own adapter, and the generic wrapper is what that adapter would
        // have been. It is only uncached, which costs NeoForge an identity check, not correctness.
        return new InternalInventoryResourceHandler(inventory);
    }
}
