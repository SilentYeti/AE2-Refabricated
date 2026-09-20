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

package appeng.blockentity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.neoforged.neoforge.model.data.ModelData;

import appeng.util.BootstrapMinecraft;

/**
 * {@link AEBaseBlockEntity} is in {@code :common} and cannot name NeoForge's model data, so
 * {@code AEBaseBlockEntityMixin} adds {@code getModelData} to it. If that mixin stopped applying, every AE2 block that
 * draws from model data -- the cable bus, the drive, the crafting cubes -- would quietly fall back to NeoForge's empty
 * data and render wrong, with nothing in the log.
 */
@BootstrapMinecraft
class AEBaseBlockEntityHookTest {
    @Test
    void neoforgesModelDataHookIsMixedIn() throws Exception {
        var method = AEBaseBlockEntity.class.getMethod("getModelData");

        assertThat(method.getDeclaringClass())
                .as("getModelData should come from AEBaseBlockEntity itself, added by AEBaseBlockEntityMixin")
                .isEqualTo(AEBaseBlockEntity.class);
        assertThat(method.getReturnType()).isEqualTo(ModelData.class);
    }
}
