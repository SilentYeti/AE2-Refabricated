package appeng.neoforge.resources;

import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.transfer.item.ItemResource;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

/**
 * Conversion between {@link AEItemKey} and NeoForge's {@link ItemResource}.
 *
 * @see NeoForgeFluids for why these live on the loader's side rather than on the key
 */
public final class NeoForgeItems {
    private NeoForgeItems() {
    }

    @Nullable
    public static AEItemKey key(ItemResource resource) {
        if (resource.isEmpty()) {
            return null;
        }
        return AEItemKey.of(resource.toStack());
    }

    public static ItemResource toResource(AEItemKey key) {
        return ItemResource.of(key.getReadOnlyStack());
    }

    /**
     * Converts an item resource and an amount into a generic stack. Null when the resource is empty.
     */
    @Nullable
    public static GenericStack genericStack(ItemResource resource, long amount) {
        var key = key(resource);
        return key == null ? null : new GenericStack(key, amount);
    }
}
