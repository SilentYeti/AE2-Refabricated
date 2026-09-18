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

package appeng.neoforge.model;

import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

import appeng.api.client.AEModelData;

/**
 * Carries an {@link AEModelData} through NeoForge's model data.
 * <p>
 * AE2 used to declare a {@code ModelProperty} per piece of state and put them in NeoForge's {@link ModelData} directly,
 * which put NeoForge's type on {@code IPart} and on every block entity that draws something -- and with it the parts
 * API, the grid node and the cable bus, none of which have anything to do with rendering. AE2 owns the type now and
 * this is the one property NeoForge sees.
 * <p>
 * Nothing is lost in the wrapping: {@code ModelData} defines no equality, so the several properties were never compared
 * against each other's, and AE2 never asked it for its property set.
 */
public final class NeoForgeModelData {
    private static final ModelProperty<AEModelData> AE2 = new ModelProperty<>();

    private NeoForgeModelData() {
    }

    /**
     * Wraps AE2's model data for NeoForge. Empty data stays {@link ModelData#EMPTY} rather than becoming a wrapper
     * around nothing, which is what a block entity with no extra state returned before.
     */
    public static ModelData of(AEModelData data) {
        return data.isEmpty() ? ModelData.EMPTY : ModelData.of(AE2, data);
    }

    /**
     * AE2's model data out of NeoForge's, for the models that read it. Anything that is not AE2's -- another mod's
     * block, or one of AE2's with no extra state -- reads as empty, and every property lookup on it answers null, as it
     * did when the properties were simply absent.
     */
    public static AEModelData unwrap(ModelData modelData) {
        var data = modelData.get(AE2);
        return data != null ? data : AEModelData.EMPTY;
    }
}
