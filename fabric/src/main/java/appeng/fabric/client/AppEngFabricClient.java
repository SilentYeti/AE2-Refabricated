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

package appeng.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

import appeng.client.render.effects.CraftingParticle;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.MatterCannonFX;
import appeng.client.render.effects.VibrantFX;
import appeng.core.particles.ParticleTypes;

/**
 * Fabric client entrypoint.
 * <p>
 * Registers the particle providers that can be drawn on Fabric: the same factories NeoForge's {@code AppEngClient}
 * registers, through Fabric's registry instead of NeoForge's event. The two lightning effects are left out on purpose:
 * they draw through a render type of AE2's own, which needs an access widener and a render pipeline -- stage 7 -- and
 * nothing that spawns them registers on Fabric yet.
 */
public class AppEngFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        var particles = ParticleProviderRegistry.getInstance();
        particles.register(ParticleTypes.CRAFTING, CraftingParticle.Factory::new);
        particles.register(ParticleTypes.ENERGY, EnergyFx.Factory::new);
        particles.register(ParticleTypes.MATTER_CANNON, MatterCannonFX.Factory::new);
        particles.register(ParticleTypes.VIBRANT, VibrantFX.Factory::new);
    }
}
