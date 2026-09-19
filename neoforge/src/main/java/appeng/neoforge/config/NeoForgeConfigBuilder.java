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

package appeng.neoforge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import appeng.core.config.ConfigBuilder;
import appeng.core.config.ConfigOption;

/**
 * {@link ConfigBuilder} as NeoForge's {@code ModConfigSpec.Builder}: every call passed straight through, so the spec
 * that comes out is the one AE2 built directly before. {@code AEConfigSpecTest} builds through this class and compares
 * the result with the spec as it was.
 */
public final class NeoForgeConfigBuilder implements ConfigBuilder {
    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

    @Override
    public ConfigBuilder push(String section) {
        builder.push(section);
        return this;
    }

    @Override
    public ConfigBuilder pop() {
        builder.pop();
        return this;
    }

    @Override
    public ConfigBuilder comment(String comment) {
        builder.comment(comment);
        return this;
    }

    @Override
    public ConfigOption<Boolean> define(String name, boolean defaultValue) {
        return option(builder.define(name, defaultValue));
    }

    @Override
    public ConfigOption<Integer> defineInRange(String name, int defaultValue, int min, int max) {
        return option(builder.defineInRange(name, defaultValue, min, max));
    }

    @Override
    public ConfigOption<Double> defineInRange(String name, double defaultValue, double min, double max) {
        return option(builder.defineInRange(name, defaultValue, min, max));
    }

    @Override
    public <E extends Enum<E>> ConfigOption<E> defineEnum(String name, E defaultValue) {
        return option(builder.defineEnum(name, defaultValue));
    }

    public ModConfigSpec build() {
        return builder.build();
    }

    private static <T> ConfigOption<T> option(ModConfigSpec.ConfigValue<T> value) {
        return new ConfigOption<>() {
            @Override
            public T get() {
                return value.get();
            }

            @Override
            public void set(T newValue) {
                value.set(newValue);
            }
        };
    }
}
