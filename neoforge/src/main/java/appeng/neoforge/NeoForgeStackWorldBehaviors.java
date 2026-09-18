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

package appeng.neoforge;

import appeng.api.stacks.AEKeyType;
import appeng.parts.automation.FluidPickupStrategy;
import appeng.parts.automation.FluidPlacementStrategy;
import appeng.parts.automation.ForgeExternalStorageStrategy;
import appeng.parts.automation.ItemPickupStrategy;
import appeng.parts.automation.ItemPlacementStrategy;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.parts.automation.StorageExportStrategy;
import appeng.parts.automation.StorageImportStrategy;
import appeng.platform.StackWorldBehaviorsPlatform;

/**
 * NeoForge side of {@link StackWorldBehaviorsPlatform}: the registrations {@link StackWorldBehaviors}' static
 * initializer used to make itself, in the same order. Registered in META-INF/services.
 * <p>
 * The placement and pickup strategies are not loader-specific and belong in {@code :common}'s list once they can cross;
 * they are here only because they are still in {@code :neoforge}.
 */
public class NeoForgeStackWorldBehaviors implements StackWorldBehaviorsPlatform {
    @Override
    public void registerDefaults() {
        StackWorldBehaviors.registerImportStrategy(AEKeyType.items(), StorageImportStrategy::createItem);
        StackWorldBehaviors.registerImportStrategy(AEKeyType.fluids(), StorageImportStrategy::createFluid);
        StackWorldBehaviors.registerExportStrategy(AEKeyType.items(), StorageExportStrategy::createItem);
        StackWorldBehaviors.registerExportStrategy(AEKeyType.fluids(), StorageExportStrategy::createFluid);
        StackWorldBehaviors.registerExternalStorageStrategy(AEKeyType.items(),
                ForgeExternalStorageStrategy::createItem);
        StackWorldBehaviors.registerExternalStorageStrategy(AEKeyType.fluids(),
                ForgeExternalStorageStrategy::createFluid);
        StackWorldBehaviors.registerPlacementStrategy(AEKeyType.fluids(), FluidPlacementStrategy::new);
        StackWorldBehaviors.registerPlacementStrategy(AEKeyType.items(), ItemPlacementStrategy::new);
        StackWorldBehaviors.registerPickupStrategy(AEKeyType.fluids(), (level, pos, side, host, enchantments,
                owningPlayerId) -> new FluidPickupStrategy(level, pos, side, host, enchantments, owningPlayerId));
        StackWorldBehaviors.registerPickupStrategy(AEKeyType.items(), (level, pos, side, host, enchantments,
                owningPlayerId) -> new ItemPickupStrategy(level, pos, side, host, enchantments, owningPlayerId));
    }
}
