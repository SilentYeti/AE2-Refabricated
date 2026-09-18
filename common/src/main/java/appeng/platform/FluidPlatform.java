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

package appeng.platform;

import java.util.ServiceLoader;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

/**
 * The two questions about a fluid that vanilla cannot answer.
 * <p>
 * {@link appeng.api.stacks.AEFluidKey} identifies a fluid as a {@link Holder} plus a {@link DataComponentPatch}, which
 * are vanilla types, so the key itself is loader-agnostic. But vanilla has no notion of a fluid having a display name
 * or default data components at all -- those are things each loader adds on top, NeoForge through {@code FluidType} and
 * {@code FluidStack}, Fabric through {@code FluidVariantAttributes}. Rather than pick one loader's answer and make the
 * other one wrong, the key asks here.
 *
 * @see AEPlatform for the rules this seam follows
 */
public interface FluidPlatform {
    /**
     * The name to show a player, e.g. "Water".
     */
    Component getDisplayName(Holder<Fluid> fluid, DataComponentPatch components);

    /**
     * Reads a data component off a fluid.
     * <p>
     * This is not simply {@code components.get(type)}: a loader may give a fluid <em>default</em> components that the
     * patch then overrides, and the patch alone cannot see those. Asking the loader keeps a fluid's components meaning
     * the same thing here as everywhere else on that loader.
     */
    <T> @Nullable T getComponent(Holder<Fluid> fluid, DataComponentPatch components, DataComponentType<T> type);

    // --- lookup ---

    static FluidPlatform get() {
        var instance = Holder0.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("No " + FluidPlatform.class.getName()
                    + " implementation was found on the classpath. The :neoforge or :fabric module must be "
                    + "present and register one via META-INF/services.");
        }
        return instance;
    }

    /**
     * Loads through this interface's own class loader, not the thread's context loader, which is what
     * {@code ServiceLoader.load(Class)} would use. Under a mod loader the context loader is not reliably the one that
     * loaded AE2: when it is not, the implementation gets defined a second time by the wrong loader and its first
     * reference back into AE2 fails with a {@code LinkageError} -- depending only on which thread happened to touch
     * this first.
     */
    final class Holder0 {
        private static final FluidPlatform INSTANCE = ServiceLoader
                .load(FluidPlatform.class, FluidPlatform.class.getClassLoader())
                .findFirst()
                .orElse(null);

        private Holder0() {
        }
    }
}
