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

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import appeng.core.particles.ParticleTypes;

/**
 * Registers AE2's particle types -- all of them, under the ids NeoForge uses, since a type is only a codec and a
 * registry entry. Drawing them needs a provider on the client, which {@code AppEngFabricClient} registers for the ones
 * that can be drawn on Fabric yet.
 */
public final class FabricParticles {
    private FabricParticles() {
    }

    public static void register() {
        ParticleTypes.all().forEach((id, type) -> Registry.register(BuiltInRegistries.PARTICLE_TYPE, id, type));
    }
}
