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

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static gregtech.common.blocks.MetaBlocks.ASPHALT;

public class SuSyBlocks {

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
    public static BlocksHardened1 HARDBLOCKS1;
    public static BlocksCustomSheets CUSTOMSHEETS;
    public static BlockMetallurgy METALLURGY;
    public static BlockMetallurgy2 METALLURGY_2;
    public static BlockMetallurgyRoll METALLURGY_ROLL;
    public static BlockConveyor CONVEYOR_BELT;
    public static BlockRocketAssemblerCasing ROCKET_ASSEMBLER_CASING;
    public static BlockRegolith REGOLITH;
    public static BlocksFakeWool FAKEWOOL;

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

        HARDBLOCKS1 = new BlocksHardened1();
        HARDBLOCKS1.setRegistryName("hardened_blocks1");

        CUSTOMSHEETS = new BlocksCustomSheets();
        CUSTOMSHEETS.setRegistryName("custom_sheets");

        METALLURGY = new BlockMetallurgy();
        METALLURGY.setRegistryName("metallurgy");

        METALLURGY_2 = new BlockMetallurgy2();
        METALLURGY_2.setRegistryName("metallurgy_2");

        METALLURGY_ROLL = new BlockMetallurgyRoll();
        METALLURGY_ROLL.setRegistryName("metallurgy_roll");

        CONVEYOR_BELT = new BlockConveyor();
        CONVEYOR_BELT.setRegistryName("conveyor_belt");
      
        ROCKET_ASSEMBLER_CASING = new BlockRocketAssemblerCasing();
        ROCKET_ASSEMBLER_CASING.setRegistryName("rocket_assembler_casing");

        REGOLITH = new BlockRegolith();
        REGOLITH.setRegistryName("regolith");

        FAKEWOOL = new BlocksFakeWool();
        FAKEWOOL.setRegistryName("fake_wool");
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        COOLING_COIL.onModelRegister();
        SINTERING_BRICK.onModelRegister();
        registerItemModel(COAGULATION_TANK_WALL);
        for (SusyStoneVariantBlock block : SUSY_STONE_BLOCKS.values())
            registerItemModel(block);
        registerItemModel(ALTERNATOR_COIL);
        registerItemModel(DRILL_HEAD);
        registerItemModel(DRILL_BIT);
        registerItemModel(TURBINE_ROTOR);
        registerItemModel(SEPARATOR_ROTOR);
        registerItemModel(STRUCTURAL_BLOCK);
        registerItemModel(STRUCTURAL_BLOCK_1);
        registerItemModel(DEPOSIT_BLOCK);
        registerItemModel(RESOURCE_BLOCK);
        registerItemModel(RESOURCE_BLOCK_1);
        registerItemModel(HOME);
        EVAPORATION_BED.onModelRegister();
        MULTIBLOCK_TANK.onModelRegister();
        ELECTRODE_ASSEMBLY.onModelRegister();
        registerItemModel(MULTIBLOCK_CASING);
        SERPENTINE.onModelRegister();
        registerItemModel(HARDBLOCKS);
        registerItemModel(HARDBLOCKS1);
        registerItemModel(CUSTOMSHEETS);
        registerItemModel(METALLURGY);
        registerItemModel(METALLURGY_2);
        registerItemModel(METALLURGY_ROLL);
        registerItemModel(CONVEYOR_BELT);
        registerItemModel(ROCKET_ASSEMBLER_CASING);
        registerItemModel(REGOLITH);
        registerItemModel(FAKEWOOL);
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
