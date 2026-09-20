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

package appeng.hooks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.level.LevelAccessor;

/**
 * Who wants to know when a level is unloaded -- quantum bridges, so they can take themselves apart before their block
 * entities go away.
 * <p>
 * The list lives here and each loader feeds it from its own event: NeoForge's {@code LevelEvent.Unload}, Fabric's
 * {@code ServerWorldEvents.UNLOAD}. Listeners come and go with the things that own them, which is why they are
 * removable and why this is a set rather than a one-time registration.
 */
public final class LevelUnloadListeners {
    private static final Set<Consumer<LevelAccessor>> LISTENERS = ConcurrentHashMap.newKeySet();

    private LevelUnloadListeners() {
    }

    public static void add(Consumer<LevelAccessor> listener) {
        LISTENERS.add(listener);
    }

    public static void remove(Consumer<LevelAccessor> listener) {
        LISTENERS.remove(listener);
    }

    /**
     * Called by the loader when a level unloads.
     */
    @ApiStatus.Internal
    public static void fire(LevelAccessor level) {
        for (var listener : LISTENERS) {
            listener.accept(level);
        }
    }
}
