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

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import appeng.api.inventories.InternalInventory;

/**
 * Implemented by an {@link InternalInventory} whose NeoForge adapter is not the generic wrapper -- one that already
 * <em>is</em> a NeoForge handler underneath, is empty, stands for the player's own inventory, and so on.
 * <p>
 * This used to be {@code InternalInventory.toResourceHandler()}, abstract on the interface itself. It moved here when
 * the interface moved to {@code :common}, which cannot name NeoForge's types. Everything else is adapted by
 * {@link NeoForgeInventories#resourceHandler}.
 */
public interface ResourceHandlerProvider {
    ResourceHandler<ItemResource> toResourceHandler();
}
