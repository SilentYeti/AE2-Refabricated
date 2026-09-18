package appeng.api.stacks;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import appeng.api.storage.AEKeyFilter;
import appeng.platform.FluidPlatform;

/**
 * Identifies a fluid, optionally carrying data components.
 * <p>
 * This used to wrap NeoForge's {@code FluidStack} with its amount pinned to 1, which is the same information as a
 * {@link Holder} plus a {@link DataComponentPatch} -- both vanilla types. Holding those directly is what lets the key
 * be shared between loaders; the two things vanilla genuinely cannot answer about a fluid, its display name and its
 * default components, go through {@link FluidPlatform}. Conversion to and from a loader's own fluid types lives on that
 * loader's side.
 */
public final class AEFluidKey extends AEKey {
    private static final Logger LOG = LoggerFactory.getLogger(AEFluidKey.class);

    public static final MapCodec<AEFluidKey> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BuiltInRegistries.FLUID.holderByNameCodec().validate(
                            holder -> holder.is(Fluids.EMPTY.builtInRegistryHolder())
                                    ? DataResult.error(() -> "Fluid must not be minecraft:empty")
                                    : DataResult.success(holder))
                            .fieldOf("id").forGetter(key -> key.fluid),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(key -> key.components))
                    .apply(instance, AEFluidKey::new));
    public static final Codec<AEFluidKey> CODEC = MAP_CODEC.codec();

    public static final int AMOUNT_BUCKET = 1000;
    public static final int AMOUNT_BLOCK = 1000;

    private final Holder<Fluid> fluid;
    private final DataComponentPatch components;
    private final int hashCode;

    private AEFluidKey(Holder<Fluid> fluid, DataComponentPatch components) {
        Preconditions.checkArgument(!fluid.is(Fluids.EMPTY.builtInRegistryHolder()), "fluid was empty");
        this.fluid = fluid;
        this.components = components;
        this.hashCode = Objects.hash(fluid.value(), components);
    }

    public static AEFluidKey of(Fluid fluid) {
        return of(fluid.builtInRegistryHolder(), DataComponentPatch.EMPTY);
    }

    /**
     * The general constructor, and the one a loader's own conversion helpers go through.
     */
    @Nullable
    public static AEFluidKey of(Holder<Fluid> fluid, DataComponentPatch components) {
        if (fluid.is(Fluids.EMPTY.builtInRegistryHolder())) {
            return null;
        }
        return new AEFluidKey(fluid, components);
    }

    public static boolean is(AEKey what) {
        return what instanceof AEFluidKey;
    }

    public static AEKeyFilter filter() {
        return AEFluidKey::is;
    }

    @Override
    public AEKeyType getType() {
        return AEKeyType.fluids();
    }

    @Override
    public AEFluidKey dropSecondary() {
        return new AEFluidKey(fluid, DataComponentPatch.EMPTY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AEFluidKey aeFluidKey = (AEFluidKey) o;
        // The hash code comparison is a fast-fail cheap check
        return hashCode == aeFluidKey.hashCode && fluid.value() == aeFluidKey.fluid.value()
                && components.equals(aeFluidKey.components);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static AEFluidKey fromTag(ValueInput input) {
        try {
            return input.read(MAP_CODEC).orElseThrow();
        } catch (Exception e) {
            LOG.debug("Tried to load an invalid fluid key from NBT: {}", input, e);
            return null;
        }
    }

    @Override
    public void toTag(ValueOutput output) {
        output.store(MAP_CODEC, this);
    }

    @Override
    public Object getPrimaryKey() {
        return getFluid();
    }

    @Override
    public Identifier getId() {
        return BuiltInRegistries.FLUID.getKey(getFluid());
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        // Fluids are voided
    }

    @Override
    protected Component computeDisplayName() {
        return FluidPlatform.get().getDisplayName(fluid, components);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isTagged(TagKey<?> tag) {
        // This will just return false for incorrectly cast tags
        return fluid.is((TagKey<Fluid>) tag);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<T> type) {
        return FluidPlatform.get().getComponent(fluid, components, type);
    }

    @Override
    public boolean hasComponents() {
        return !components.isEmpty();
    }

    public Fluid getFluid() {
        return fluid.value();
    }

    /**
     * The fluid as a registry holder, for a loader converting this to its own fluid type.
     */
    public Holder<Fluid> getFluidHolder() {
        return fluid;
    }

    /**
     * The data components, for a loader converting this to its own fluid type.
     */
    public DataComponentPatch getComponents() {
        return components;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        STREAM_CODEC.encode(data, this);
    }

    public static AEFluidKey fromPacket(RegistryFriendlyByteBuf data) {
        return STREAM_CODEC.decode(data);
    }

    private static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, AEFluidKey> STREAM_CODEC = net.minecraft.network.codec.StreamCodec
            .composite(
                    ByteBufCodecs.holderRegistry(Registries.FLUID),
                    key -> key.fluid,
                    DataComponentPatch.STREAM_CODEC,
                    key -> key.components,
                    AEFluidKey::new);

    public static boolean is(@Nullable GenericStack stack) {
        return stack != null && stack.what() instanceof AEFluidKey;
    }

    @Override
    public String toString() {
        var id = BuiltInRegistries.FLUID.getKey(getFluid());
        String idString = id != BuiltInRegistries.FLUID.getDefaultKey() ? id.toString()
                : getFluid().getClass().getName() + "(unregistered)";
        return components.isEmpty() ? idString : idString + " (+components)";
    }
}
