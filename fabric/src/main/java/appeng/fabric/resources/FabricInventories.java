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

import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.MenuOnlyInventory;
import appeng.util.inv.CarriedItemInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.PlayerInternalInventory;
import appeng.util.inv.SupplierInternalInventory;

/**
 * Exposes AE2's {@link InternalInventory} to Fabric's transfer API, as {@code SlottedStorage<ItemVariant>}.
 * <p>
 * The Fabric twin of {@code NeoForgeInventories.resourceHandler}, case for case, so that an inventory looks the same to
 * other mods on either loader:
 * <ul>
 * <li>an inventory that supplies its own storage ({@link ItemStorageProvider}) is taken at its word;</li>
 * <li>the shared empty inventory is an empty storage;</li>
 * <li>a {@link MenuOnlyInventory} is refused -- it backs menu slots, and exposing it would let another mod write filter
 * entries through the item API;</li>
 * <li>the carried item and the player's inventory are Fabric's own storages over the same thing, as NeoForge uses its
 * own wrappers for them; the player's covers the whole inventory, as NeoForge's does, not only the 36 main slots
 * {@link PlayerInternalInventory} shows;</li>
 * <li>a {@link SupplierInternalInventory} is whatever it currently delegates to;</li>
 * <li>a {@link CombinedInternalInventory} is one combined storage over its parts, and like any
 * {@link BaseInternalInventory} it keeps that storage for as long as it lives;</li>
 * <li>anything else gets the generic {@link InternalInventoryStorage}.</li>
 * </ul>
 * The empty upgrade inventory has no case of its own here, unlike on NeoForge, because the class that hands it out has
 * not crossed yet. It falls through to the generic storage, which over zero slots behaves as an empty one; only the
 * identity differs, which cannot matter for an inventory that never changes. Give it the empty storage when
 * {@code UpgradeInventories} crosses.
 */
public final class FabricInventories {
    private static final SlottedStorage<ItemVariant> EMPTY = new CombinedSlottedStorage<>(List.of());

    private FabricInventories() {
    }

    public static SlottedStorage<ItemVariant> storage(InternalInventory inventory) {
        if (inventory instanceof ItemStorageProvider provider) {
            return provider.toItemStorage();
        }
        if (inventory == InternalInventory.empty()) {
            return EMPTY;
        }
        if (inventory instanceof MenuOnlyInventory) {
            throw new UnsupportedOperationException(inventory + " backs menu slots and cannot be exposed");
        }
        if (inventory instanceof CarriedItemInventory carried) {
            return PlayerInventoryStorage.getCursorStorage(carried.getMenu());
        }
        if (inventory instanceof PlayerInternalInventory player) {
            return PlayerInventoryStorage.of(player.getPlayerInventory());
        }
        if (inventory instanceof SupplierInternalInventory<?> supplier) {
            // Whatever it delegates to right now -- the point of the class is that this can change
            return storage(supplier.getDelegate());
        }
        if (inventory instanceof CombinedInternalInventory combined) {
            return combined.getOrCreatePlatformAdapter(() -> combine(combined));
        }
        if (inventory instanceof BaseInternalInventory base) {
            return base.getOrCreatePlatformAdapter(() -> new InternalInventoryStorage(base));
        }
        // An addon implementing InternalInventory directly. Uncached, which costs an identity, not correctness.
        return new InternalInventoryStorage(inventory);
    }

    private static SlottedStorage<ItemVariant> combine(CombinedInternalInventory combined) {
        var parts = combined.getSubInventories().stream()
                .map(FabricInventories::storage)
                .toList();
        return new CombinedSlottedStorage<>(parts);
    }
}
