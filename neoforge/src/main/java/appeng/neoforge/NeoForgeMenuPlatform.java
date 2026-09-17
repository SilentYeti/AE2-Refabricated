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

import java.util.function.Consumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import appeng.platform.MenuPlatform;

/**
 * NeoForge side of {@link MenuPlatform}, which is a direct pass-through: NeoForge injects {@code IMenuTypeExtension}
 * onto {@code MenuType} and overloads {@code openMenu} with a data writer, so both methods are the calls AE2 made
 * before this interface existed.
 */
public class NeoForgeMenuPlatform implements MenuPlatform {
    @Override
    public <M extends AbstractContainerMenu> MenuType<M> createMenuType(MenuFromNetwork<M> factory) {
        return IMenuTypeExtension.create(factory::create);
    }

    @Override
    public void openMenu(Player player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> dataWriter) {
        player.openMenu(provider, dataWriter::accept);
    }
}
