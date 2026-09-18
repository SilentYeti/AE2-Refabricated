package appeng.fabric;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import appeng.platform.FluidPlatform;

/**
 * Answers the two fluid questions with Fabric's {@code FluidVariant}.
 * <p>
 * A {@code FluidVariant} is a fluid plus a {@link DataComponentPatch}, which is exactly what {@code AEFluidKey} holds,
 * so this is a straight handoff in both directions rather than a conversion.
 */
public class FabricFluidPlatform implements FluidPlatform {
    @Override
    public Component getDisplayName(Holder<Fluid> fluid, DataComponentPatch components) {
        return FluidVariantAttributes.getName(variant(fluid, components));
    }

    @Override
    public <T> @Nullable T getComponent(Holder<Fluid> fluid, DataComponentPatch components, DataComponentType<T> type) {
        return variant(fluid, components).getComponents().get(type);
    }

    private static FluidVariant variant(Holder<Fluid> fluid, DataComponentPatch components) {
        return FluidVariant.of(fluid.value(), components);
    }
}
