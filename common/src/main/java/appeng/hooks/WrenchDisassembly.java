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

/**
 * Whether the current thread is part-way through a wrench disassembly.
 * <p>
 * Blocks consult this to tell a wrench taking them apart from an ordinary break, which has to happen in loader-agnostic
 * code. Setting it cannot: it is driven from a player-interaction event, and each loader delivers those differently. So
 * the flag lives here and the event handler that raises it stays with its loader -- on NeoForge that is
 * {@code WrenchHook}.
 */
public final class WrenchDisassembly {
    private static final ThreadLocal<Boolean> IN_PROGRESS = new ThreadLocal<>();

    private WrenchDisassembly() {
    }

    public static boolean isDisassembling() {
        return Boolean.TRUE.equals(IN_PROGRESS.get());
    }

    /**
     * Runs {@code disassembly} with {@link #isDisassembling()} true for this thread, clearing it afterwards however the
     * call ends.
     */
    public static <T> T whileDisassembling(java.util.function.Supplier<T> disassembly) {
        IN_PROGRESS.set(true);
        try {
            return disassembly.get();
        } finally {
            IN_PROGRESS.remove();
        }
    }
}
