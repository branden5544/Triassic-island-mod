package com.triassicisland.registry;

import com.triassicisland.TriassicIsland;
import com.triassicisland.computer.ComputerBlockEntity;
import com.triassicisland.machine.MachineBlockEntity;
import com.triassicisland.machine.MachineKind;
import com.triassicisland.park.ElectricFenceBlockEntity;
import com.triassicisland.park.ElectricFenceGateBlockEntity;
import com.triassicisland.park.FeederBlockEntity;
import com.triassicisland.power.BatteryBlockEntity;
import com.triassicisland.power.BreakerBlockEntity;
import com.triassicisland.power.EnergyMeterBlockEntity;
import com.triassicisland.power.FuseHolderBlockEntity;
import com.triassicisland.power.LampBlockEntity;
import com.triassicisland.power.PoleInsulatorBlockEntity;
import com.triassicisland.power.VariableTransformerBlockEntity;
import com.triassicisland.power.CableBlockEntity;
import com.triassicisland.power.HiddenCableBlockEntity;
import com.triassicisland.power.SolarPanelBlockEntity;
import com.triassicisland.power.TransformerBlockEntity;
import com.triassicisland.power.WindTurbineBlockEntity;
import com.triassicisland.security.AlarmBlockEntity;
import com.triassicisland.security.KeycardReaderBlockEntity;
import com.triassicisland.security.MonitorBlockEntity;
import com.triassicisland.security.SecurityCameraBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TriassicIsland.MODID);

    private static final Map<MachineKind, RegistryObject<BlockEntityType<MachineBlockEntity>>> MACHINES = new EnumMap<>(MachineKind.class);

    static {
        for (MachineKind kind : MachineKind.values()) {
            MACHINES.put(kind, BLOCK_ENTITIES.register(kind.id(),
                    () -> BlockEntityType.Builder.of(kind.factory, ModBlocks.MACHINES.get(kind).get()).build(null)));
        }
    }

    public static final RegistryObject<BlockEntityType<CableBlockEntity>> CABLE = BLOCK_ENTITIES.register("cable",
            () -> BlockEntityType.Builder.of((pos, state) -> new CableBlockEntity(ModBlockEntities.CABLE.get(), pos, state),
                    ModBlocks.LOW_VOLTAGE_CABLE.get(), ModBlocks.MEDIUM_VOLTAGE_CABLE.get(), ModBlocks.HIGH_VOLTAGE_CABLE.get(),
                    ModBlocks.COMBINED_CABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<HiddenCableBlockEntity>> HIDDEN_CABLE = BLOCK_ENTITIES.register("hidden_cable",
            () -> BlockEntityType.Builder.of(HiddenCableBlockEntity::new,
                    ModBlocks.HIDDEN_CABLES.stream().map(RegistryObject::get).toArray(net.minecraft.world.level.block.Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<ComputerBlockEntity>> COMPUTER = BLOCK_ENTITIES.register("computer",
            () -> BlockEntityType.Builder.of(ComputerBlockEntity::new, ModBlocks.COMPUTER.get()).build(null));
    public static final RegistryObject<BlockEntityType<BatteryBlockEntity>> BATTERY = BLOCK_ENTITIES.register("battery",
            () -> BlockEntityType.Builder.of(BatteryBlockEntity::new, ModBlocks.BATTERY.get(), ModBlocks.LARGE_BATTERY.get()).build(null));
    public static final RegistryObject<BlockEntityType<TransformerBlockEntity>> TRANSFORMER = BLOCK_ENTITIES.register("transformer",
            () -> BlockEntityType.Builder.of(TransformerBlockEntity::new, ModBlocks.TRANSFORMER.get()).build(null));
    public static final RegistryObject<BlockEntityType<BreakerBlockEntity>> BREAKER = BLOCK_ENTITIES.register("circuit_breaker",
            () -> BlockEntityType.Builder.of(BreakerBlockEntity::new, ModBlocks.CIRCUIT_BREAKER.get()).build(null));
    public static final RegistryObject<BlockEntityType<WindTurbineBlockEntity>> WIND_TURBINE = BLOCK_ENTITIES.register("wind_turbine",
            () -> BlockEntityType.Builder.of(WindTurbineBlockEntity::new, ModBlocks.WIND_TURBINE.get()).build(null));
    public static final RegistryObject<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL = BLOCK_ENTITIES.register("solar_panel",
            () -> BlockEntityType.Builder.of(SolarPanelBlockEntity::new, ModBlocks.SOLAR_PANEL.get()).build(null));

    public static final RegistryObject<BlockEntityType<ElectricFenceBlockEntity>> ELECTRIC_FENCE = BLOCK_ENTITIES.register("electric_fence",
            () -> BlockEntityType.Builder.of(ElectricFenceBlockEntity::new, ModBlocks.ELECTRIC_FENCE.get(), ModBlocks.HIGH_VOLTAGE_FENCE.get()).build(null));
    public static final RegistryObject<BlockEntityType<ElectricFenceGateBlockEntity>> ELECTRIC_FENCE_GATE = BLOCK_ENTITIES.register("electric_fence_gate",
            () -> BlockEntityType.Builder.of(ElectricFenceGateBlockEntity::new, ModBlocks.ELECTRIC_FENCE_GATE.get()).build(null));
    public static final RegistryObject<BlockEntityType<FeederBlockEntity>> FEEDER = BLOCK_ENTITIES.register("feeder",
            () -> BlockEntityType.Builder.of(FeederBlockEntity::new, ModBlocks.FEEDER.get()).build(null));

    public static final RegistryObject<BlockEntityType<SecurityCameraBlockEntity>> SECURITY_CAMERA = BLOCK_ENTITIES.register("security_camera",
            () -> BlockEntityType.Builder.of(SecurityCameraBlockEntity::new, ModBlocks.SECURITY_CAMERA.get()).build(null));
    public static final RegistryObject<BlockEntityType<MonitorBlockEntity>> MONITOR = BLOCK_ENTITIES.register("monitor",
            () -> BlockEntityType.Builder.of(MonitorBlockEntity::new, ModBlocks.MONITOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<KeycardReaderBlockEntity>> KEYCARD_READER = BLOCK_ENTITIES.register("keycard_reader",
            () -> BlockEntityType.Builder.of(KeycardReaderBlockEntity::new, ModBlocks.KEYCARD_READER.get()).build(null));
    public static final RegistryObject<BlockEntityType<AlarmBlockEntity>> ALARM = BLOCK_ENTITIES.register("alarm",
            () -> BlockEntityType.Builder.of(AlarmBlockEntity::new, ModBlocks.ALARM.get()).build(null));

    public static final RegistryObject<BlockEntityType<LampBlockEntity>> LAMP = BLOCK_ENTITIES.register("lamp",
            () -> BlockEntityType.Builder.of(LampBlockEntity::new, ModBlocks.LAMP.get(), ModBlocks.FLOODLIGHT.get()).build(null));
    public static final RegistryObject<BlockEntityType<VariableTransformerBlockEntity>> VARIABLE_TRANSFORMER = BLOCK_ENTITIES.register("variable_transformer",
            () -> BlockEntityType.Builder.of(VariableTransformerBlockEntity::new, ModBlocks.VARIABLE_TRANSFORMER.get()).build(null));
    public static final RegistryObject<BlockEntityType<EnergyMeterBlockEntity>> ENERGY_METER = BLOCK_ENTITIES.register("energy_meter",
            () -> BlockEntityType.Builder.of(EnergyMeterBlockEntity::new, ModBlocks.ENERGY_METER.get()).build(null));
    public static final RegistryObject<BlockEntityType<FuseHolderBlockEntity>> FUSE_HOLDER = BLOCK_ENTITIES.register("fuse_holder",
            () -> BlockEntityType.Builder.of(FuseHolderBlockEntity::new, ModBlocks.FUSE_HOLDER.get()).build(null));

    public static final RegistryObject<BlockEntityType<PoleInsulatorBlockEntity>> POLE_INSULATOR = BLOCK_ENTITIES.register("pole_insulator",
            () -> BlockEntityType.Builder.<PoleInsulatorBlockEntity>of((pos, state) -> new PoleInsulatorBlockEntity(ModBlockEntities.POLE_INSULATOR.get(), pos, state),
                    ModBlocks.POLE_INSULATOR.get()).build(null));

    public static BlockEntityType<MachineBlockEntity> type(MachineKind kind) {
        return MACHINES.get(kind).get();
    }

    private ModBlockEntities() {}
}
