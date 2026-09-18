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

/**
 * Supplies AE2's own default import, export, external storage, placement and pickup strategies to
 * {@code StackWorldBehaviors}. The strategies that reach other blocks' inventories are written against the loader's
 * transfer API, so the loader decides which ones exist.
 * <p>
 * {@link #registerDefaults()} is called from {@code StackWorldBehaviors}' static initializer, which is where the
 * defaults used to be registered directly. That timing matters: its register methods keep the first registration for a
 * key type, so registering AE2's defaults before anything else can touch the class is what makes them take precedence
 * over an addon's. Implementations call back into {@code StackWorldBehaviors.register*}.
 */
public interface StackWorldBehaviorsPlatform {
    void registerDefaults();

    // --- lookup ---

    static StackWorldBehaviorsPlatform get() {
        var instance = Holder0.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("No " + StackWorldBehaviorsPlatform.class.getName()
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
        private static final StackWorldBehaviorsPlatform INSTANCE = ServiceLoader
                .load(StackWorldBehaviorsPlatform.class, StackWorldBehaviorsPlatform.class.getClassLoader())
                .findFirst()
                .orElse(null);

        private Holder0() {
        }
    }
}
