package supersymmetry.common.blocks;

import static gregtech.common.blocks.MetaBlocks.ASPHALT;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantBlock;
import gregtech.api.util.BlockUtility;
import supersymmetry.common.blocks.rocketry.*;
import supersymmetry.common.tileentities.SuSyTileEntities;

public class SuSyBlocks {

    public static BlockSpacecraftInstrument SPACE_INSTRUMENT;
    public static BlockCoolingCoil COOLING_COIL;
    public static BlockSinteringBrick SINTERING_BRICK;
    public static BlockCoagulationTankWall COAGULATION_TANK_WALL;
    public static final EnumMap<SusyStoneVariantBlock.StoneVariant, SusyStoneVariantBlock> SUSY_STONE_BLOCKS = new EnumMap<>(
            SusyStoneVariantBlock.StoneVariant.class);
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
    public static BlockRandomConcrete RANDOM_CONCRETE;
    public static BlockRandomConcrete1 RANDOM_CONCRETE1;
    public static BlockInductionCoilAssembly INDUCTION_COIL_ASSEMBLY;
    public static BlockEngineCasing ENGINE_CASING;
    public static BlockEngineCasing2 ENGINE_CASING_2;
    public static BlocksActiveCasing ACTIVE_CASING;
    public static BlockSupport SUPPORT;

    public static BlockSuSyRocketMultiblockCasing ROCKET_MULTIBLOCK_CASING;

    public static BlockOuterHatch OUTER_HATCH;
    public static BlockInterStage INTERSTAGE;
    public static BlockFairingHull FAIRING_HULL;
    public static BlockRocketControl ROCKET_CONTROL;
    public static BlockTankShell TANK_SHELL;
    public static BlockTankShell1 TANK_SHELL1;
    public static BlockTurboPump TURBOPUMP;
    public static BlockRocketNozzle ROCKET_NOZZLE;
    public static BlockCombustionChamber COMBUSTION_CHAMBER;
    public static BlockLifeSupport LIFE_SUPPORT;
    public static BlockRoomPadding ROOM_PADDING;
    public static BlockFairingConnector FAIRING_CONNECTOR;
    public static BlockSpacecraftHull SPACECRAFT_HULL;
    public static BlockEccentricRoll ECCENTRIC_ROLL;
    public static BlockGrinderCasing GRINDER_CASING;
    public static BlockGirthGearTooth GIRTH_GEAR_TOOTH;

    public static ArrayList<VariantBlock<?>> susyBlocks;

    public static void init() {
        for (SusyStoneVariantBlock.StoneVariant shape : SusyStoneVariantBlock.StoneVariant.values()) {
            SUSY_STONE_BLOCKS.put(shape, new SusyStoneVariantBlock(shape));
        }
        registerWalkingSpeedBonus();
        susyBlocks = new ArrayList<>();
        // Test all fields
        for (Field field : SuSyBlocks.class.getDeclaredFields()) {
            if (VariantBlock.class.isAssignableFrom(field.getType())) {
                // Try block is necessary in case getDeclaredConstructor does not exist (though it should)
                try {
                    VariantBlock<?> newBlock = (VariantBlock<?>) field.getType().getDeclaredConstructor().newInstance();
                    // the 5 is used because getTranslationKey leaves ".file" at the start
                    newBlock.setRegistryName(newBlock.getTranslationKey().substring(5));
                    field.set(null, newBlock);
                    susyBlocks.add(newBlock);
                } catch (Exception e) {
                    System.out.println("Field " + field.getName() + " of type " + field.getType() +
                            " is a variant block in SuSyBlocks and yet is not valid");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }

        REGOLITH = new BlockRegolith();
        REGOLITH.setRegistryName("regolith");

        SuSyTileEntities.register();
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        COOLING_COIL.onModelRegister();
        SINTERING_BRICK.onModelRegister();
        registerItemModel(COAGULATION_TANK_WALL);
        for (SusyStoneVariantBlock block : SUSY_STONE_BLOCKS.values())
            registerItemModel(block);
        susyBlocks.forEach(b -> {
            if (b instanceof VariantActiveBlock) ((VariantActiveBlock<?>) b).onModelRegister();
            else registerItemModel(b);
        });
        registerItemModel(REGOLITH);
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
    private static <T extends Comparable<T>> @NotNull String getPropertyName(@NotNull IProperty<T> property,
                                                                             Comparable<?> value) {
        return property.getName((T) value);
    }
}
