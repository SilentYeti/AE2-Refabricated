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

package appeng.neoforge;

import java.util.Optional;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.util.thread.SidedThreadGroups;

import appeng.platform.AEPlatform;

/**
 * NeoForge side of {@link AEPlatform}. Registered in META-INF/services.
 */
public class NeoForgePlatform implements AEPlatform {
    @Override
    public Loader loader() {
        return Loader.NEOFORGE;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        // Mirrors appeng.util.Platform: the null check covers unit tests, which run without a loader.
        var loader = FMLLoader.getCurrentOrNull();
        return loader == null || !loader.isProduction();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Optional<String> getModVersion(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString());
    }

    @Override
    public boolean isPhysicalClient() {
        var loader = FMLLoader.getCurrentOrNull();
        return loader == null || loader.getDist().isClient();
    }

    @Override
    public boolean isServerThread() {
        // What appeng.util.Platform.isServer() answers: FML runs the logical server in its own thread group
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER;
    }
}
