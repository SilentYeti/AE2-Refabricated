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

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;

import appeng.api.inventories.InternalInventory;

/**
 * An {@link InternalInventory} that supplies its own Fabric item storage, for {@link FabricInventories} to hand out
 * instead of wrapping it.
 * <p>
 * The counterpart of NeoForge's {@code ResourceHandlerProvider}, and for the same two cases: an inventory that is
 * itself a view of a Fabric storage, and an addon's own inventory with its own adapter. None of AE2's own inventories
 * implement it -- {@link FabricInventories} recognises those by their type.
 */
public interface ItemStorageProvider {
    SlottedStorage<ItemVariant> toItemStorage();
}
