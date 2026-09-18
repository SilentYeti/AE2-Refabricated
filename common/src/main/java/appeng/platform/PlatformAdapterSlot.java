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

package appeng.platform;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

/**
 * Holds one object that the loader's module creates over the thing holding this slot, for as long as that thing lives.
 * <p>
 * The object is the loader's own type -- a {@code ResourceHandler} for an inventory, a {@code SnapshotJournal} over one
 * -- so only the loader module can name it, and only the loader module calls {@link #getOrCreate}. What {@code :common}
 * owns is the requirement that it be the <em>same</em> object every time: loaders cache what a capability lookup
 * returns by identity, and a transaction journal that was not the same object throughout a transaction would not roll
 * every change back.
 * <p>
 * Only one loader runs at a time and only its own module calls this, so the slot only ever holds that loader's type and
 * the unchecked cast cannot fail. Not synchronised: it is filled on the thread that first looks the thing up, and
 * creating the adapter twice would be wasteful rather than wrong -- which is the behaviour this replaced.
 */
@ApiStatus.Internal
public final class PlatformAdapterSlot {
    private Object adapter;

    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(Supplier<T> factory) {
        if (adapter == null) {
            adapter = factory.get();
        }
        return (T) adapter;
    }
}
