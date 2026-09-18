package appeng.client.gui.me.common;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.GenericStack;
import appeng.items.misc.WrappedGenericStack;
import appeng.menu.slot.ClientReadOnlySlot;

/**
 * A slot to showcase an item on the client-side.
 */
public class ClientDisplaySlot extends ClientReadOnlySlot {
    private final ItemStack item;

    public ClientDisplaySlot(@Nullable GenericStack stack) {
        item = WrappedGenericStack.wrapOrEmpty(stack);
    }

    @Override
    public ItemStack getItem() {
        return item;
    }
}
