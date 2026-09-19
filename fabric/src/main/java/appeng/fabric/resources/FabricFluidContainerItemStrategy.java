/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.fabric.resources;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

/**
 * Fluids inside items -- buckets, tanks, other mods' cells -- through Fabric's {@code FluidStorage.ITEM}: the twin of
 * {@code NeoForgeFluidContainerItemStrategy}, method for method, with every amount converted at the boundary by
 * {@link FabricFluids}, in whole millibuckets.
 * <p>
 * The item is reached the way Fabric expects, through a {@link ContainerItemContext} -- the cursor, a player slot, or
 * for merely looking inside a stack, a constant copy of it. Changing a bucket's contents replaces the item in that
 * slot, so the context has to be a real slot for an exchange to stick.
 * <p>
 * Every call is its own outer transaction, as {@code Transaction.open(null)} is on NeoForge: AE2 calls these from menu
 * interactions, never from inside another mod's transaction.
 */
public class FabricFluidContainerItemStrategy implements ContainerItemStrategy<AEFluidKey, Storage<FluidVariant>> {
    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        var storage = ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM);
        if (storage == null) {
            return null;
        }
        // What is in it, not what could be taken out: a constant context accepts any exchange and changes nothing, so
        // the views are read directly, as NeoForge's FluidUtil.getFirstStackContained does
        for (var view : storage.nonEmptyViews()) {
            long millibuckets = FabricFluids.toMillibuckets(view.getAmount());
            if (millibuckets > 0) {
                return new GenericStack(FabricFluids.key(view.getResource()), millibuckets);
            }
        }
        return null;
    }

    @Override
    public @Nullable Storage<FluidVariant> findCarriedContext(Player player, AbstractContainerMenu menu) {
        return ContainerItemContext.ofPlayerCursor(player, menu).find(FluidStorage.ITEM);
    }

    @Override
    public @Nullable Storage<FluidVariant> findPlayerSlotContext(Player player, int slot) {
        var slotStorage = PlayerInventoryStorage.of(player).getSlots().get(slot);
        return ContainerItemContext.ofPlayerSlot(player, slotStorage).find(FluidStorage.ITEM);
    }

    @Override
    public long extract(Storage<FluidVariant> context, AEFluidKey what, long amount, Actionable mode) {
        try (var tx = Transaction.openOuter()) {
            long extracted = FabricFluids.extractWholeMillibuckets(context, FabricFluids.variant(what), amount, tx);
            if (mode == Actionable.MODULATE) {
                tx.commit();
            }
            return extracted;
        }
    }

    @Override
    public long insert(Storage<FluidVariant> context, AEFluidKey what, long amount, Actionable mode) {
        try (var tx = Transaction.openOuter()) {
            long inserted = FabricFluids.insertWholeMillibuckets(context, FabricFluids.variant(what), amount, tx);
            if (mode == Actionable.MODULATE) {
                tx.commit();
            }
            return inserted;
        }
    }

    @Override
    public void playFillSound(Player player, AEFluidKey what) {
        playSound(player, FluidVariantAttributes.getFillSound(FabricFluids.variant(what)));
    }

    @Override
    public void playEmptySound(Player player, AEFluidKey what) {
        playSound(player, FluidVariantAttributes.getEmptySound(FabricFluids.variant(what)));
    }

    /** As NeoForge's {@code FluidSoundHelper} plays it. */
    private static void playSound(Player player, @Nullable SoundEvent sound) {
        if (sound != null) {
            player.playSound(sound);
        }
    }

    @Override
    public @Nullable GenericStack getExtractableContent(Storage<FluidVariant> context) {
        try (var tx = Transaction.openOuter()) {
            var first = FabricFluids.firstExtractable(context, tx);
            return first != null ? new GenericStack(first.what(), first.millibuckets()) : null;
        }
    }
}
