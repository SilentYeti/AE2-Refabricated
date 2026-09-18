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

package appeng.blockentity.crafting;

import java.util.EnumSet;

import net.minecraft.core.Direction;

import appeng.api.client.AEModelData;
import appeng.api.client.AEModelProperty;

public final class CraftingCubeModelData {

    // Contains information on which sides of the block are connected to other parts
    // of a formed crafting cube
    public static final AEModelProperty<EnumSet<Direction>> CONNECTIONS = new AEModelProperty<>();

    private CraftingCubeModelData() {
    }

    public static AEModelData.Builder builder(EnumSet<Direction> connections) {
        return AEModelData.builder()
                .with(CONNECTIONS, connections);
    }

    public static AEModelData create(EnumSet<Direction> connections) {
        return builder(connections).build();
    }
}
