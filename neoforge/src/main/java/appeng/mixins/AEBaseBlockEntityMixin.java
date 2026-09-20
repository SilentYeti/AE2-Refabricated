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

package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.neoforged.neoforge.model.data.ModelData;

import appeng.api.client.AEModelData;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.neoforge.model.NeoForgeModelData;

/**
 * Gives {@link AEBaseBlockEntity} NeoForge's {@code getModelData}, which it cannot declare itself.
 * <p>
 * The block entity lives in {@code :common} and hands out AE2's own {@link AEModelData}. NeoForge asks for its model
 * data through a method whose <em>return type</em> is NeoForge's, so unlike the item hooks it cannot simply be declared
 * without {@code @Override} and left to match by signature. The method is added here instead, where NeoForge's types
 * can be named, and overrides {@code BlockEntity.getModelData} once mixed in.
 * <p>
 * Fabric's hook, {@code RenderDataBlockEntity.getRenderData}, is an interface of Fabric's and wants the same treatment:
 * a mixin in {@code :fabric} implementing it and answering with {@code getAEModelData}. It cannot be written yet --
 * {@code :fabric} sees only {@code :common}, and this class has not crossed -- so write it when it does, or AE2's
 * blocks will draw on Fabric with no model data at all.
 * <p>
 * {@code AEBaseBlockEntityHookTest} fails if this stops being applied -- nothing else would notice, and every AE2 block
 * that draws from model data would quietly lose it.
 */
@Mixin(AEBaseBlockEntity.class)
public abstract class AEBaseBlockEntityMixin {
    @Shadow
    public abstract AEModelData getAEModelData();

    public ModelData getModelData() {
        return NeoForgeModelData.of(getAEModelData());
    }
}
