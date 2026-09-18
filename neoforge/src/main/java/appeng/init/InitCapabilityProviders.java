package appeng.init;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import appeng.api.AECapabilities;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.parts.RegisterPartCapabilitiesEventInternal;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.core.AELog;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.helpers.externalstorage.GenericStackFluidHandler;
import appeng.helpers.externalstorage.GenericStackItemHandler;
import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import appeng.neoforge.NeoForgeCapabilities;
import appeng.neoforge.resources.NeoForgeInventories;
import appeng.neoforge.resources.TransactionalGenericInventory;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.encoding.PatternEncodingTerminalPart;
import appeng.parts.misc.InterfacePart;
import appeng.parts.networking.EnergyAcceptorPart;
import appeng.parts.p2p.FEP2PTunnelPart;
import appeng.parts.p2p.FluidP2PTunnelPart;
import appeng.parts.p2p.ItemP2PTunnelPart;

public final class InitCapabilityProviders {

    private InitCapabilityProviders() {
    }

    /**
     * Called with high priority to mark which capabilities are proxyable.
     */
    public static void markProxyableCapabilities(RegisterCapabilitiesEvent event) {
        // Definitely proxyable - this is a storage capability.
        event.setProxyable(NeoForgeCapabilities.of(AECapabilities.ME_STORAGE));
        // Why not - in principle a crafting machine could be behind a tunnel.
        event.setProxyable(NeoForgeCapabilities.of(AECapabilities.CRAFTING_MACHINE));
        // Why not - this is a storage capability, albeit in principle not exposed directly.
        event.setProxyable(NeoForgeCapabilities.of(AECapabilities.GENERIC_INTERNAL_INV));
        // Definitely not proxyable, we don't want to connect nodes through a capability tunnel.
        event.setNonProxyable(NeoForgeCapabilities.of(AECapabilities.IN_WORLD_GRID_NODE_HOST));
        // It would be weird to crank through a tunnel, and we might miss neighbor updates from the crankable.
        event.setNonProxyable(NeoForgeCapabilities.of(AECapabilities.CRANKABLE));
    }

    public static void register(RegisterCapabilitiesEvent event) {

        var partEvent = new RegisterPartCapabilitiesEvent();
        partEvent.addHostType(AEBlockEntities.CABLE_BUS.get());
        registerPartCapabilities(partEvent);
        ModLoader.postEvent(partEvent);
        RegisterPartCapabilitiesEventInternal.register(partEvent, event);

        initInterface(event);
        initPatternProvider(event);
        initCondenser(event);
        initMEChest(event);
        initMisc(event);
        initPoweredItem(event);
        initCrankable(event);

        for (var type : AEBlockEntities.getSubclassesOf(AEBaseInvBlockEntity.class)) {
            event.registerBlockEntity(Capabilities.Item.BLOCK, type,
                    AEBaseInvBlockEntity::getExposedItemHandler);
        }
        for (var type : AEBlockEntities.getSubclassesOf(AEBasePoweredBlockEntity.class)) {
            event.registerBlockEntity(Capabilities.Energy.BLOCK, type,
                    AEBasePoweredBlockEntity::getEnergyStorage);
        }
        for (var type : AEBlockEntities.getImplementorsOf(IInWorldGridNodeHost.class)) {
            event.registerBlockEntity(NeoForgeCapabilities.of(AECapabilities.IN_WORLD_GRID_NODE_HOST), type,
                    (object, context) -> (IInWorldGridNodeHost) object);
        }
    }

    /**
     * This registration is called with the lowest possible priority to register adapters.
     */
    public static void registerGenericAdapters(RegisterCapabilitiesEvent event) {

        for (var block : BuiltInRegistries.BLOCK) {
            if (event.isBlockRegistered(NeoForgeCapabilities.of(AECapabilities.GENERIC_INTERNAL_INV), block)) {
                registerGenericInvAdapter(event, block, Capabilities.Item.BLOCK, GenericStackItemHandler::new);
                registerGenericInvAdapter(event, block, Capabilities.Fluid.BLOCK, GenericStackFluidHandler::new);
            }
        }

    }

    private static <T> void registerGenericInvAdapter(RegisterCapabilitiesEvent event,
            Block block,
            BlockCapability<T, Direction> capability,
            Function<TransactionalGenericInventory, T> adapter) {
        event.registerBlock(
                capability,
                (level, pos, state, blockEntity, context) -> {
                    var genericInv = level.getCapability(NeoForgeCapabilities.of(AECapabilities.GENERIC_INTERNAL_INV),
                            pos, state,
                            blockEntity, context);
                    if (genericInv instanceof TransactionalGenericInventory transactional) {
                        return adapter.apply(transactional);
                    }
                    if (genericInv != null) {
                        warnNotTransactional(genericInv);
                    }
                    return null;
                },
                block);
    }

    private static final Set<Class<?>> WARNED_NOT_TRANSACTIONAL = ConcurrentHashMap.newKeySet();

    /**
     * Every inventory AE2 itself exposes is transactional. One that is not can only come from another mod, and is left
     * unexposed rather than wrapped in a handler that could not undo an aborted transaction.
     */
    private static void warnNotTransactional(GenericInternalInventory inventory) {
        if (WARNED_NOT_TRANSACTIONAL.add(inventory.getClass())) {
            AELog.warn("{} is a GenericInternalInventory but not a TransactionalGenericInventory, so it is not exposed "
                    + "as a NeoForge item or fluid handler: an aborted transfer could not be rolled back. Implement "
                    + "TransactionalGenericInventory to expose it.", inventory.getClass().getName());
        }
    }

    private static void initInterface(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                NeoForgeCapabilities.of(AECapabilities.GENERIC_INTERNAL_INV),
                AEBlockEntities.INTERFACE.get(),
                (be, context) -> be.getInterfaceLogic().getStorage());

