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

package appeng.fabric.resources;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnit;
import appeng.api.implementations.items.IAEItemPowerStorage;

/**
 * Exposes an AE2 powered item -- a charged staff, a portable cell -- as Team Reborn Energy, so other mods' chargers can
 * fill it: the twin of NeoForge's {@code PoweredItemCapabilities}, decision for decision.
 * <p>
 * Energy is converted at the same rate as Forge Energy, through {@link PowerUnit#FE} and its {@code forgeEnergy} config
 * ratio: Team Reborn Energy is counted one-for-one with Forge Energy by the mods that bridge the two. Insertion only,
 * as on NeoForge -- AE2 never lets another mod drain a tool. The new charge is written to a copy of the stack and
 * exchanged into the item's slot through the {@link ContainerItemContext}, which is how an item's contents change under
 * Fabric's transfer API, and it rolls back with the transaction like any other exchange.
 */
public final class PoweredItemEnergyStorage implements EnergyStorage {
    private final ContainerItemContext context;
    private final Item validItem;
    private final IAEItemPowerStorage item;

    public PoweredItemEnergyStorage(ContainerItemContext context, Item validItem, IAEItemPowerStorage item) {
        this.context = context;
        this.validItem = validItem;
        this.item = item;
    }

    /** Registers this for every item in {@code items} that stores AE power. */
    public static void registerFor(Iterable<Item> items) {
        for (var candidate : items) {
            if (candidate instanceof IAEItemPowerStorage powered) {
                EnergyStorage.ITEM.registerForItems(
                        (stack, context) -> new PoweredItemEnergyStorage(context, candidate, powered), candidate);
            }
        }
    }

    @Override
    public long getAmount() {
        var variant = context.getItemVariant();
        if (!variant.isOf(validItem)) {
            return 0;
        }
        return (long) PowerUnit.AE.convertTo(PowerUnit.FE, item.getAECurrentPower(variant.toStack()));
    }

    @Override
    public long getCapacity() {
        var variant = context.getItemVariant();
        if (!variant.isOf(validItem)) {
            return 0;
        }
        return (long) PowerUnit.AE.convertTo(PowerUnit.FE, item.getAEMaxPower(variant.toStack()));
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        long count = context.getAmount();
        if (count == 0) {
            return 0;
        }
        long amountPerItem = maxAmount / count;
        if (amountPerItem == 0) {
            return 0;
        }

        var variant = context.getItemVariant();
        if (!variant.isOf(validItem)) {
            return 0;
        }

        // Charge a copy, then exchange the copy into the slot
        var amountAE = PowerUnit.FE.convertTo(PowerUnit.AE, maxAmount);
        var charged = variant.toStack();
        double overflowAE = item.injectAEPower(charged, amountAE, Actionable.MODULATE);
        long insertedPerItem = Math.min(amountPerItem,
                (long) PowerUnit.AE.convertTo(PowerUnit.FE, amountAE - overflowAE));

        if (insertedPerItem > 0 && !charged.isEmpty()) {
            return insertedPerItem * context.exchange(ItemVariant.of(charged), count, transaction);
        }
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public String toString() {
        return "PoweredItemEnergyStorage[" + BuiltInRegistries.ITEM.getKey(validItem) + "]";
    }
}
