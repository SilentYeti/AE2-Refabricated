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

package appeng.neoforge.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;

import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.PlatformInventoryWrapper;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.BootstrapMinecraft;
import appeng.util.ConfigMenuInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.SupplierInternalInventory;

/**
 * {@link NeoForgeInventories} replaced {@code InternalInventory.toResourceHandler()}, which each inventory used to
 * implement itself. These pin the answers those implementations gave, so the move changed where the code lives and
 * nothing about what NeoForge sees.
 */
@BootstrapMinecraft
class NeoForgeInventoriesTest {

    @Test
    void anInventoryHandsOutTheSameAdapterEveryTime() {
        // NeoForge caches capability results by identity, which is why BaseInternalInventory keeps the adapter
        var inv = new AppEngInternalInventory(3);

        var first = NeoForgeInventories.resourceHandler(inv);

        assertThat(first).isInstanceOf(InternalInventoryResourceHandler.class);
        assertThat(NeoForgeInventories.resourceHandler(inv)).isSameAs(first);
    }

    @Test
    void differentInventoriesGetDifferentAdapters() {
        assertThat(NeoForgeInventories.resourceHandler(new AppEngInternalInventory(1)))
                .isNotSameAs(NeoForgeInventories.resourceHandler(new AppEngInternalInventory(1)));
    }

    @Test
    void theEmptyInventoryIsNeoForgesEmptyHandler() {
        assertThat(NeoForgeInventories.resourceHandler(InternalInventory.empty()))
                .isSameAs(EmptyResourceHandler.instance());
    }

    @Test
    void aWrappedNeoForgeHandlerIsUnwrappedNotWrappedTwice() {
        var handler = NeoForgeInventories.resourceHandler(new AppEngInternalInventory(2));

        assertThat(NeoForgeInventories.resourceHandler(new PlatformInventoryWrapper(handler))).isSameAs(handler);
    }

    @Test
    void aSupplierInventoryAdaptsWhateverItCurrentlyDelegatesTo() {
        var delegate = new AppEngInternalInventory(2);

        assertThat(NeoForgeInventories.resourceHandler(new SupplierInternalInventory<>(() -> delegate)))
                .isSameAs(NeoForgeInventories.resourceHandler(delegate));
    }

    @Test
    void aCombinedInventoryIsOneCombinedHandlerKeptLikeAnyOther() {
        var combined = new CombinedInternalInventory(new AppEngInternalInventory(1), new AppEngInternalInventory(2));

        var first = NeoForgeInventories.resourceHandler(combined);

        assertThat(first).isInstanceOf(CombinedResourceHandler.class);
        assertThat(first.size()).isEqualTo(3);
        assertThat(NeoForgeInventories.resourceHandler(combined)).isSameAs(first);
    }

    @Test
    void aConfigMenuInventoryStillRefusesToBeExposed() {
        var inv = new ConfigMenuInventory(new GenericStackInv(null, 1));

        assertThrows(UnsupportedOperationException.class, () -> NeoForgeInventories.resourceHandler(inv));
    }
}
