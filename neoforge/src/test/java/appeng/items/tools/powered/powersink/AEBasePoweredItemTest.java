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

package appeng.items.tools.powered.powersink;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;

import appeng.util.BootstrapMinecraft;

/**
 * {@link AEBasePoweredItem#shouldCauseReequipAnimation} overrides NeoForge's hook without saying so, so that the class
 * compiles in {@code :common}. Nothing would notice if NeoForge changed the hook's signature: this one would quietly
 * become an unrelated method and every charging tool would start bobbing again. So the override is checked here.
 */
@BootstrapMinecraft
class AEBasePoweredItemTest {
    @Test
    void theReequipHookStillOverridesNeoForges() throws Exception {
        var neoforge = IItemExtension.class.getMethod("shouldCauseReequipAnimation", ItemStack.class,
                ItemStack.class, boolean.class);
        var ours = AEBasePoweredItem.class.getMethod(neoforge.getName(), neoforge.getParameterTypes());

        assertThat(ours.getDeclaringClass()).isEqualTo(AEBasePoweredItem.class);
        assertThat(ours.getReturnType()).isEqualTo(neoforge.getReturnType());
    }
}
