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

import java.nio.file.Files;
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
        packs.add(pack("ae2_common", "/pack.mcmeta", 1, "assets/ae2"));
        packs.add(pack("ae2", "/META-INF/neoforge.mods.toml", 2, "ae2.mixins.json"));

        ReloadableResourceManager resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
        resourceManager.createReload(Runnable::run, Runnable::run, CompletableFuture.supplyAsync(() -> Unit.INSTANCE),
                packs);
        return Mockito.spy(resourceManager);
    }

    /**
     * Neither marker is unique on the test classpath -- the Minecraft jar also carries a {@code pack.mcmeta} and
     * GuideME a {@code neoforge.mods.toml} -- so every candidate is resolved and the first one that is a directory
     * containing {@code proof} wins. Picking whichever the class loader happened to return first would silently mount
     * the wrong root, or blow up on a {@code jar:} URL.
     *
     * @param marker   A resource that exists in the root being looked for.
     * @param levelsUp How far the root sits above that marker.
     * @param proof    A path that exists relative to the root, used to recognize it.
     */
    private static PathPackResources pack(String id, String marker, int levelsUp, String proof) {
        var candidates = new ArrayList<Path>();
        try {
            var urls = MockResourceManager.class.getClassLoader()
                    .getResources(marker.substring(1))
                    .asIterator();
            while (urls.hasNext()) {
                var url = urls.next();
                if (!"file".equals(url.getProtocol())) {
                    continue; // Inside a jar, so not one of our source roots
                }

                var root = Paths.get(url.toURI());
                for (var i = 0; i < levelsUp; i++) {
                    root = root.getParent();
                }
                candidates.add(root);
                if (Files.exists(root.resolve(proof))) {
                    return new PathPackResources(
                            new PackLocationInfo(id, Component.literal("AE2"), PackSource.BUILT_IN, Optional.empty()),
                            root);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to locate the resource root containing " + marker, e);
        }

        throw new IllegalStateException("Couldn't find a resource root via marker " + marker
                + " that contains " + proof + ". Candidates: " + candidates);
    }
}
