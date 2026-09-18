package appeng.api.stacks;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.ids.AEItemIds;

/**
 * Carrying a {@link GenericStack} inside an {@link ItemStack}, for the places that can only speak in item stacks -- GUI
 * slots, filters and ghost ingredients.
 * <p>
 * The item this uses is {@code ae2:wrapped_generic_stack}, whose class is {@code WrappedGenericStack}. That class
 * extends AE2's item base and so cannot be reached from the key API, which is why the primitives live here and the item
 * delegates to them rather than the other way round. Same split as {@code AEMissingContent}: the component is defined
 * here and registered by {@code AEComponents}, so registration stays in one place without the two naming each other.
 */
public final class WrappedStacks {
    private WrappedStacks() {
    }

    /**
     * Registered as {@code ae2:wrapped_stack} by {@code AEComponents}.
     */
    public static final DataComponentType<GenericStack> COMPONENT = DataComponentType.<GenericStack>builder()
            .persistent(GenericStack.CODEC).networkSynchronized(GenericStack.STREAM_CODEC).build();

    private static final Supplier<Item> ITEM = Suppliers
            .memoize(() -> BuiltInRegistries.ITEM.getValue(AEItemIds.WRAPPED_GENERIC_STACK));

    public static Item item() {
        return ITEM.get();
    }

    public static ItemStack wrap(GenericStack stack) {
        var result = new ItemStack(item());
        result.set(COMPONENT, stack);
        return result;
    }

    public static ItemStack wrap(AEKey what, long amount) {
        return wrap(new GenericStack(what, amount));
    }

    /**
     * Null-tolerant: an absent stack becomes {@link ItemStack#EMPTY}.
     */
    public static ItemStack wrapOrEmpty(@Nullable GenericStack stack) {
        return stack != null ? wrap(stack) : ItemStack.EMPTY;
    }

    public static boolean isWrapped(ItemStack stack) {
        return stack.is(item());
    }

    /**
     * The stack carried by a wrapped item stack, or null if this is not one.
     */
    @Nullable
    public static GenericStack unwrap(ItemStack stack) {
        // The isEmpty check is needed because the item can match while its count is 0
        if (!stack.isEmpty() && isWrapped(stack)) {
            return stack.get(COMPONENT);
        }
        return null;
    }
}
