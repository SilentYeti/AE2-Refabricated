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

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypesInternal;

/**
 * Creates AE2's key-type registry on Fabric and registers the two built-in types -- what NeoForge's {@code AppEngBase}
 * does from its {@code NewRegistryEvent} and {@code RegisterEvent}.
 * <p>
 * Synced, as on NeoForge, so client and server agree on the ids. NeoForge also caps the registry at 127 entries,
 * because a key type's id travels as a byte; Fabric's builder has no cap, so the same limit is checked here instead of
 * being assumed.
 */
public final class FabricKeyTypes {
    private static final int MAX_ID = 127;

    private FabricKeyTypes() {
    }

    public static void register() {
        var registry = FabricRegistryBuilder.create(AEKeyType.REGISTRY_KEY)
                .attribute(RegistryAttribute.SYNCED)
                .buildAndRegister();
        AEKeyTypesInternal.setRegistry(registry);
        Registry.register(registry, AEKeyType.items().getId(), AEKeyType.items());
        Registry.register(registry, AEKeyType.fluids().getId(), AEKeyType.fluids());
        if (registry.size() > MAX_ID + 1) {
            throw new IllegalStateException("AE2 key types travel as a byte; " + registry.size() + " is too many");
        }
    }
}
