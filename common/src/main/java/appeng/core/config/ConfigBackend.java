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
 * The loader's config system, as {@code AEConfig} sees it.
 * <p>
 * Handed to {@code AEConfig.register} by each loader's entrypoint rather than found through a service loader, because
 * it needs something only the entrypoint has: the mod container on NeoForge, the config directory on Fabric.
 */
public interface ConfigBackend {
    ConfigBuilder newBuilder();

    /**
     * Registers the file the builder declared. {@code onLoad} runs every time its values are (re)read from disk -- on
     * NeoForge from the config events, on Fabric once, before this returns.
     */
    ConfigFile register(ConfigType type, ConfigBuilder builder, Runnable onLoad);
}
