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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;

import appeng.api.AEBlockCapability;
import appeng.api.AECapabilities;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.storage.MEStorage;
import appeng.util.BootstrapMinecraft;

/**
 * {@link AECapabilities} used to hold NeoForge {@link BlockCapability} objects and now holds handles. Registration and
 * lookup only still meet if each handle resolves to <em>the same object</em> the field used to hold -- a capability
 * that is merely equal would register providers nothing ever finds. These pin that.
 */
@BootstrapMinecraft
class NeoForgeCapabilitiesTest {

    @Test
    void aeCapabilitiesResolveToWhatTheFieldsUsedToHold() {
        // Exactly the calls AECapabilities made before it held handles
        assertThat(NeoForgeCapabilities.of(AECapabilities.ME_STORAGE))
                .isSameAs(BlockCapability.createSided(id("me_storage"), MEStorage.class));
        assertThat(NeoForgeCapabilities.of(AECapabilities.CRAFTING_MACHINE))
                .isSameAs(BlockCapability.createSided(id("crafting_machine"), ICraftingMachine.class));
        assertThat(NeoForgeCapabilities.of(AECapabilities.GENERIC_INTERNAL_INV))
                .isSameAs(BlockCapability.createSided(id("generic_internal_inv"), GenericInternalInventory.class));
        assertThat(NeoForgeCapabilities.of(AECapabilities.CRANKABLE))
                .isSameAs(BlockCapability.createSided(id("crankable"), ICrankable.class));
    }

    @Test
    void theUnsidedCapabilityResolvesToTheVoidContextOne() {
        // NeoForge throws on a context class mismatch, so this also proves NONE maps to void.class
        assertThat(NeoForgeCapabilities.of(AECapabilities.IN_WORLD_GRID_NODE_HOST))
                .isSameAs(BlockCapability.createVoid(id("inworld_gridnode_host"), IInWorldGridNodeHost.class));
    }

    @Test
    void resolvingIsStable() {
        assertThat(NeoForgeCapabilities.of(AECapabilities.ME_STORAGE))
                .isSameAs(NeoForgeCapabilities.of(AECapabilities.ME_STORAGE));
    }

    @Test
    void aNeoForgeCapabilityWrappedAsAHandleResolvesBackToItself() {
        // The P2P tunnels do this with NeoForge's own item, fluid and energy capabilities
        var itemCapability = Capabilities.Item.BLOCK;

        assertThat(NeoForgeCapabilities.of(NeoForgeCapabilities.handle(itemCapability))).isSameAs(itemCapability);
    }

    @Test
    void aFreshHandleWithAnExistingIdFindsThatCapabilityNotACopy() {
        var itemCapability = Capabilities.Item.BLOCK;
        var fresh = AEBlockCapability.sided(itemCapability.name(), itemCapability.typeClass());

        assertThat(NeoForgeCapabilities.of(fresh)).isSameAs(itemCapability);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("ae2", path);
    }
}
