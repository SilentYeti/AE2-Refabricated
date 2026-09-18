/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.client;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

/**
 * The extra state a block entity or part hands to the model that draws it, beyond what the block state carries: which
 * sides a crafting cube is connected on, which cells are in a drive, a P2P tunnel's frequency.
 * <p>
 * AE2's own type rather than the loader's, because the classes that <em>produce</em> it are the parts and block
 * entities -- {@code IPart.collectModelData} is on the part interface itself -- and a loader type there would keep the
 * whole of {@code appeng.api.parts} and everything reaching it on one loader's side of the build. Each loader carries
 * one of these through its own mechanism: {@code NeoForgeModelData} puts it in a single NeoForge {@code ModelProperty}.
 * <p>
 * The surface is deliberately the same as NeoForge's {@code ModelData}, so that the producers and the models read the
 * same way as before. Like it, this is immutable and does <em>not</em> define equality: a fresh instance is built for
 * each query, and nothing compares two.
 */
public final class AEModelData {
    public static final AEModelData EMPTY = new AEModelData(Map.of());

    private final Map<AEModelProperty<?>, Object> properties;

    private AEModelData(Map<AEModelProperty<?>, Object> properties) {
        this.properties = properties;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static <T> AEModelData of(AEModelProperty<T> property, T value) {
        return builder().with(property, value).build();
    }

    public Set<AEModelProperty<?>> getProperties() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    public boolean has(AEModelProperty<?> property) {
        return properties.containsKey(property);
    }

    /**
     * The value stored under the given key, or null if there is none.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(AEModelProperty<T> property) {
        return (T) properties.get(property);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    /**
     * A builder holding everything this does, to add to.
     */
    public Builder derive() {
        return new Builder().withAll(this);
    }

    public static final class Builder {
        // Identity, because AEModelProperty is an identity key; a hash map would work but would be asking the wrong
        // question of it.
        private final Map<AEModelProperty<?>, Object> properties = new IdentityHashMap<>();

        private Builder() {
        }

        public <T> Builder with(AEModelProperty<T> property, T value) {
            properties.put(Objects.requireNonNull(property, "property"), Objects.requireNonNull(value, "value"));
            return this;
        }

        private Builder withAll(AEModelData data) {
            properties.putAll(data.properties);
            return this;
        }

        public AEModelData build() {
            return properties.isEmpty() ? EMPTY : new AEModelData(Map.copyOf(properties));
        }
    }
}
