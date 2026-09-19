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

package appeng.core.particles;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import appeng.api.ids.AEConstants;

public final class ParticleTypes {

    private ParticleTypes() {
    }

    public static final ParticleType<ItemParticleOption> CRAFTING = new ParticleType<>(false) {
        @Override
        public MapCodec<ItemParticleOption> codec() {
            return ItemParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ItemParticleOption> streamCodec() {
            return ItemParticleOption.streamCodec(this);
        }
    };
    public static final ParticleType<EnergyParticleData> ENERGY = new ParticleType<>(false) {
        @Override
        public MapCodec<EnergyParticleData> codec() {
            return EnergyParticleData.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, EnergyParticleData> streamCodec() {
            return EnergyParticleData.STREAM_CODEC;
        }
    };
    public static final ParticleType<LightningArcParticleData> LIGHTNING_ARC = new ParticleType<>(false) {
        @Override
        public MapCodec<LightningArcParticleData> codec() {
            return LightningArcParticleData.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, LightningArcParticleData> streamCodec() {
            return LightningArcParticleData.STREAM_CODEC;
        }
    };
    public static final SimpleParticleType LIGHTNING = new PlainParticleType();
    public static final SimpleParticleType MATTER_CANNON = new PlainParticleType();
    public static final SimpleParticleType VIBRANT = new PlainParticleType();

    /**
     * Every type declared here with its id, in the order they have always been registered, for whichever loader is
     * registering them -- the id table used to live in NeoForge's {@code InitParticleTypes}.
     */
    public static Map<Identifier, ParticleType<?>> all() {
        var all = new LinkedHashMap<Identifier, ParticleType<?>>();
        all.put(AEConstants.makeId("crafting"), CRAFTING);
        all.put(AEConstants.makeId("energy_fx"), ENERGY);
        all.put(AEConstants.makeId("lightning_arc_fx"), LIGHTNING_ARC);
        all.put(AEConstants.makeId("lightning_fx"), LIGHTNING);
        all.put(AEConstants.makeId("matter_cannon_fx"), MATTER_CANNON);
        all.put(AEConstants.makeId("vibrant_fx"), VIBRANT);
        return Collections.unmodifiableMap(all);
    }

    /**
     * {@link SimpleParticleType}'s constructor is protected in vanilla, and was only reachable through an access
     * transformer, which {@code :common} does not have. A subclass may call it -- the same answer as
     * {@code AEStairBlock}.
     */
    private static final class PlainParticleType extends SimpleParticleType {
        PlainParticleType() {
            super(false);
        }
    }

}
