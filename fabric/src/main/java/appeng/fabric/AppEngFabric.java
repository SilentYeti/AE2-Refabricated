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

package appeng.fabric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

import appeng.platform.AEPlatform;

/**
 * Fabric entrypoint.
 * <p>
 * This does not yet start AE2: the mod's content lives in {@code :neoforge} and is still bound to NeoForge
 * APIs (registries, capabilities and the transfer API in particular). What this proves today is that the
 * multiloader wiring is sound -- the Fabric jar builds, loads, bundles {@code :common} and the shared
 * assets, and resolves the platform SPI. Content moves across as each NeoForge API gets an abstraction.
 */
public class AppEngFabric implements ModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("AE2");

    @Override
    public void onInitialize() {
        var platform = AEPlatform.get();
        LOG.info("AE2 Fabric: platform SPI resolved -- loader={}, dev={}, client={}",
                platform.loader(), platform.isDevelopmentEnvironment(), platform.isPhysicalClient());
        LOG.warn("AE2 on Fabric is scaffolding only: no blocks, items or networks are registered yet.");
    }
}
