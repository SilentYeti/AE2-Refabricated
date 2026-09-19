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

package appeng.core.config;

/**
 * Declares the sections and values of one config file, in order.
 * <p>
 * Exactly the operations AE2's config uses of NeoForge's {@code ModConfigSpec.Builder}, with the same meaning, so that
 * on NeoForge the file comes out byte-for-byte as it did -- which {@code AEConfigSpecTest} pins. A comment applies to
 * the next section or value declared; a value outside its range is clamped to it when the file is read.
 */
public interface ConfigBuilder {
    ConfigBuilder push(String section);

    ConfigBuilder pop();

    ConfigBuilder comment(String comment);

    ConfigOption<Boolean> define(String name, boolean defaultValue);

    ConfigOption<Integer> defineInRange(String name, int defaultValue, int min, int max);

    ConfigOption<Double> defineInRange(String name, double defaultValue, double min, double max);

    <E extends Enum<E>> ConfigOption<E> defineEnum(String name, E defaultValue);
}
