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

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import appeng.api.stacks.AEFluidKey;

/**
 * Fluids between AE2 and Fabric's transfer API: the counterpart of {@code NeoForgeFluids}, plus the one thing NeoForge
 * never needed -- a change of unit.
 * <p>
 * <b>AE2 counts fluid in millibuckets on both loaders</b> ({@link AEFluidKey#AMOUNT_BUCKET}), because that is what is
 * in saved worlds and in every cell. Fabric counts droplets, {@value #DROPLETS_PER_MILLIBUCKET} to the millibucket. So
 * every amount crossing the boundary is converted, and AE2 only ever moves <em>whole</em> millibuckets: a tank holding
 * a third of a bucket (27000 droplets, 333⅓ mB) gives up 333 mB and keeps its last 27 droplets, and a tank with room
 * for 250.5 mB is offered 250. Rounding the other way would make fluid out of nothing or lose it, since AE2 has nowhere
 * to keep a fraction.
 */
public final class FabricFluids {
    public static final long DROPLETS_PER_MILLIBUCKET = FluidConstants.BUCKET / AEFluidKey.AMOUNT_BUCKET;

    static {
        if (DROPLETS_PER_MILLIBUCKET * AEFluidKey.AMOUNT_BUCKET != FluidConstants.BUCKET) {
            throw new IllegalStateException("A bucket is not a whole number of droplets per millibucket");
        }
    }

    private FabricFluids() {
    }

    public static AEFluidKey key(FluidVariant variant) {
        return AEFluidKey.of(variant.typeHolder(), variant.getComponentsPatch());
    }

    public static FluidVariant variant(AEFluidKey key) {
        return FluidVariant.of(key.getFluid(), key.getComponents());
    }

    /** Saturates rather than overflowing: AE2 asks for {@code Long.MAX_VALUE} to mean "as much as there is". */
    public static long toDroplets(long millibuckets) {
        try {
            return Math.multiplyExact(millibuckets, DROPLETS_PER_MILLIBUCKET);
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    /** Whole millibuckets only, rounded down. */
    public static long toMillibuckets(long droplets) {
        return droplets / DROPLETS_PER_MILLIBUCKET;
    }

    /**
     * Extracts up to {@code millibuckets} of {@code variant}, in whole millibuckets only, and answers how many.
     * <p>
     * Asks first how much the storage would give, rounds that down to whole millibuckets, and then extracts exactly
     * that inside a nested transaction. A storage that then gives a different amount -- which a well-behaved one does
     * not -- is rolled back and counted as having given nothing, rather than leaving a fraction unaccounted for.
     */
    public static long extractWholeMillibuckets(Storage<FluidVariant> storage, FluidVariant variant, long millibuckets,
            TransactionContext transaction) {
        long wanted;
        try (var probe = transaction.openNested()) {
            wanted = toMillibuckets(storage.extract(variant, toDroplets(millibuckets), probe));
        }
        return exactly(storage, variant, wanted, transaction, true);
    }

    /**
     * Inserts up to {@code millibuckets} of {@code variant}, in whole millibuckets only, and answers how many; the same
     * rule as {@link #extractWholeMillibuckets}.
     */
    public static long insertWholeMillibuckets(Storage<FluidVariant> storage, FluidVariant variant, long millibuckets,
            TransactionContext transaction) {
        long room;
        try (var probe = transaction.openNested()) {
            room = toMillibuckets(storage.insert(variant, toDroplets(millibuckets), probe));
        }
        return exactly(storage, variant, room, transaction, false);
    }

    private static long exactly(Storage<FluidVariant> storage, FluidVariant variant, long millibuckets,
            TransactionContext transaction, boolean extract) {
        if (millibuckets <= 0) {
            return 0;
        }
        long droplets = toDroplets(millibuckets);
        try (var move = transaction.openNested()) {
            long moved = extract
                    ? storage.extract(variant, droplets, move)
                    : storage.insert(variant, droplets, move);
            if (moved != droplets) {
                return 0; // not committed, so rolled back
            }
            move.commit();
            return millibuckets;
        }
    }

    /**
     * The first fluid in the storage and how many whole millibuckets of it could be extracted, without extracting any.
     */
    @Nullable
    public static FluidAmount firstExtractable(Storage<FluidVariant> storage, TransactionContext transaction) {
        for (var view : storage.nonEmptyViews()) {
            var variant = view.getResource();
            long millibuckets;
            try (var probe = transaction.openNested()) {
                millibuckets = toMillibuckets(view.extract(variant, Long.MAX_VALUE, probe));
            }
            if (millibuckets > 0) {
                return new FluidAmount(key(variant), millibuckets);
            }
        }
        return null;
    }

    public record FluidAmount(AEFluidKey what, long millibuckets) {
    }
}
