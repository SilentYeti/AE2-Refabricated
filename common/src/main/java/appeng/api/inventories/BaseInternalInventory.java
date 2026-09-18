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

package appeng.api.inventories;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import appeng.platform.PlatformAdapterSlot;

/**
 * Implementation aid for {@link InternalInventory} that ensures the platorm adapter maintains its referential equality
 * over time.
 * <p>
 * The adapter itself is the loader's type ({@code ResourceHandler<ItemResource>} on NeoForge), so it is created and
 * named by the loader module; this class only owns the slot that keeps it, so that the same inventory hands out the
 * same adapter for as long as it lives. Loaders cache the objects they get back from a capability lookup by identity.
 */
public abstract class BaseInternalInventory implements InternalInventory {

    private final PlatformAdapterSlot platformAdapter = new PlatformAdapterSlot();

    /**
     * Returns the loader's adapter for this inventory, creating it on first use.
     */
    @ApiStatus.Internal
    public final <T> T getOrCreatePlatformAdapter(Supplier<T> factory) {
        return platformAdapter.getOrCreate(factory);
    }
}
