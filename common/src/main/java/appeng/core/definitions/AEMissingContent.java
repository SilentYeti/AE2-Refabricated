package appeng.core.definitions;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import appeng.api.ids.AEItemIds;

/**
 * The {@code ae2:missing_content} item and the data components it carries.
 * <p>
 * When a key, a generic stack or an item stack fails to deserialize, AE2 substitutes this item and attaches the
 * original NBT to it, so the data survives a round trip through a world that could not understand it and is written
 * back unchanged on save. Losing that would silently delete a player's contents.
 * <p>
 * This owns the components rather than {@code AEComponents} because {@code AEComponents} names the whole content
 * registry, including the key API that needs these three -- the two would name each other. Splitting the feature out is
 * the same cut that untangled the recipe types. {@code AEComponents} re-exports and registers them, so it remains the
 * single place components are registered.
 */
public final class AEMissingContent {
    private AEMissingContent() {
    }

    public static final DataComponentType<CustomData> ITEMSTACK_DATA = DataComponentType.<CustomData>builder()
            .persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC).build();

    public static final DataComponentType<CustomData> AEKEY_DATA = DataComponentType.<CustomData>builder()
            .persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC).build();

    public static final DataComponentType<String> ERROR = DataComponentType.<String>builder()
            .persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();

    /**
     * Resolved from the registry rather than from {@code AEItems}, which names every item AE2 has and so cannot be
     * reached from the key API. Memoised, because this is on a deserialization failure path that can repeat.
     */
    private static final Supplier<Item> ITEM = Suppliers
            .memoize(() -> BuiltInRegistries.ITEM.getValue(AEItemIds.MISSING_CONTENT));

    public static Item item() {
        return ITEM.get();
    }

    public static ItemStack stack() {
        return new ItemStack(item());
    }

    public static boolean is(ItemStack stack) {
        return stack.is(item());
    }

    /**
     * Builds the replacement stack for a failed deserialization, carrying the original data so it can be written back.
     *
     * @param dataComponent which of {@link #ITEMSTACK_DATA} / {@link #AEKEY_DATA} the caller round-trips through
     * @param originalData  the NBT that failed to parse, or null when it could not be converted
     */
    public static ItemStack replacement(DataComponentType<CustomData> dataComponent,
            @Nullable CompoundTag originalData, String error) {
        var stack = stack();
        if (originalData != null) {
            stack.set(dataComponent, CustomData.of(originalData));
        }
        stack.set(ERROR, error);
        return stack;
    }
}
