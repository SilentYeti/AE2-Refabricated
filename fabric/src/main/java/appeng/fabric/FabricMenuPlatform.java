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

import java.util.function.Consumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import appeng.platform.MenuPlatform;

/**
 * Fabric side of {@link MenuPlatform}.
 * <p>
 * <b>Not implemented yet, deliberately.</b> No menu registers on Fabric, so an implementation here would be dead code
 * that nothing exercises -- and this one cannot be written blind, because it depends on an ordering guarantee that has
 * to be confirmed in a running game.
 * <p>
 * The intended shape, for whoever picks this up:
 * <ul>
 * <li>{@code createMenuType} returns a plain {@code new MenuType<>(supplier, FeatureFlags.VANILLA_SET)} whose supplier
 * reads the pending buffer described below and hands it to the {@link MenuFromNetwork}.</li>
 * <li>{@code openMenu} sends the extra data as an AE2 payload <em>first</em>, then calls vanilla
 * {@code player.openMenu(provider)}. The client stashes the buffer, and the menu supplier consumes and clears it when
 * the open-screen packet that follows constructs the menu.</li>
 * </ul>
 * That works only if the payload is handled before the open-screen packet. Both travel the same connection and both are
 * dispatched to the client thread in arrival order, so it should hold -- but it is an assumption about packet
 * scheduling, not a guarantee the API makes, and it is exactly what {@code :fabric:runClientGametest} should assert
 * once a menu exists to open. Clear the stash on menu close as well as on read, so a failed open cannot leak data into
 * the next one.
 * <p>
 * The alternative is {@code fabric-screen-handler-api-v1}, which provides this properly. It is published on the Fabric
 * maven but is absent from the 0.157.0+26.2 bundle, so it would have to be depended on directly and verified against
 * 26.2 first.
 */
public class FabricMenuPlatform implements MenuPlatform {
    private static final String NOT_YET = "Menus do not open on Fabric yet. See FabricMenuPlatform's javadoc "
            + "for the intended implementation and the packet-ordering assumption it rests on.";

    @Override
    public <M extends AbstractContainerMenu> MenuType<M> createMenuType(MenuFromNetwork<M> factory) {
        throw new UnsupportedOperationException(NOT_YET);
    }

    @Override
    public void openMenu(Player player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> dataWriter) {
        throw new UnsupportedOperationException(NOT_YET);
    }
}
