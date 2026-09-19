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

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

import appeng.core.config.ConfigBackend;
import appeng.core.config.ConfigBuilder;
import appeng.core.config.ConfigFile;
import appeng.core.config.ConfigType;

/**
 * {@link ConfigBackend} as NeoForge's own config system: each file is registered with the mod container, and the load
 * callback runs on the same two events AE2 listened to before -- the first load and every reload of that file.
 */
public final class NeoForgeConfigBackend implements ConfigBackend {
    private final ModContainer container;

    public NeoForgeConfigBackend(ModContainer container) {
        this.container = container;
    }

    @Override
    public ConfigBuilder newBuilder() {
        return new NeoForgeConfigBuilder();
    }

    @Override
    public ConfigFile register(ConfigType type, ConfigBuilder builder, Runnable onLoad) {
        var spec = ((NeoForgeConfigBuilder) builder).build();
        container.registerConfig(switch (type) {
            case CLIENT -> ModConfig.Type.CLIENT;
            case COMMON -> ModConfig.Type.COMMON;
        }, spec);
        container.getEventBus().addListener((ModConfigEvent.Loading event) -> {
            if (event.getConfig().getSpec() == spec) {
                onLoad.run();
            }
        });
        container.getEventBus().addListener((ModConfigEvent.Reloading event) -> {
            if (event.getConfig().getSpec() == spec) {
                onLoad.run();
            }
        });
        return spec::save;
    }
}
