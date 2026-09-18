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

package appeng.parts.automation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import appeng.api.stacks.AEKeyType;
import appeng.util.BootstrapMinecraft;

/**
 * AE2's default strategies now come from the loader through {@code StackWorldBehaviorsPlatform} instead of being named
 * in {@link StackWorldBehaviors}' static initializer. On NeoForge that must still register items and fluids for every
 * kind of strategy, or every bus and plane would silently move nothing.
 */
@BootstrapMinecraft
class StackWorldBehaviorsDefaultsTest {
    @Test
    void itemsAndFluidsHaveEveryKindOfStrategy() {
        var both = new AEKeyType[] { AEKeyType.items(), AEKeyType.fluids() };

        assertThat(StackWorldBehaviors.withImportStrategy()).containsExactlyInAnyOrder(both);
        assertThat(StackWorldBehaviors.withExportStrategy()).containsExactlyInAnyOrder(both);
        assertThat(StackWorldBehaviors.withPlacementStrategy()).containsExactlyInAnyOrder(both);
    }
}
