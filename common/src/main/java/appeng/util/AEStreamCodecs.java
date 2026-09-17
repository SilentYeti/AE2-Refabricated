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

package appeng.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;

/**
 * Loader-agnostic replacements for the handful of {@code NeoForgeStreamCodecs} helpers AE2 used.
 * <p>
 * Those helpers are thin wrappers over {@link FriendlyByteBuf} methods that vanilla already provides, so
 * nothing loader-specific is involved and these can live in {@code :common}. Keeping them here is what lets
 * the enums that carry a {@code STREAM_CODEC} - {@code AEColor}, {@code FuzzyMode} and friends - be
 * loader-agnostic, and almost the whole codebase depends on those.
 */
public final class AEStreamCodecs {
    private AEStreamCodecs() {
    }

    public static final StreamCodec<FriendlyByteBuf, ChunkPos> CHUNK_POS = new StreamCodec<>() {
        @Override
        public ChunkPos decode(FriendlyByteBuf buf) {
            return buf.readChunkPos();
        }

        @Override
        public void encode(FriendlyByteBuf buf, ChunkPos pos) {
            buf.writeChunkPos(pos);
        }
    };

    public static <B extends FriendlyByteBuf, V extends Enum<V>> StreamCodec<B, V> enumCodec(Class<V> enumClass) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buf) {
                return buf.readEnum(enumClass);
            }

            @Override
            public void encode(B buf, V value) {
                buf.writeEnum(value);
            }
        };
    }
}
