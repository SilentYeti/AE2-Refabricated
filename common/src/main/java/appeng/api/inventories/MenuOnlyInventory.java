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

import org.jetbrains.annotations.ApiStatus;

/**
 * An {@link InternalInventory} that exists only to back slots in a menu, and must never be handed to another mod's item
 * API. Asking a loader to expose one is a programming error and throws.
 * <p>
 * The configuration-slot view of a {@code GenericStackInv} is one: it converts between item stacks and AE keys for the
 * player's benefit, and exposing it would let anything with an item API write filter entries. It used to refuse from
 * its own {@code toResourceHandler()}; as a marker here, each loader's adapter honours it without having to know the
 * class, so a loader cannot forget to.
 */
@ApiStatus.Internal
public interface MenuOnlyInventory extends InternalInventory {
}
