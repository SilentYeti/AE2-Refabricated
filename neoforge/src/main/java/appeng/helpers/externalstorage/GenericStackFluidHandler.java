package appeng.helpers.externalstorage;

import net.neoforged.neoforge.transfer.fluid.FluidResource;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.stacks.AEKeyType;
import appeng.helpers.ResourceConversion;
import appeng.neoforge.resources.TransactionJournal;

/**
 * Exposes a {@link GenericInternalInventory} as the platforms external fluid storage interface.
 */
public class GenericStackFluidHandler extends GenericStackInvHandler<FluidResource> {
    public GenericStackFluidHandler(GenericInternalInventory inv, TransactionJournal journal) {
        super(ResourceConversion.FLUID, AEKeyType.fluids(), inv, journal);
    }
}
