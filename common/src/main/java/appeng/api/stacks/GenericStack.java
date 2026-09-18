package appeng.api.stacks;

import java.util.List;
import java.util.Objects;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import appeng.core.definitions.AEMissingContent;

/**
 * Represents some amount of some generic resource that AE can store or handle in crafting.
 */
public record GenericStack(AEKey what, long amount) {

    @ApiStatus.Internal
    public static final String AMOUNT_FIELD = "#";

    private static final Logger LOG = LoggerFactory.getLogger(GenericStack.class);

    public static final MapCodec<GenericStack> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            AEKey.MAP_CODEC.forGetter(GenericStack::what),
            Codec.LONG.fieldOf(AMOUNT_FIELD).forGetter(GenericStack::amount)).apply(builder, GenericStack::new));

    public static final Codec<GenericStack> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, GenericStack> STREAM_CODEC = StreamCodec.ofMember(
            GenericStack::writeBuffer,
            GenericStack::readBuffer);

    /**
     * This result function converts failed serialization results for GenericStack into missing content storing the
     * error message.
     */
    private static final Codec.ResultFunction<GenericStack> MISSING_CONTENT_GENERICSTACK_RESULT = new Codec.ResultFunction<>() {
        @Override
        public <T> DataResult<Pair<GenericStack, T>> apply(DynamicOps<T> ops, T input,
                DataResult<Pair<GenericStack, T>> a) {
            if (a instanceof DataResult.Error<Pair<GenericStack, T>> error) {
                var convert = Dynamic.convert(ops, NbtOps.INSTANCE, input);
                LOG.error("Failed to deserialize GenericStack {}: {}", input, error.message());
                var missingContent = AEMissingContent.replacement(AEMissingContent.ITEMSTACK_DATA,
                        convert instanceof CompoundTag compoundTag ? compoundTag : null, error.message());

                var replacement = new GenericStack(AEItemKey.of(missingContent), 1);

                return DataResult.success(
                        Pair.of(replacement, input),
                        Lifecycle.stable());
            }

            // Return unchanged if deserialization succeeded
            return a;
        }

        @Override
        public <T> DataResult<T> coApply(DynamicOps<T> ops, GenericStack input, DataResult<T> t) {
            // When the serialization result failed, we write a missing content item instead
            // this one will NOT be recoverable
            if (t instanceof DataResult.Error<T> error) {
                LOG.error("Failed to serialize GenericStack {}: {}", input, error.message());
                var missingContent = AEMissingContent.replacement(AEMissingContent.ITEMSTACK_DATA, null,
                        error.message());

                var replacement = new GenericStack(AEItemKey.of(missingContent), 1);
                return CODEC.encodeStart(ops, replacement).setLifecycle(t.lifecycle());
            }

            // When the input is a MISSING_CONTENT item and has the original data attached,
            // we write that back.
            if (input.what() instanceof AEItemKey itemKey && AEMissingContent.is(itemKey.getReadOnlyStack())) {
                var originalData = itemKey.get(AEMissingContent.ITEMSTACK_DATA);
                if (originalData != null) {
                    return DataResult.success(Dynamic.convert(NbtOps.INSTANCE, ops, originalData.copyTag()),
                            t.lifecycle());
                }
            }

            return t;
        }
    };

    public static final Codec<List<@Nullable GenericStack>> FAULT_TOLERANT_NULLABLE_LIST_CODEC = new GenericStackListCodec(
            CODEC.mapResult(MISSING_CONTENT_GENERICSTACK_RESULT));

    public static final Codec<List<GenericStack>> FAULT_TOLERANT_LIST_CODEC = CODEC
            .mapResult(MISSING_CONTENT_GENERICSTACK_RESULT).listOf();

    public GenericStack {
        Objects.requireNonNull(what, "what");
    }

    @Nullable
    public static GenericStack readBuffer(RegistryFriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }

        var what = AEKey.readKey(buffer);
        if (what == null) {
            return null;
        }

        return new GenericStack(what, buffer.readVarLong());
    }

    public static void writeBuffer(@Nullable GenericStack stack, RegistryFriendlyByteBuf buffer) {
        if (stack == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);

            AEKey.writeKey(buffer, stack.what);
            buffer.writeVarLong(stack.amount);
        }
    }

    @Nullable
    public static GenericStack readTag(ValueInput input) {
        if (input.getString(AEKey.TYPE_FIELD).isEmpty()) {
            return null;
        }
        return input.read(MAP_CODEC).orElse(null);
    }

    public static void writeTag(ValueOutput output, @Nullable GenericStack stack) {
        if (stack != null) {
            output.store(MAP_CODEC, stack);
        }
    }

    /**
     * Converts a given item stack into a generic stack, accounting for a {@link GenericStack} already wrapped in an
     * {@link ItemStack}, unwrapping it automatically. If the item stack is empty, null is returned.
     */
    @Nullable
    public static GenericStack fromItemStack(ItemStack stack) {
        var genericStack = WrappedStacks.unwrap(stack);
        if (genericStack != null) {
            return genericStack;
        }

        var key = AEItemKey.of(stack);
        if (key == null) {
            return null;
        }
        return new GenericStack(key, stack.getCount());
    }

    public static long getStackSizeOrZero(@Nullable GenericStack stack) {
        return stack == null ? 0 : stack.amount;
    }

    public static GenericStack sum(GenericStack left, GenericStack right) {
        if (!left.what.equals(right.what)) {
            throw new IllegalArgumentException("Cannot sum generic stacks of " + left.what + " and " + right.what);
        }
        return new GenericStack(left.what, left.amount + right.amount);
    }

}
