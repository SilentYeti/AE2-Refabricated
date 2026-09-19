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

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.fabric.resources.FabricFluids;

/**
 * Fluids on Fabric: AE2's key types, the millibucket/droplet boundary, and fluid in items through
 * {@code ContainerItemStrategies} -- the entry point AE2's own menus use -- against real buckets in a player's
 * inventory.
 */
final class FluidChecks {
    private static final int SLOT = 35; // the last main-inventory slot; the hotbar showcase fills the first ones

    private FluidChecks() {
    }

    static int run(MinecraftServer server) {
        theKeyTypesAreRegistered();
        millibucketsAndDropletsConvert();
        onlyWholeMillibucketsLeaveATank();
        onlyWholeMillibucketsGoIntoATank();
        whatIsInABucketCanBeSeen();
        aBucketInAPlayersInventoryEmptiesAndFills(server);
        return 6;
    }

    private static void theKeyTypesAreRegistered() {
        check(AEKeyTypes.getAll().contains(AEKeyType.items()) && AEKeyTypes.getAll().contains(AEKeyType.fluids()),
                "both built-in key types should be registered; got " + AEKeyTypes.getAll());
        check(ContainerItemStrategies.isTypeSupported(AEKeyType.fluids()),
                "fluids in items should have a strategy on Fabric");
    }

    private static void millibucketsAndDropletsConvert() {
        check(FabricFluids.toDroplets(AEFluidKey.AMOUNT_BUCKET) == FluidConstants.BUCKET,
                "a bucket in millibuckets should be a bucket in droplets");
        check(FabricFluids.toMillibuckets(FluidConstants.BUCKET / 3) == 333, "a third of a bucket is 333 whole mB");
        check(FabricFluids.toDroplets(Long.MAX_VALUE) == Long.MAX_VALUE, "'everything' must not overflow");
    }

    private static void onlyWholeMillibucketsLeaveATank() {
        var tank = tank(FluidConstants.BUCKET);
        tank.variant = FluidVariant.of(Fluids.WATER);
        tank.amount = FluidConstants.BUCKET / 3; // 333 1/3 mB

        try (var tx = Transaction.openOuter()) {
            long got = FabricFluids.extractWholeMillibuckets(tank, tank.variant, 1000, tx);
            check(got == 333, "a third of a bucket should give 333 mB, gave " + got);
            tx.commit();
        }
        check(tank.amount == FluidConstants.BUCKET / 3 - 333 * FabricFluids.DROPLETS_PER_MILLIBUCKET,
                "the fraction of a millibucket should stay in the tank; left " + tank.amount + " droplets");
    }

    private static void onlyWholeMillibucketsGoIntoATank() {
        // Room for 250 and a half millibuckets
        var tank = tank(250 * FabricFluids.DROPLETS_PER_MILLIBUCKET + FabricFluids.DROPLETS_PER_MILLIBUCKET / 2);

        try (var tx = Transaction.openOuter()) {
            long put = FabricFluids.insertWholeMillibuckets(tank, FluidVariant.of(Fluids.WATER), 1000, tx);
            check(put == 250, "room for 250.5 mB should take 250, took " + put);
            tx.commit();
        }
        check(tank.amount == 250 * FabricFluids.DROPLETS_PER_MILLIBUCKET,
                "exactly 250 mB should be in the tank; there are " + tank.amount + " droplets");
    }

    private static void whatIsInABucketCanBeSeen() {
        var water = ContainerItemStrategies.getContainedStack(new ItemStack(Items.WATER_BUCKET));
        check(water != null && water.equals(new GenericStack(AEFluidKey.of(Fluids.WATER), AEFluidKey.AMOUNT_BUCKET)),
                "a water bucket should hold 1000 mB of water; got " + water);
        check(ContainerItemStrategies.getContainedStack(new ItemStack(Items.BUCKET)) == null,
                "an empty bucket holds nothing");
        check(ContainerItemStrategies.getContainedStack(new ItemStack(Items.DIRT)) == null, "dirt holds nothing");
    }

    private static void aBucketInAPlayersInventoryEmptiesAndFills(MinecraftServer server) {
        var player = server.getPlayerList().getPlayers().getFirst();
        var inventory = player.getInventory();
        var previous = inventory.getItem(SLOT);
        try {
            var water = AEFluidKey.of(Fluids.WATER);
            var lava = AEFluidKey.of(Fluids.LAVA);

            inventory.setItem(SLOT, new ItemStack(Items.WATER_BUCKET));
            var context = ContainerItemStrategies.findOwnedItemContext(AEKeyType.fluids(), player,
                    inventory.getItem(SLOT));
            check(context != null, "a water bucket in the inventory should have a fluid context");
            check(context.extract(water, 1000, Actionable.SIMULATE) == 1000, "1000 mB should be extractable");
            check(inventory.getItem(SLOT).is(Items.WATER_BUCKET), "a simulation must change nothing");
            check(context.extract(water, 1000, Actionable.MODULATE) == 1000, "1000 mB should be extracted");
            check(inventory.getItem(SLOT).is(Items.BUCKET), "the water bucket should now be empty");

            context = ContainerItemStrategies.findOwnedItemContext(AEKeyType.fluids(), player,
                    inventory.getItem(SLOT));
            check(context != null, "an empty bucket should have a fluid context");
            check(context.insert(lava, 1000, Actionable.MODULATE) == 1000, "1000 mB of lava should go in");
            check(inventory.getItem(SLOT).is(Items.LAVA_BUCKET), "the bucket should now hold lava");

            context = ContainerItemStrategies.findOwnedItemContext(AEKeyType.fluids(), player,
                    inventory.getItem(SLOT));
            check(context.extract(lava, 500, Actionable.MODULATE) == 0,
                    "a bucket gives all or nothing, so half of one should come out as nothing");
            var content = context.getExtractableContent();
            check(content != null && content.equals(new GenericStack(lava, 1000)),
                    "the lava bucket's extractable content should be 1000 mB of lava; got " + content);
        } finally {
            inventory.setItem(SLOT, previous);
        }
    }

    private static SingleVariantStorage<FluidVariant> tank(long capacity) {
        return new SingleVariantStorage<>() {
            @Override
            protected FluidVariant getBlankVariant() {
                return FluidVariant.blank();
            }

            @Override
            protected long getCapacity(FluidVariant variant) {
                return capacity;
            }
        };
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("AE2 fluid check failed: " + message);
        }
    }
}
