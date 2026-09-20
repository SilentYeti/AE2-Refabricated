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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;

import appeng.core.AEConfig;
import appeng.core.definitions.AECommonBlocks;
import appeng.core.definitions.AECommonItems;
import appeng.fabric.config.FabricConfigBackend;
import appeng.fabric.resources.PoweredItemEnergyStorage;
import appeng.hooks.LevelUnloadListeners;
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
        // First, as on NeoForge: much of AE2 reads its config while it is being set up
        AEConfig.register(new FabricConfigBackend(FabricLoader.getInstance().getConfigDir(),
                platform.isPhysicalClient()));
        ServerLifecycleEvents.SERVER_STARTING.register(FabricPlatform::setServer);
        // NeoForge's LevelEvent.Unload, which AE2's quantum bridges listen for. Fabric's fires for server levels
        // only, which is where clusters live.
        ServerLevelEvents.UNLOAD.register((server, level) -> LevelUnloadListeners.fire(level));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> FabricPlatform.setServer(null));
        // Blocks first: their BlockItems have to exist before the creative tab is populated, and the
        // stair, slab and wall variants copy the block state of the block they are cut from.
        // Components before the items: an item's default components name component types, and a stack carrying one
        // cannot be read back before its type is in the registry.
        // Key types before anything that can reach AEKeyTypes -- which is most of the storage API
        FabricKeyTypes.register();
        FabricComponents.register();
        FabricParticles.register();
        FabricBlocks.register();
        FabricItems.register();
        // Every registered item that stores AE power, whichever those are as items cross -- so a newly ported tool
        // charges from other mods without anyone remembering to add it here
        PoweredItemEnergyStorage.registerFor(FabricItems.registered().stream()
                .map(BuiltInRegistries.ITEM::getValue).toList());
        FabricRecipes.register();
        LOG.info("AE2 Fabric: registered {} blocks, {} items, {} recipe types, {} component types, plus the "
                + "creative tab",
                FabricBlocks.registered().size(), FabricItems.registered().size(),
                FabricRecipes.registeredCount(), FabricComponents.registeredCount());
        LOG.warn("AE2 on Fabric is still partial: {} more items and {} more blocks need the grid, menu, "
                + "energy or storage seams before they can be registered, and no parts or networks exist yet.",
                AECommonItems.notYetPortable().size(), AECommonBlocks.notYetPortable().size());
    }
}