        event.registerBlockEntity(
                NeoForgeCapabilities.of(AECapabilities.ME_STORAGE),
                AEBlockEntities.INTERFACE.get(),
                (blockEntity, context) -> {
                    return blockEntity.getInterfaceLogic().getInventory();
                });
    }

    private static void initPatternProvider(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                NeoForgeCapabilities.of(AECapabilities.GENERIC_INTERNAL_INV),
                AEBlockEntities.PATTERN_PROVIDER.get(),
                (blockEntity, context) -> blockEntity.getLogic().getReturnInv());
    }

    private static void initCondenser(RegisterCapabilitiesEvent event) {
        // Condenser will always return its external inventory, even when context is null
        // (unlike the base class it derives from)
        event.registerBlockEntity(Capabilities.Item.BLOCK, AEBlockEntities.CONDENSER.get(),
                (blockEntity, context) -> {
                    return NeoForgeInventories.resourceHandler(blockEntity.getExternalInv());
                });
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, AEBlockEntities.CONDENSER.get(),
                ((blockEntity, context) -> {
                    return blockEntity.getFluidHandler();
                }));
        event.registerBlockEntity(NeoForgeCapabilities.of(AECapabilities.ME_STORAGE), AEBlockEntities.CONDENSER.get(),
                (blockEntity, context) -> {
                    return blockEntity.getMEStorage();
                });
    }

    private static void initMEChest(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, AEBlockEntities.ME_CHEST.get(),
                MEChestBlockEntity::getFluidHandler);
        event.registerBlockEntity(NeoForgeCapabilities.of(AECapabilities.ME_STORAGE), AEBlockEntities.ME_CHEST.get(),
                MEChestBlockEntity::getMEStorage);
    }

    private static void initMisc(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                NeoForgeCapabilities.of(AECapabilities.CRAFTING_MACHINE),
                AEBlockEntities.MOLECULAR_ASSEMBLER.get(),
                (object, context) -> object);
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                AEBlockEntities.DEBUG_ITEM_GEN.get(),
                (object, context) -> object.getItemHandler());
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                AEBlockEntities.DEBUG_ENERGY_GEN.get(),
                (object, context) -> object);
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                AEBlockEntities.SKY_STONE_TANK.get(),
                (object, context) -> object.getFluidHandler());
    }

    private static void initPoweredItem(RegisterCapabilitiesEvent event) {
        registerPowerStorageItem(event, AEItems.ENTROPY_MANIPULATOR);
        registerPowerStorageItem(event, AEItems.CHARGED_STAFF);
        registerPowerStorageItem(event, AEItems.COLOR_APPLICATOR);
        registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL1K);
        registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL4K);
        registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL16K);
        registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL64K);
        registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL256K);
        registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL1K);
        registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL4K);
        registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL16K);
        registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL64K);
        registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL256K);
        registerPowerStorageItem(event, AEItems.MATTER_CANNON);
        registerPowerStorageItem(event, AEItems.WIRELESS_TERMINAL);
        registerPowerStorageItem(event, AEItems.WIRELESS_CRAFTING_TERMINAL);
    }

    private static <T extends Item & IAEItemPowerStorage> void registerPowerStorageItem(RegisterCapabilitiesEvent event,
            ItemDefinition<T> definition) {
        IAEItemPowerStorage powerStorage = definition.get();

        event.registerItem(
                Capabilities.Energy.ITEM,
                (object, context) -> new PoweredItemCapabilities(context, definition.asItem(), powerStorage),
                definition);
    }

    private static void initCrankable(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(NeoForgeCapabilities.of(AECapabilities.CRANKABLE), AEBlockEntities.CHARGER.get(),
                ChargerBlockEntity::getCrankable);
        event.registerBlockEntity(NeoForgeCapabilities.of(AECapabilities.CRANKABLE), AEBlockEntities.INSCRIBER.get(),
                InscriberBlockEntity::getCrankable);
        event.registerBlockEntity(NeoForgeCapabilities.of(AECapabilities.CRANKABLE),
                AEBlockEntities.GROWTH_ACCELERATOR.get(),
                GrowthAcceleratorBlockEntity::getCrankable);
    }

    private static void registerPartCapabilities(RegisterPartCapabilitiesEvent event) {
        event.register(Capabilities.Item.BLOCK,
                (part, direction) -> NeoForgeInventories.resourceHandler(part.getLogic().getBlankPatternInv()),
                PatternEncodingTerminalPart.class);
        event.register(NeoForgeCapabilities.of(AECapabilities.GENERIC_INTERNAL_INV),
                (part, context) -> part.getLogic().getReturnInv(),
                PatternProviderPart.class);
        event.register(NeoForgeCapabilities.of(AECapabilities.GENERIC_INTERNAL_INV),
                (part, context) -> part.getInterfaceLogic().getStorage(),
                InterfacePart.class);
        event.register(NeoForgeCapabilities.of(AECapabilities.ME_STORAGE),
                (part, context) -> part.getInterfaceLogic().getInventory(), InterfacePart.class);

        event.register(Capabilities.Item.BLOCK, (part, context) -> part.getExposedApi(),
                ItemP2PTunnelPart.class);
        event.register(Capabilities.Energy.BLOCK, (part, context) -> part.getExposedApi(),
                FEP2PTunnelPart.class);
        event.register(Capabilities.Fluid.BLOCK, (part, context) -> part.getExposedApi(),
                FluidP2PTunnelPart.class);

        event.register(Capabilities.Energy.BLOCK, (part, context) -> part.getEnergyStorage(),
                EnergyAcceptorPart.class);
    }

}
