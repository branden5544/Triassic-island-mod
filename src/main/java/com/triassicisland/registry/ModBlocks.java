package com.triassicisland.registry;

import com.triassicisland.TriassicIsland;
import com.triassicisland.computer.ComputerBlock;
import com.triassicisland.machine.MachineBlock;
import com.triassicisland.machine.MachineKind;
import com.triassicisland.park.ElectricFenceBlock;
import com.triassicisland.park.ElectricFenceGateBlock;
import com.triassicisland.park.FeederBlock;
import com.triassicisland.park.FenceTier;
import com.triassicisland.park.WarningSignBlock;
import com.triassicisland.power.BatteryBlock;
import com.triassicisland.power.BatteryBlockEntity;
import com.triassicisland.power.BreakerBlock;
import com.triassicisland.power.CableBlock;
import com.triassicisland.power.CableTier;
import com.triassicisland.power.EnergyMeterBlock;
import com.triassicisland.power.FuseHolderBlock;
import com.triassicisland.power.LampBlock;
import com.triassicisland.power.VariableTransformerBlock;
import com.triassicisland.power.Voltage;
import com.triassicisland.power.HiddenCableBlock;
import com.triassicisland.power.SimpleElectricalBlock;
import com.triassicisland.power.SolarPanelBlockEntity;
import com.triassicisland.power.TransformerBlock;
import com.triassicisland.power.WindTurbineBlock;
import com.triassicisland.light.TemporaryLightBlock;
import com.triassicisland.power.PoleInsulatorBlock;
import com.triassicisland.power.TowerPartBlock;
import com.triassicisland.power.UtilityPoleBlock;
import com.triassicisland.security.AlarmBlock;
import com.triassicisland.security.KeycardReaderBlock;
import com.triassicisland.security.MonitorBlock;
import com.triassicisland.security.SecurityCameraBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TriassicIsland.MODID);

    // Fossils: mined for fossil fragments, the start of the DNA chain.
    public static final RegistryObject<Block> FOSSIL_ORE = BLOCKS.register("fossil_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.STONE).requiresCorrectToolForDrops(), UniformInt.of(1, 3)));
    public static final RegistryObject<Block> DEEPSLATE_FOSSIL_ORE = BLOCKS.register("deepslate_fossil_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).requiresCorrectToolForDrops(), UniformInt.of(2, 4)));

    // Amber: can contain prehistoric mosquitoes carrying dinosaur blood.
    public static final RegistryObject<Block> AMBER_ORE = BLOCKS.register("amber_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.STONE).requiresCorrectToolForDrops(), UniformInt.of(1, 3)));

    // DNA lab machines and the generator, one block per kind.
    public static final Map<MachineKind, RegistryObject<Block>> MACHINES = new EnumMap<>(MachineKind.class);

    static {
        for (MachineKind kind : MachineKind.values()) {
            MACHINES.put(kind, BLOCKS.register(kind.id(), () -> new MachineBlock(kind, metal())));
        }
    }

    // Power grid
    public static final RegistryObject<Block> LOW_VOLTAGE_CABLE = cable("low_voltage_cable", CableTier.LOW);
    public static final RegistryObject<Block> MEDIUM_VOLTAGE_CABLE = cable("medium_voltage_cable", CableTier.MEDIUM);
    public static final RegistryObject<Block> HIGH_VOLTAGE_CABLE = cable("high_voltage_cable", CableTier.HIGH);
    public static final RegistryObject<Block> BATTERY = BLOCKS.register("battery",
            () -> new BatteryBlock(BatteryBlockEntity.Size.SMALL, metal()));
    public static final RegistryObject<Block> LARGE_BATTERY = BLOCKS.register("large_battery",
            () -> new BatteryBlock(BatteryBlockEntity.Size.LARGE, metal()));
    public static final RegistryObject<Block> TRANSFORMER = BLOCKS.register("transformer",
            () -> new TransformerBlock(metal()));
    public static final RegistryObject<Block> CIRCUIT_BREAKER = BLOCKS.register("circuit_breaker",
            () -> new BreakerBlock(metal()));
    public static final RegistryObject<Block> WIND_TURBINE = BLOCKS.register("wind_turbine",
            () -> new WindTurbineBlock(metal().noOcclusion()));
    public static final RegistryObject<Block> WIND_TURBINE_MAST = BLOCKS.register("wind_turbine_mast",
            () -> new TowerPartBlock(metal().noOcclusion().noLootTable(), () -> ModBlocks.WIND_TURBINE.get(),
                    WindTurbineBlock.HEIGHT, Block.box(6, 0, 6, 10, 16, 10), false));
    public static final RegistryObject<Block> SOLAR_PANEL = BLOCKS.register("solar_panel",
            () -> new SimpleElectricalBlock(metal().noOcclusion(), SolarPanelBlockEntity::new, Block.box(0, 0, 0, 16, 6, 16)));

    // More Electrical Age blocks
    public static final RegistryObject<Block> LAMP = BLOCKS.register("electric_lamp",
            () -> new LampBlock(Voltage.LOW, 15, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(0.8f).sound(SoundType.GLASS)));
    public static final RegistryObject<Block> FLOODLIGHT = BLOCKS.register("floodlight",
            () -> new LampBlock(Voltage.MEDIUM, 60, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(0.8f).sound(SoundType.GLASS)));
    public static final RegistryObject<Block> VARIABLE_TRANSFORMER = BLOCKS.register("variable_transformer",
            () -> new VariableTransformerBlock(metal()));
    public static final RegistryObject<Block> ENERGY_METER = BLOCKS.register("energy_meter",
            () -> new EnergyMeterBlock(metal()));
    public static final RegistryObject<Block> FUSE_HOLDER = BLOCKS.register("fuse_holder",
            () -> new FuseHolderBlock(metal()));

    public static final RegistryObject<Block> TEMPORARY_LIGHT = BLOCKS.register("temporary_light",
            () -> new TemporaryLightBlock(BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().noOcclusion().instabreak()));

    // Power poles and overhead lines
    public static final RegistryObject<Block> UTILITY_POLE = BLOCKS.register("utility_pole",
            () -> new UtilityPoleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2f).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> POLE_INSULATOR = BLOCKS.register("pole_insulator",
            () -> new PoleInsulatorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2f).sound(SoundType.WOOD).noOcclusion()));

    // Park
    public static final RegistryObject<Block> ELECTRIC_FENCE = BLOCKS.register("electric_fence",
            () -> new ElectricFenceBlock(FenceTier.STANDARD, metal().noOcclusion()));
    public static final RegistryObject<Block> HIGH_VOLTAGE_FENCE = BLOCKS.register("high_voltage_fence",
            () -> new ElectricFenceBlock(FenceTier.HIGH_VOLTAGE, metal().noOcclusion().strength(5f, 8f)));
    public static final RegistryObject<Block> ELECTRIC_FENCE_GATE = BLOCKS.register("electric_fence_gate",
            () -> new ElectricFenceGateBlock(metal().noOcclusion().strength(5f, 8f)));
    public static final RegistryObject<Block> FEEDER = BLOCKS.register("feeder",
            () -> new FeederBlock(metal().noOcclusion()));
    public static final RegistryObject<Block> HIGH_VOLTAGE_SIGN = BLOCKS.register("high_voltage_sign",
            () -> new WarningSignBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(1f).noCollission().noOcclusion()));

    // Data network and computers
    public static final RegistryObject<Block> DATA_CABLE = BLOCKS.register("data_cable",
            () -> new CableBlock(null, true, cableProperties()));
    public static final RegistryObject<Block> COMBINED_CABLE = BLOCKS.register("combined_cable",
            () -> new CableBlock(CableTier.LOW, true, cableProperties()));
    public static final RegistryObject<Block> HIDDEN_LOW_VOLTAGE_CABLE = hidden("hidden_low_voltage_cable", CableTier.LOW, false);
    public static final RegistryObject<Block> HIDDEN_MEDIUM_VOLTAGE_CABLE = hidden("hidden_medium_voltage_cable", CableTier.MEDIUM, false);
    public static final RegistryObject<Block> HIDDEN_HIGH_VOLTAGE_CABLE = hidden("hidden_high_voltage_cable", CableTier.HIGH, false);
    public static final RegistryObject<Block> HIDDEN_DATA_CABLE = hidden("hidden_data_cable", null, true);
    public static final RegistryObject<Block> HIDDEN_COMBINED_CABLE = hidden("hidden_combined_cable", CableTier.LOW, true);
    public static final List<RegistryObject<Block>> HIDDEN_CABLES = List.of(HIDDEN_LOW_VOLTAGE_CABLE, HIDDEN_MEDIUM_VOLTAGE_CABLE,
            HIDDEN_HIGH_VOLTAGE_CABLE, HIDDEN_DATA_CABLE, HIDDEN_COMBINED_CABLE);
    public static final RegistryObject<Block> COMPUTER = BLOCKS.register("computer", () -> new ComputerBlock(metal()));

    // Security
    public static final RegistryObject<Block> SECURITY_CAMERA = BLOCKS.register("security_camera",
            () -> new SecurityCameraBlock(metal().noOcclusion()));
    public static final RegistryObject<Block> MONITOR = BLOCKS.register("monitor",
            () -> new MonitorBlock(metal().noOcclusion()));
    public static final RegistryObject<Block> KEYCARD_READER = BLOCKS.register("keycard_reader",
            () -> new KeycardReaderBlock(metal().noOcclusion()));
    public static final RegistryObject<Block> ALARM = BLOCKS.register("alarm",
            () -> new AlarmBlock(metal().noOcclusion()));

    private static RegistryObject<Block> cable(String name, CableTier tier) {
        return BLOCKS.register(name, () -> new CableBlock(tier, false, cableProperties()));
    }

    private static RegistryObject<Block> hidden(String name, CableTier tier, boolean data) {
        return BLOCKS.register(name, () -> new HiddenCableBlock(tier, data,
                BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f, 6f).sound(SoundType.STONE)));
    }

    private static BlockBehaviour.Properties cableProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.5f).sound(SoundType.WOOL);
    }

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private ModBlocks() {}
}
