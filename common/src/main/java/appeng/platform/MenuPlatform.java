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

import java.util.ServiceLoader;
import java.util.function.Consumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Opening a menu with extra data attached, which vanilla cannot do.
 * <p>
 * Vanilla's {@code Player.openMenu(MenuProvider)} carries nothing beyond the menu type, and {@code MenuType} is
 * constructed from a {@code MenuSupplier} that only receives a container id and the player inventory. AE2 needs more
 * than that at construction time -- at minimum the {@code MenuHostLocator} saying what the menu is attached to -- so
 * every menu it opens depends on some loader extension.
 * <p>
 * NeoForge supplies one by injecting {@code IMenuTypeExtension} onto {@code MenuType}. Fabric, as of API 0.157.0+26.2,
 * supplies none: {@code fabric-screen-handler-api-v1} exists on the Fabric maven but is not part of the 26.2 bundle,
 * and no other module has taken the capability over.
 * <p>
 * Everything AE2 needs therefore goes through these two methods, which are the only place in the mod that has to care.
 * Both signatures are vanilla-typed, so this interface can live in {@code :common}.
 */
public interface MenuPlatform {
    /**
     * Builds the menu on the client from the bytes {@link #openMenu} wrote on the server.
     */
    @FunctionalInterface
    interface MenuFromNetwork<M extends AbstractContainerMenu> {
        M create(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data);
    }

    /**
     * A menu type whose client-side constructor receives the extra data.
     */
    <M extends AbstractContainerMenu> MenuType<M> createMenuType(MenuFromNetwork<M> factory);

    /**
     * Opens {@code provider} for {@code player}, with {@code dataWriter} appending the extra data that the matching
     * {@link MenuFromNetwork} will read.
     */
    void openMenu(Player player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> dataWriter);

    // --- lookup ---

    static MenuPlatform get() {
        var instance = Holder.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("No " + MenuPlatform.class.getName()
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
        private static final MenuPlatform INSTANCE = ServiceLoader
                .load(MenuPlatform.class, MenuPlatform.class.getClassLoader())
                .findFirst()
                .orElse(null);

        private Holder() {
        }
    }
}
