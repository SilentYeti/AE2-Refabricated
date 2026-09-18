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

package appeng.fabric;

import appeng.platform.StackWorldBehaviorsPlatform;

/**
 * Fabric side of {@link StackWorldBehaviorsPlatform}.
 * <p>
 * <b>Registers nothing yet, deliberately.</b> The consumers are the import, export and storage buses, the formation and
 * annihilation planes, and the pattern provider, and none of them registers on Fabric. An empty registry is a state AE2
 * already handles -- a key type without a strategy is simply not transferred -- so unlike an unreachable seam this does
 * not throw.
 * <p>
 * That same leniency is why this must be filled in <em>before</em> any of them arrives on Fabric, or they will register
 * and silently move nothing. Stage 4 adds the item and fluid import, export and external storage strategies over
 * {@code Storage<ItemVariant>} / {@code Storage<FluidVariant>}; the placement and pickup strategies are loader-agnostic
 * and should be registered from {@code :common} once they cross.
 */
public class FabricStackWorldBehaviors implements StackWorldBehaviorsPlatform {
    @Override
    public void registerDefaults() {
    }
}
