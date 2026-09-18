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

package appeng.api;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.platform.BlockCapabilityPlatform;

/**
 * An API that blocks can expose to their neighbours, looked up by position -- what NeoForge calls a block capability
 * and Fabric a block API lookup.
 * <p>
 * This is only a name for one: an id, the type it hands out, and whether the asker says which side it is asking from.
 * Each loader resolves it to its own object, and both key theirs by id, so a handle with the id of an existing loader
 * capability refers to that very capability rather than a copy.
 *
 * @param <A> The API type handed out.
 * @param <C> The context the asker passes: {@code Direction} for sided lookups, {@code Void} (always null) otherwise.
 */
public final class AEBlockCapability<A, C> {
    public enum Context {
        /** The asker passes the side it is asking from. Null means "not from any particular side". */
        SIDED,
        /** No context. */
        NONE
    }

    private final Identifier id;
    private final Class<A> apiClass;
    private final Context context;

    /** The loader's own object for this capability, resolved once. See {@link #getOrCreatePlatformHandle}. */
    private volatile Object platformHandle;

    private AEBlockCapability(Identifier id, Class<A> apiClass, Context context) {
        this.id = Objects.requireNonNull(id);
        this.apiClass = Objects.requireNonNull(apiClass);
        this.context = Objects.requireNonNull(context);
    }

    public static <A> AEBlockCapability<A, @Nullable Direction> sided(Identifier id, Class<A> apiClass) {
        return new AEBlockCapability<>(id, apiClass, Context.SIDED);
    }

    public static <A> AEBlockCapability<A, @Nullable Void> unsided(Identifier id, Class<A> apiClass) {
        return new AEBlockCapability<>(id, apiClass, Context.NONE);
    }

    public Identifier id() {
        return id;
    }

    public Class<A> apiClass() {
        return apiClass;
    }

    public Context context() {
        return context;
    }

    /**
     * @return The API the block at {@code pos} exposes, or null.
     */
    @Nullable
    public A find(Level level, BlockPos pos, C context) {
        return find(level, pos, null, null, context);
    }

    /**
     * As {@link #find(Level, BlockPos, Object)}, for a caller that already has the block's state and block entity and
     * so saves the loader looking them up again.
     */
    @Nullable
    public A find(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        return BlockCapabilityPlatform.get().find(this, level, pos, state, blockEntity, context);
    }

    /**
     * A lookup at a fixed position that is cheap to repeat, for something that asks every tick.
     *
     * @param isValid              Checked on each lookup; while false the cache answers null.
     * @param invalidationListener Called when the answer may have changed, on a loader that tracks that. NeoForge does:
     *                             its caches are only refreshed when told, so this is how a change reaches anything
     *                             caching downstream. Fabric re-queries on every lookup, has nothing to invalidate, and
     *                             never calls it.
     */
    public AEBlockCapabilityCache<A> createCache(ServerLevel level, BlockPos pos, C context, BooleanSupplier isValid,
            Runnable invalidationListener) {
        return BlockCapabilityPlatform.get().createCache(this, level, pos, context, isValid, invalidationListener);
    }

    public AEBlockCapabilityCache<A> createCache(ServerLevel level, BlockPos pos, C context) {
        return createCache(level, pos, context, () -> true, () -> {
        });
    }

    /**
     * The loader's own object for this capability, resolved on first use and kept, so a lookup costs what it did when
     * code named the loader's capability directly.
     * <p>
     * Only one loader runs and only its module calls this, so the slot only ever holds that loader's type. Two threads
     * racing to fill it both resolve the same loader object -- both loaders intern capabilities by id -- so the race is
     * harmless.
     */
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public <T> T getOrCreatePlatformHandle(Function<AEBlockCapability<A, C>, T> factory) {
        var handle = platformHandle;
        if (handle == null) {
            handle = factory.apply(this);
            platformHandle = handle;
        }
        return (T) handle;
    }

    @Override
    public String toString() {
        return "AEBlockCapability[" + id + " -> " + apiClass.getName() + ", " + context + "]";
    }
}
