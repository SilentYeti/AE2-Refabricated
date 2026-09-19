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

package appeng.fabric.gametest;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.config.PowerUnit;
import appeng.api.ids.AEItemIds;
import appeng.api.implementations.items.IAEItemPowerStorage;

/**
 * Energy on Fabric: that Team Reborn Energy is loaded from inside AE2's jar, and that an AE2 powered item in a player's
 * inventory takes charge through it the way NeoForge's energy capability charges it -- converted at the Forge Energy
 * rate, stuck to the item in its slot, undone by an aborted transaction, and never drained.
 */
final class EnergyChecks {
    private static final int SLOT = 34;

    private EnergyChecks() {
    }

    static int run(MinecraftServer server) {
        check(FabricLoader.getInstance().isModLoaded("team_reborn_energy"),
                "Team Reborn Energy should load from inside AE2's jar");

        var staffItem = BuiltInRegistries.ITEM.getValue(AEItemIds.CHARGED_STAFF);
        check(staffItem instanceof IAEItemPowerStorage, "the charged staff should be registered on Fabric");
        var powered = (IAEItemPowerStorage) staffItem;

        var player = server.getPlayerList().getPlayers().getFirst();
        var inventory = player.getInventory();
        var previous = inventory.getItem(SLOT);
        try {
            inventory.setItem(SLOT, new ItemStack(staffItem));
            var slot = PlayerInventoryStorage.of(player).getSlots().get(SLOT);
            var energy = ContainerItemContext.ofPlayerSlot(player, slot).find(EnergyStorage.ITEM);
            check(energy != null, "an AE2 powered item should expose Team Reborn Energy");

            var capacityAE = powered.getAEMaxPower(inventory.getItem(SLOT));
            check(energy.getCapacity() == (long) PowerUnit.AE.convertTo(PowerUnit.FE, capacityAE),
                    "capacity should be the AE capacity at the Forge Energy rate; got " + energy.getCapacity());
            check(energy.getAmount() == 0, "a fresh staff should be empty");

            try (var tx = Transaction.openOuter()) {
                check(energy.insert(1000, tx) == 1000, "1000 E should go in");
                // not committed
            }
            check(powered.getAECurrentPower(inventory.getItem(SLOT)) == 0,
                    "an aborted charge must leave the staff empty");

            try (var tx = Transaction.openOuter()) {
                check(energy.insert(1000, tx) == 1000, "1000 E should go in");
                tx.commit();
            }
            var expectedAE = PowerUnit.FE.convertTo(PowerUnit.AE, 1000);
            check(powered.getAECurrentPower(inventory.getItem(SLOT)) == expectedAE,
                    "the staff in its slot should now hold " + expectedAE + " AE; holds "
                            + powered.getAECurrentPower(inventory.getItem(SLOT)));

            try (var tx = Transaction.openOuter()) {
                check(!energy.supportsExtraction() && energy.extract(1000, tx) == 0,
                        "AE2 tools cannot be drained by other mods");
            }

            long capacity = energy.getCapacity();
            try (var tx = Transaction.openOuter()) {
                long accepted = energy.insert(Long.MAX_VALUE / 4, tx);
                check(accepted > 0 && accepted <= capacity, "an overfull charge should stop at capacity; took "
                        + accepted);
                tx.commit();
            }
            check(powered.getAECurrentPower(inventory.getItem(SLOT)) <= capacityAE, "never above capacity");
        } finally {
            inventory.setItem(SLOT, previous);
        }
        return 6;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("AE2 energy check failed: " + message);
        }
    }
}
