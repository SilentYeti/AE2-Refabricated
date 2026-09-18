package appeng.neoforge;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.platform.FluidPlatform;

/**
 * Answers the two fluid questions with NeoForge's {@code FluidStack}, which is what {@code AEFluidKey} used to hold
 * outright. Amount is irrelevant to both, so it is always 1.
 */
public class NeoForgeFluidPlatform implements FluidPlatform {
    @Override
    public Component getDisplayName(Holder<Fluid> fluid, DataComponentPatch components) {
        return new FluidStack(fluid, 1, components).getHoverName();
    }

    @Override
    public <T> @Nullable T getComponent(Holder<Fluid> fluid, DataComponentPatch components, DataComponentType<T> type) {
        return new FluidStack(fluid, 1, components).get(type);
    }
}
