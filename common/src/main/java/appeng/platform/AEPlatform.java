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

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The seam between loader-agnostic code in {@code :common} and the mod loader underneath it.
 * <p>
 * Code in {@code :common} must never import {@code net.neoforged.*} or {@code net.fabricmc.*}. When it needs something
 * only a loader can answer, it asks this interface, and {@code :neoforge} / {@code :fabric} each supply an
 * implementation discovered through {@link ServiceLoader}. Keep this surface small: every method added here has to be
 * written twice.
 */
public interface AEPlatform {
    /**
     * @return Which loader is running.
     */
    Loader loader();

    /**
     * @return True when running in a development environment rather than from a packaged jar.
     */
    boolean isDevelopmentEnvironment();

    /**
     * @return True when the given mod id is present.
     */
    boolean isModLoaded(String modId);

    /**
     * @return The version of the given mod, or empty if it is not loaded.
     */
    Optional<String> getModVersion(String modId);

    /**
     * @return True on the physical client; false on a dedicated server.
     */
    boolean isPhysicalClient();

    enum Loader {
        NEOFORGE,
        FABRIC
    }

    // --- lookup ---

    /**
     * Resolved once on first use. Fails loudly rather than degrading, because an absent implementation means the loader
     * module was not bundled and nothing downstream can work.
     */
    static AEPlatform get() {
        var instance = Holder.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("No " + AEPlatform.class.getName()
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
    final class Holder {
        private static final AEPlatform INSTANCE = ServiceLoader
                .load(AEPlatform.class, AEPlatform.class.getClassLoader())
                .findFirst()
                .orElse(null);

        private Holder() {
        }
    }
}
