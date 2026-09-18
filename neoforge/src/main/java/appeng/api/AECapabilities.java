/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.ids.AEConstants;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.storage.MEStorage;

/**
 * Utility class that holds the capabilities provided by AE2.
 * <p>
 * These are {@link AEBlockCapability} handles rather than a loader's own capability objects, so that code on either
 * loader can look them up. On NeoForge, {@code NeoForgeCapabilities.of(...)} gives the {@code BlockCapability} to
 * register providers against; it is the same object these fields held before they became handles.
 */
public final class AECapabilities {
    private AECapabilities() {
    }

    public static final AEBlockCapability<MEStorage, @Nullable Direction> ME_STORAGE = AEBlockCapability
            .sided(AEConstants.makeId("me_storage"), MEStorage.class);

    public static final AEBlockCapability<ICraftingMachine, @Nullable Direction> CRAFTING_MACHINE = AEBlockCapability
            .sided(AEConstants.makeId("crafting_machine"), ICraftingMachine.class);

    public static final AEBlockCapability<GenericInternalInventory, @Nullable Direction> GENERIC_INTERNAL_INV = AEBlockCapability
            .sided(AEConstants.makeId("generic_internal_inv"), GenericInternalInventory.class);

    public static final AEBlockCapability<IInWorldGridNodeHost, @Nullable Void> IN_WORLD_GRID_NODE_HOST = AEBlockCapability
            .unsided(AEConstants.makeId("inworld_gridnode_host"), IInWorldGridNodeHost.class);

    public static final AEBlockCapability<ICrankable, @Nullable Direction> CRANKABLE = AEBlockCapability
            .sided(AEConstants.makeId("crankable"), ICrankable.class);

}
