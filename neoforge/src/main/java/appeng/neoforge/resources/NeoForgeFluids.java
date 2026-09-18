package appeng.neoforge.resources;

import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

/**
 * Conversion between {@link AEFluidKey} and NeoForge's own fluid types.
 * <p>
 * These used to be methods on {@link AEFluidKey} itself, which pinned it -- and through it the whole key API -- to
 * NeoForge. The key now identifies a fluid with vanilla types only, so each loader keeps the conversions to its own
 * types on its own side. The Fabric counterpart converts to {@code FluidVariant} instead.
 */
public final class NeoForgeFluids {
    private NeoForgeFluids() {
    }

    @Nullable
    public static AEFluidKey key(FluidStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return AEFluidKey.of(stack.typeHolder(), stack.getComponentsPatch());
    }

    @Nullable
    public static AEFluidKey key(FluidResource resource) {
        if (resource.isEmpty()) {
            return null;
        }
        return key(resource.toStack(1));
    }

    public static FluidStack toStack(AEFluidKey key, int amount) {
        return new FluidStack(key.getFluidHolder(), amount, key.getComponents());
    }

    public static FluidResource toResource(AEFluidKey key) {
        return FluidResource.of(toStack(key, 1));
    }

    public static boolean matches(AEKey what, FluidStack stack) {
        return what instanceof AEFluidKey key && matches(key, stack);
    }

    public static boolean matches(AEFluidKey key, FluidStack stack) {
        return !stack.isEmpty() && key.equals(key(stack));
    }

    /**
     * Converts a fluid stack into a generic stack, carrying the amount over. Null when the stack is empty.
     */
    @Nullable
    public static GenericStack genericStack(FluidStack stack) {
        var key = key(stack);
        return key == null ? null : new GenericStack(key, stack.getAmount());
    }

    /**
     * Converts a fluid resource and an amount into a generic stack. Null when the resource is empty.
     */
    @Nullable
    public static GenericStack genericStack(FluidResource resource, long amount) {
        var key = key(resource);
        return key == null ? null : new GenericStack(key, amount);
    }
}
