package supersymmetry.common.blocks;

import gregtech.api.util.BlockUtility;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.blocks.rocketry.*;

import java.util.*;
import java.util.stream.Collectors;

import static gregtech.common.blocks.MetaBlocks.ASPHALT;

public class SuSyBlocks {

    public static BlockSpacecraftInstrument SPACE_INSTRUMENT;
    public static BlockCoolingCoil COOLING_COIL;
    public static BlockSinteringBrick SINTERING_BRICK;
    public static BlockCoagulationTankWall COAGULATION_TANK_WALL;
    public static final EnumMap<SusyStoneVariantBlock.StoneVariant, SusyStoneVariantBlock> SUSY_STONE_BLOCKS = new EnumMap<>(SusyStoneVariantBlock.StoneVariant.class);
    public static BlockAlternatorCoil ALTERNATOR_COIL;
    public static BlockTurbineRotor TURBINE_ROTOR;
    public static BlockSeparatorRotor SEPARATOR_ROTOR;
    public static BlockDrillHead DRILL_HEAD;
    public static BlockDrillBit DRILL_BIT;
    public static BlockStructural STRUCTURAL_BLOCK;
    public static BlockStructural1 STRUCTURAL_BLOCK_1;
    public static BlockDeposit DEPOSIT_BLOCK;
    public static BlockResource RESOURCE_BLOCK;
    public static BlockResource1 RESOURCE_BLOCK_1;
    public static BlockHome HOME;
    public static BlockMultiblockTank MULTIBLOCK_TANK;
    public static BlockEvaporationBed EVAPORATION_BED;
    public static BlockElectrodeAssembly ELECTRODE_ASSEMBLY;
    public static BlockSuSyMultiblockCasing MULTIBLOCK_CASING;
    public static BlockSerpentine SERPENTINE;
    public static BlocksHardened HARDBLOCKS;
    public static BlocksCustomSheets CUSTOMSHEETS;
    public static BlockConveyor CONVEYOR_BELT;
    public static BlockRocketAssemblerCasing ROCKET_ASSEMBLER_CASING;

    public static BlockOuterHatch OUTER_HATCH;
    public static BlockInterStage INTERSTAGE;
    public static BlockFairingHull FAIRING_HULL;
    public static BlockRocketControl ROCKET_CONTROL;
    public static BlockTankShell TANK_SHELL;
    public static BlockTurboPump TURBOPUMP;
    public static BlockRocketNozzle ROCKET_NOZZLE;
    public static BlockCombustionChamber COMBUSTION_CHAMBER;
    public static BlockLifeSupport LIFE_SUPPORT;
    public static BlockRoomPadding ROOM_PADDING;
    public static BlockFairingConnector FAIRING_CONNECTOR;
    public static BlockSpacecraftHull SPACECRAFT_HULL;

