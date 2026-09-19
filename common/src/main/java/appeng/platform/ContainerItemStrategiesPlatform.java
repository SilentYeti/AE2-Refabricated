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
 * Supplies AE2's default container-item strategies to {@code ContainerItemStrategies}: how to see and move what is
 * inside an item that holds something -- a bucket, a tank, another mod's fluid cell. What an item holds is answered by
 * the loader's transfer API, so the loader decides which strategies exist.
 * <p>
 * {@link #registerDefaults()} is called from {@code ContainerItemStrategies}' static initializer, which is where the
 * fluid strategy used to be registered directly, for the reason {@link StackWorldBehaviorsPlatform} gives: its register
 * method keeps the first registration for a key type, so AE2's must be in before anything else can touch the class.
 * Implementations call back into {@code ContainerItemStrategies.register}.
 */
public interface ContainerItemStrategiesPlatform {
    void registerDefaults();

    // --- lookup ---

    static ContainerItemStrategiesPlatform get() {
        var instance = Holder0.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("No " + ContainerItemStrategiesPlatform.class.getName()
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
        private static final ContainerItemStrategiesPlatform INSTANCE = ServiceLoader
                .load(ContainerItemStrategiesPlatform.class, ContainerItemStrategiesPlatform.class.getClassLoader())
                .findFirst()
                .orElse(null);

        private Holder0() {
        }
    }
}
