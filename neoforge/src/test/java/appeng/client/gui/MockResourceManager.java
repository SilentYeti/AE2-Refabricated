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

package appeng.client.gui;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.mockito.Mockito;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;

import appeng.core.AppEng;

/**
 * Fake resource manager that more or less loads AE2 resource pack resources.
 */
public final class MockResourceManager {
    private MockResourceManager() {
    }

    public static ReloadableResourceManager create() {
        // Multiloader: the shared assets/data live in :common and the loader-specific resources in
        // :neoforge, so there are two roots on the test classpath rather than one. Each is located by a
        // file that only exists in it.
        var packs = new ArrayList<PackResources>();
        packs.add(ServerPacksSource.createVanillaPackSource());
        packs.add(pack("ae2_common", "/pack.mcmeta", 1));
        packs.add(pack("ae2", "/META-INF/neoforge.mods.toml", 2));

        ReloadableResourceManager resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
        resourceManager.createReload(Runnable::run, Runnable::run, CompletableFuture.supplyAsync(() -> Unit.INSTANCE),
                packs);
        return Mockito.spy(resourceManager);
    }

    /**
     * @param marker   A resource that exists only in the root being looked for.
     * @param levelsUp How far the root sits above that marker.
     */
    private static PathPackResources pack(String id, String marker, int levelsUp) {
        var markerUrl = AppEng.class.getResource(marker);
        if (markerUrl == null) {
            throw new IllegalStateException("Couldn't find resource root via marker " + marker);
        }

        Path root;
        try {
            root = Paths.get(markerUrl.toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to convert " + markerUrl + " to a path on disk.", e);
        }
        for (var i = 0; i < levelsUp; i++) {
            root = root.getParent();
        }

        return new PathPackResources(
                new PackLocationInfo(id, Component.literal("AE2"), PackSource.BUILT_IN, Optional.empty()),
                root);
    }
}
