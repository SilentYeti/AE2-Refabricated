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

import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.CarriedSlotWrapper;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.MenuOnlyInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.util.inv.CarriedItemInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.PlayerInternalInventory;
import appeng.util.inv.SupplierInternalInventory;

/**
 * Exposes AE2's {@link InternalInventory} to NeoForge's transfer API.
 * <p>
 * This is what {@code InternalInventory.toResourceHandler()} did before the interface moved to {@code :common}, and it
 * gives the same answer in every case. Every inventory whose answer was special is recognised here by its own type,
 * which is what lets those inventories themselves be loader-agnostic: only the answer is NeoForge's, not the inventory.
 * {@link ResourceHandlerProvider} remains for the inventories that genuinely are NeoForge's --
 * {@link appeng.api.inventories.PlatformInventoryWrapper} wraps a handler to begin with -- and for an addon that wants
 * to supply its own.
 * <p>
 * Adapters are handed out by identity wherever the old implementation did, because NeoForge caches capability results
 * by identity: a {@link BaseInternalInventory} keeps its adapter in the slot the base class provides, and the
 * inventories that are not one are wrapped afresh, which is what they did before.
 */
public final class NeoForgeInventories {
    private NeoForgeInventories() {
    }

    public static ResourceHandler<ItemResource> resourceHandler(InternalInventory inventory) {
        if (inventory instanceof ResourceHandlerProvider provider) {
            return provider.toResourceHandler();
        }
        if (inventory == InternalInventory.empty() || inventory == UpgradeInventories.empty()) {
            return EmptyResourceHandler.instance();
        }
        if (inventory instanceof MenuOnlyInventory) {
            // Backs menu slots only -- ConfigMenuInventory, which converts between item stacks and AE keys. Exposing
            // it would let another mod write filter entries through the item API, so it refused before the move too.
            throw new UnsupportedOperationException();
        }
        if (inventory instanceof CarriedItemInventory carried) {
            return CarriedSlotWrapper.of(carried.getMenu());
        }
        if (inventory instanceof PlayerInternalInventory player) {
            return PlayerInventoryWrapper.of(player.getPlayerInventory());
        }
        if (inventory instanceof SupplierInternalInventory<?> supplier) {
            // Whatever it delegates to right now -- the point of the class is that this can change
            return resourceHandler(supplier.getDelegate());
        }
        if (inventory instanceof CombinedInternalInventory combined) {
            // In the base class's slot, so the combined handler keeps its identity like any other adapter
            return combined.getOrCreatePlatformAdapter(() -> combine(combined));
        }
        if (inventory instanceof BaseInternalInventory base) {
            return base.getOrCreatePlatformAdapter(() -> new InternalInventoryResourceHandler(base));
        }
        // Every AE2 inventory is one of the above. This is for an addon that implements InternalInventory directly:
        // before the move it had to supply its own adapter, and the generic wrapper is what that adapter would
        // have been. It is only uncached, which costs NeoForge an identity check, not correctness.
        return new InternalInventoryResourceHandler(inventory);
    }

    @SuppressWarnings("unchecked")
    private static ResourceHandler<ItemResource> combine(CombinedInternalInventory combined) {
        var parts = combined.getSubInventories().stream()
                .map(NeoForgeInventories::resourceHandler)
                .toArray(ResourceHandler[]::new);
        return new CombinedResourceHandler<>(parts);
    }
}