    public static void init() {
        COOLING_COIL = new BlockCoolingCoil();
        COOLING_COIL.setRegistryName("cooling_coil");

        SINTERING_BRICK = new BlockSinteringBrick();
        SINTERING_BRICK.setRegistryName("sintering_brick");

        DRILL_HEAD = new BlockDrillHead();
        DRILL_HEAD.setRegistryName("drill_head");

        DRILL_BIT = new BlockDrillBit();
        DRILL_BIT.setRegistryName("drill_bit");

        COAGULATION_TANK_WALL = new BlockCoagulationTankWall();
        COAGULATION_TANK_WALL.setRegistryName("coagulation_tank_wall");

        for (SusyStoneVariantBlock.StoneVariant shape : SusyStoneVariantBlock.StoneVariant.values()) {
            SUSY_STONE_BLOCKS.put(shape, new SusyStoneVariantBlock(shape));
        }
        registerWalkingSpeedBonus();

        ALTERNATOR_COIL = new BlockAlternatorCoil();
        ALTERNATOR_COIL.setRegistryName("alternator_coil");

        TURBINE_ROTOR = new BlockTurbineRotor();
        TURBINE_ROTOR.setRegistryName("turbine_rotor");

        SEPARATOR_ROTOR = new BlockSeparatorRotor();
        SEPARATOR_ROTOR.setRegistryName("separator_rotor");

        STRUCTURAL_BLOCK = new BlockStructural();
        STRUCTURAL_BLOCK.setRegistryName("structural_block");

        STRUCTURAL_BLOCK_1 = new BlockStructural1();
        STRUCTURAL_BLOCK_1.setRegistryName("structural_block_1");

        DEPOSIT_BLOCK = new BlockDeposit();
        DEPOSIT_BLOCK.setRegistryName("deposit_block");

        RESOURCE_BLOCK = new BlockResource();
        RESOURCE_BLOCK.setRegistryName("resource_block");

        RESOURCE_BLOCK_1 = new BlockResource1();
        RESOURCE_BLOCK_1.setRegistryName("resource_block_1");

        HOME = new BlockHome();
        HOME.setRegistryName("home_block");

        EVAPORATION_BED = new BlockEvaporationBed();
        EVAPORATION_BED.setRegistryName("evaporation_bed");

        MULTIBLOCK_TANK = new BlockMultiblockTank();
        MULTIBLOCK_TANK.setRegistryName("multiblock_tank");

        ELECTRODE_ASSEMBLY = new BlockElectrodeAssembly();
        ELECTRODE_ASSEMBLY.setRegistryName("electrode_assembly");

        MULTIBLOCK_CASING = new BlockSuSyMultiblockCasing();
        MULTIBLOCK_CASING.setRegistryName("susy_multiblock_casing");

        SERPENTINE = new BlockSerpentine();
        SERPENTINE.setRegistryName("serpentine");

        HARDBLOCKS = new BlocksHardened();
        HARDBLOCKS.setRegistryName("hardened_blocks");

        CUSTOMSHEETS = new BlocksCustomSheets();
        CUSTOMSHEETS.setRegistryName("custom_sheets");

        CONVEYOR_BELT = new BlockConveyor();
        CONVEYOR_BELT.setRegistryName("conveyor_belt");

        ROCKET_ASSEMBLER_CASING = new BlockRocketAssemblerCasing();
        ROCKET_ASSEMBLER_CASING.setRegistryName("rocket_assembler_casing");

        OUTER_HATCH = new BlockOuterHatch();
        OUTER_HATCH.setRegistryName("rocket_outer_hatch");

        FAIRING_HULL = new BlockFairingHull();
        FAIRING_HULL.setRegistryName("rocket_fairing");

        ROCKET_CONTROL = new BlockRocketControl();
        ROCKET_CONTROL.setRegistryName("rocket_control");

        TANK_SHELL = new BlockTankShell();
        TANK_SHELL.setRegistryName("rocket_tank_shell");

        INTERSTAGE = new BlockInterStage();
        INTERSTAGE.setRegistryName("rocket_interstage");

        COMBUSTION_CHAMBER = new BlockCombustionChamber();
        COMBUSTION_CHAMBER.setRegistryName("rocket_combustion_chamber");

        TURBOPUMP = new BlockTurboPump();
        TURBOPUMP.setRegistryName("rocket_turbopump");

        ROCKET_NOZZLE = new BlockRocketNozzle();
        ROCKET_NOZZLE.setRegistryName("rocket_nozzle");

        ROCKET_ASSEMBLER_CASING = new BlockRocketAssemblerCasing();
        ROCKET_ASSEMBLER_CASING.setRegistryName("rocket_assembler_casing");

        LIFE_SUPPORT = new BlockLifeSupport();
        LIFE_SUPPORT.setRegistryName("spacecraft_life_support");

        SPACECRAFT_HULL = new BlockSpacecraftHull();
        SPACECRAFT_HULL.setRegistryName("spacecraft_hull");

        FAIRING_CONNECTOR = new BlockFairingConnector();
        FAIRING_CONNECTOR.setRegistryName("rocket_fairing_connector");

        ROOM_PADDING = new BlockRoomPadding();
        ROOM_PADDING.setRegistryName("spacecraft_room_padding");

        SPACE_INSTRUMENT = new BlockSpacecraftInstrument();
        SPACE_INSTRUMENT.setRegistryName("spacecraft_instrument");

    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        for (SusyStoneVariantBlock block : SUSY_STONE_BLOCKS.values())
            registerItemModel(block);
        Block[] toBeRegistered = {
                ALTERNATOR_COIL, COAGULATION_TANK_WALL, DRILL_HEAD, DRILL_BIT,
                TURBINE_ROTOR, SEPARATOR_ROTOR, STRUCTURAL_BLOCK, STRUCTURAL_BLOCK_1,
                DEPOSIT_BLOCK, RESOURCE_BLOCK, RESOURCE_BLOCK_1, HOME,
                INTERSTAGE, TANK_SHELL, OUTER_HATCH, FAIRING_HULL, ROCKET_CONTROL,
                ROCKET_NOZZLE, COMBUSTION_CHAMBER, TURBOPUMP, ROOM_PADDING,
                FAIRING_CONNECTOR, LIFE_SUPPORT, SPACECRAFT_HULL, HARDBLOCKS,
                CUSTOMSHEETS, CONVEYOR_BELT, ROCKET_ASSEMBLER_CASING, MULTIBLOCK_CASING
        };
        Arrays.stream(toBeRegistered).forEach(SuSyBlocks::registerItemModel);

        EVAPORATION_BED.onModelRegister();
        MULTIBLOCK_TANK.onModelRegister();
        ELECTRODE_ASSEMBLY.onModelRegister();
        SERPENTINE.onModelRegister();
        COOLING_COIL.onModelRegister();
        SINTERING_BRICK.onModelRegister();
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(@NotNull Block block) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(state.getProperties())));
        }
    }

    public static @NotNull String statePropertiesToString(@NotNull Map<IProperty<?>, Comparable<?>> properties) {
        StringBuilder stringbuilder = new StringBuilder();

        List<Map.Entry<IProperty<?>, Comparable<?>>> entries = properties.entrySet().stream()
                .sorted(Comparator.comparing(c -> c.getKey().getName()))
                .collect(Collectors.toList());

        for (Map.Entry<IProperty<?>, Comparable<?>> entry : entries) {
            if (stringbuilder.length() != 0) {
                stringbuilder.append(",");
            }

            IProperty<?> property = entry.getKey();
            stringbuilder.append(property.getName());
            stringbuilder.append("=");
            stringbuilder.append(getPropertyName(property, entry.getValue()));
        }

        if (stringbuilder.length() == 0) {
            stringbuilder.append("normal");
        }

        return stringbuilder.toString();
    }

    public static void registerWalkingSpeedBonus() {
        for (SusyStoneVariantBlock block : SUSY_STONE_BLOCKS.values()) {
            if (block.getWalkingSpeed() == 0)
                continue;
            for (IBlockState state : block.getBlockState().getValidStates())
                BlockUtility.setWalkingSpeedBonus(state, block.getWalkingSpeed());
        }
        for (IBlockState state : ASPHALT.getBlockState().getValidStates()) {
            BlockUtility.setWalkingSpeedBonus(state, 1); // Buff from 0.6F
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> @NotNull String getPropertyName(@NotNull IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }
}
