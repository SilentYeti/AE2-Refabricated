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

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKeyType;
import appeng.platform.ContainerItemStrategiesPlatform;

/**
 * NeoForge side of {@link ContainerItemStrategiesPlatform}: the registration {@link ContainerItemStrategies}' static
 * initializer used to make itself. Registered in META-INF/services.
 */
public class NeoForgeContainerItemStrategies implements ContainerItemStrategiesPlatform {
    @Override
    public void registerDefaults() {
        ContainerItemStrategies.register(AEKeyType.fluids(), AEFluidKey.class,
                new NeoForgeFluidContainerItemStrategy());
    }
}
