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

package appeng.datagen.providers.tags;

import java.util.Arrays;
import java.util.function.Function;

import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

/**
 * Tag content is addressed by {@link ResourceKey} since 26.2; the {@code add(T)} overloads that took the registry
 * object itself are gone. Our providers deal in registry objects throughout ({@code AEBlocks.X.block()},
 * {@code Blocks.BEDROCK}), so this wrapper converts them back, the same way vanilla's
 * {@link net.minecraft.data.tags.BlockItemTagAppender} layers its own convenience overloads on top.
 */
public class AE2TagAppender<T> implements TagAppender<T> {
    private final TagAppender<T> parent;
    private final Function<T, ResourceKey<T>> keyExtractor;

    public AE2TagAppender(TagAppender<T> parent, Function<T, ResourceKey<T>> keyExtractor) {
        this.parent = parent;
        this.keyExtractor = keyExtractor;
    }

    public AE2TagAppender<T> add(T element) {
        return add(keyExtractor.apply(element));
    }

    @SafeVarargs
    public final AE2TagAppender<T> add(T... elements) {
        return addAllElements(Arrays.stream(elements).map(keyExtractor)::iterator);
    }

    private AE2TagAppender<T> addAllElements(Iterable<ResourceKey<T>> keys) {
        keys.forEach(parent::add);
        return this;
    }

    @Override
    public AE2TagAppender<T> add(ResourceKey<T> element) {
        parent.add(element);
        return this;
    }

    @SafeVarargs
    @Override
    public final AE2TagAppender<T> add(ResourceKey<T>... elements) {
        parent.add(elements);
        return this;
    }

    @Override
    public AE2TagAppender<T> addOptional(ResourceKey<T> element) {
        parent.addOptional(element);
        return this;
    }

    @Override
    public AE2TagAppender<T> addTag(TagKey<T> tag) {
        parent.addTag(tag);
        return this;
    }

    @Override
    public AE2TagAppender<T> addOptionalTag(TagKey<T> tag) {
        parent.addOptionalTag(tag);
        return this;
    }

    @Override
    public AE2TagAppender<T> add(TagEntry entry) {
        parent.add(entry);
        return this;
    }

    @Override
    public AE2TagAppender<T> replace(boolean value) {
        parent.replace(value);
        return this;
    }

    @Override
    public AE2TagAppender<T> remove(ResourceKey<T> element) {
        parent.remove(element);
        return this;
    }

    @Override
    public AE2TagAppender<T> remove(TagKey<T> tag) {
        parent.remove(tag);
        return this;
    }
}
