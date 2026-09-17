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

import appeng.core.definitions.AECommonItems;
import appeng.platform.AEPlatform;

/**
 * Fabric entrypoint.
 * <p>
 * Starts the part of AE2 that is loader-agnostic today: the items declared in {@link AECommonItems} and the creative
 * tab holding them. The rest of the mod's content still lives in {@code :neoforge} bound to NeoForge APIs (capabilities
 * and the transfer API in particular), and moves across as each of those gets an abstraction.
 */
public class AppEngFabric implements ModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("AE2");

    @Override
    public void onInitialize() {
        var platform = AEPlatform.get();
        LOG.info("AE2 Fabric: platform SPI resolved -- loader={}, dev={}, client={}",
                platform.loader(), platform.isDevelopmentEnvironment(), platform.isPhysicalClient());
        FabricItems.register();
        LOG.info("AE2 Fabric: registered {} items and the creative tab", FabricItems.registered().size());
        LOG.warn("AE2 on Fabric is still partial: {} more of AE2's items need the grid, menu, energy or "
                + "storage seams before they can be registered, and no blocks, parts or networks exist yet.",
                AECommonItems.notYetPortable().size());
    }
}
