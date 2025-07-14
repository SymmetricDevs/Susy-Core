package supersymmetry.api.metatileentity.multiblock;

import cam72cam.immersiverailroading.IRBlocks;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.PatternStringError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.common.blocks.BlockColored;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import supersymmetry.SuSyValues;
import supersymmetry.api.blocks.VariantAxialRotatableBlock;
import supersymmetry.common.blocks.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Class containing global predicates
 */
public class SuSyPredicates {

    private static final Supplier<TraceabilityPredicate> COOLING_COILS = () -> new TraceabilityPredicate(blockWorldState -> {
        IBlockState state = blockWorldState.getBlockState();
        if (state.getBlock() instanceof BlockCoolingCoil) {
            BlockCoolingCoil.CoolingCoilType type = SuSyBlocks.COOLING_COIL.getState(state);
            Object currentCoil = blockWorldState.getMatchContext().getOrPut("CoolingCoilType", type);
            if (!currentCoil.equals(type)) {
                blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.coils"));
                return false;
            }
            blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
            return true;
        }
        return false;
    }, () -> Arrays.stream(BlockCoolingCoil.CoolingCoilType.values())
            .map(type -> new BlockInfo(SuSyBlocks.COOLING_COIL.getState(type)))
            .toArray(BlockInfo[]::new)
    );

    private static final Supplier<TraceabilityPredicate> SINTERING_BRICKS = () -> new TraceabilityPredicate(blockWorldState -> {
        IBlockState state = blockWorldState.getBlockState();
        if (state.getBlock() instanceof BlockSinteringBrick) {
            BlockSinteringBrick.SinteringBrickType type = SuSyBlocks.SINTERING_BRICK.getState(state);
            Object currentBrick = blockWorldState.getMatchContext().getOrPut("SinteringBrickType", type);
            if (!currentBrick.equals(type)) {
                blockWorldState.setError(new PatternStringError("susy.multiblock.pattern.error.sintering_bricks"));
                return false;
            }
            blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
            return true;
        }
        return false;
    }, () -> Arrays.stream(BlockSinteringBrick.SinteringBrickType.values())
            .map(type -> new BlockInfo(SuSyBlocks.SINTERING_BRICK.getState(type)))
            .toArray(BlockInfo[]::new)
    );

    private static final Supplier<TraceabilityPredicate> RAILS = () -> new TraceabilityPredicate(blockWorldState -> {
        if (!Loader.isModLoaded(SuSyValues.MODID_IMMERSIVERAILROADING)) return true;

        IBlockState state = blockWorldState.getBlockState();

        Block block = state.getBlock();

        return block == IRBlocks.BLOCK_RAIL.internal || block == IRBlocks.BLOCK_RAIL_GAG.internal;
    });

    // Allow all conveyor belts, and require them to have the same type.
    // This will create a list of Pair<BlockPos, RelativeDirection> allowing the multiblock to reorient the facing.
    private static final Map<RelativeDirection, Supplier<TraceabilityPredicate>> CONVEYOR_BELT =
            Arrays.stream(RelativeDirection.values()).collect(Collectors.toMap(facing -> facing,
                    facing -> () -> new TraceabilityPredicate(blockWorldState -> {
                        IBlockState state = blockWorldState.getBlockState();
                        if (state.getBlock() instanceof BlockConveyor) {
                            // Check conveyor type
                            BlockConveyor.ConveyorType type = ((BlockConveyor) state.getBlock()).getState(state);
                            Object currentConveyor = blockWorldState.getMatchContext().getOrPut("ConveyorType", type);
                            if (!currentConveyor.equals(type)) {
                                blockWorldState.setError(new PatternStringError("susy.multiblock.pattern.error.conveyor"));
                                return false;
                            }
                            // Adds the position of the conveyor (and target facing) to the match context
                            blockWorldState.getMatchContext().getOrPut("ConveyorBelt", new LinkedList<>())
                                    .add(Pair.of(blockWorldState.getPos(), facing));
                            return true;
                        }
                        return false;
                    }, () -> Arrays.stream(BlockConveyor.ConveyorType.values())
                            .map(entry -> new BlockInfo(SuSyBlocks.CONVEYOR_BELT.getState(entry), null))
                            .toArray(BlockInfo[]::new)
                    ).addTooltips("susy.multiblock.pattern.error.conveyor")));

    public static Supplier<TraceabilityPredicate> COILS_OR_BED = () -> new TraceabilityPredicate(blockWorldState -> {
        IBlockState blockState = blockWorldState.getBlockState();
        if (GregTechAPI.HEATING_COILS.containsKey(blockState) || blockState == SuSyBlocks.EVAPORATION_BED.getDefaultState()) {
            IHeatingCoilBlockStats stats = GregTechAPI.HEATING_COILS.get(blockState);
            Object key = (stats == null ? ' ' : stats);
            Object current = blockWorldState.getMatchContext().getOrPut("CoilType", key);
            if (!current.equals(key)) {
                blockWorldState.setError(new PatternStringError("susy.multiblock.pattern.error.coils_or_bed"));
                return false;
            }
            blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
            return true;
        }
        return false;
    }, () -> GregTechAPI.HEATING_COILS.entrySet().stream()
            // sort to make autogenerated jei previews not pick random coils each game load
            .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
            .map(entry -> new BlockInfo(entry.getKey(), null))
            .toArray(BlockInfo[]::new))
            .addTooltips("susy.multiblock.pattern.error.coils_or_bed");

    private static final Supplier<TraceabilityPredicate> EVAP_BED = () -> new TraceabilityPredicate(blockWorldState -> false, () ->
            new BlockInfo[]{new BlockInfo(SuSyBlocks.EVAPORATION_BED.getDefaultState())})
            .addTooltips("susy.multiblock.pattern.error.coils_or_bed");

    /**
     * A predicate for allowing using only the same type of metal sheet blocks in a structure
     * This includes predicate for both small & large metal sheets
     * but only supplies {@link BlockInfo[]} for small ones
     *
     * @see #LARGE_METAL_SHEETS
     */
    private static final Supplier<TraceabilityPredicate> METAL_SHEETS = () -> new TraceabilityPredicate(blockWorldState -> {
        IBlockState state = blockWorldState.getBlockState();
        if (state.getBlock() instanceof BlockColored colored) {
            IBlockState defaultState = colored.getDefaultState();
            int colorValue = colored.getState(state).getMetadata();
            int typeValue = defaultState == MetaBlocks.METAL_SHEET.getDefaultState() ? 0 :
                    defaultState == MetaBlocks.LARGE_METAL_SHEET.getDefaultState() ? 1 : -1;
            if (typeValue >= 0) {
                byte value = (byte) (typeValue << 4 | colorValue);
                Object currentCoil = blockWorldState.getMatchContext().getOrPut("MetalSheet", value);
                if (!currentCoil.equals(value)) {
                    blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.metal_sheets"));
                    return false;
                }
                return true;
            }
        }
        return false;
    }, () -> Arrays.stream(EnumDyeColor.values())
            .map(type -> new BlockInfo(MetaBlocks.METAL_SHEET.getState(type)))
            .toArray(BlockInfo[]::new)
    );

    /**
     * This only supplies {@link BlockInfo[]} for large metal sheet blocks.
     *
     * @see #METAL_SHEETS
     */
    private static final Supplier<TraceabilityPredicate> LARGE_METAL_SHEETS = () -> new TraceabilityPredicate(blockWorldState -> false, () -> Arrays.stream(EnumDyeColor.values())
            .map(type -> new BlockInfo(MetaBlocks.LARGE_METAL_SHEET.getState(type)))
            .toArray(BlockInfo[]::new)
    );

    @NotNull
    public static TraceabilityPredicate coolingCoils() {
        return COOLING_COILS.get();
    }

    @NotNull
    public static TraceabilityPredicate sinteringBricks() {
        return SINTERING_BRICKS.get();
    }

    @NotNull
    public static TraceabilityPredicate rails() {
        return RAILS.get();
    }

    @NotNull
    public static TraceabilityPredicate conveyorBelts(RelativeDirection facing) {
        return CONVEYOR_BELT.get(facing).get();
    }

    @NotNull
    public static TraceabilityPredicate coilsOrBeds() {
        return COILS_OR_BED.get().or(EVAP_BED.get());
    }

    @NotNull
    public static TraceabilityPredicate metalSheets() {
        return METAL_SHEETS.get().or(LARGE_METAL_SHEETS.get());
    }

    /**
     * @param facing the axis direction of the eccentric roll (rotates CCW)
     * <p>
     * Supplies predicates for each facing direction
     * This autocorrects the facing of the eccentric roll
     * and adds the position of the eccentric roll to the match context
     */
    @NotNull
    public static TraceabilityPredicate eccentricRolls(EnumFacing facing) {

        return new TraceabilityPredicate(bws -> {
            IBlockState state = bws.getBlockState();
            if (state.getBlock() instanceof BlockEccentricRoll) {

                // Corrects the direction of the eccentric roll, while ignoring the in/active state
                if (state.getValue(BlockDirectional.FACING) != facing) {
                    World world = bws.getWorld();
                    BlockPos pos = bws.getPos();
                    world.setBlockState(pos, state.withProperty(BlockDirectional.FACING, facing));
                }

                /// Adds the position of the eccentric roll to the match context
                /// This works much like how CEu deals with VAActiveBlocks (e.g. coils)
                /// @see MultiblockControllerBase#states(IBlockState...)
                bws.getMatchContext().getOrPut("ERC_Rolls", new LinkedList<>()).add(bws.getPos());
                return true;
            }
            return false;
            // Supplies an eccentric roll with the correct direction
        }, () -> new BlockInfo[]{new BlockInfo(SuSyBlocks.ECCENTRIC_ROLL.getDefaultState()
                .withProperty(BlockDirectional.FACING, facing))}
        );
    }

    @NotNull
    public static TraceabilityPredicate hiddenStates(IBlockState... allowedStates) {
            return new TraceabilityPredicate(bws -> {
                IBlockState state = bws.getBlockState();
                bws.getMatchContext().getOrPut("Hidden", new LinkedList<>()).add(bws.getPos());
                return ArrayUtils.contains(allowedStates, state);
            }, () -> Arrays.stream(allowedStates).map(state -> new BlockInfo(state, null)).toArray(BlockInfo[]::new));
    }

    @NotNull
    public static TraceabilityPredicate hiddenGearTooth(EnumFacing.Axis axis) {

        return new TraceabilityPredicate(bws -> {
            IBlockState state = bws.getBlockState();
            if (state.getBlock() instanceof BlockGirthGearTooth) {

                if (state.getValue(VariantAxialRotatableBlock.AXIS) != axis) {
                    World world = bws.getWorld();
                    BlockPos pos = bws.getPos();
                    world.setBlockState(pos, state.withProperty(VariantAxialRotatableBlock.AXIS, axis));
                }

                bws.getMatchContext().getOrPut("Hidden", new LinkedList<>()).add(bws.getPos());
                return true;
            }
            return false;
            // Supplies an eccentric roll with the correct direction
        }, () -> new BlockInfo[]{new BlockInfo(SuSyBlocks.GIRTH_GEAR_TOOTH.getDefaultState()
                .withProperty(VariantAxialRotatableBlock.AXIS, axis))}
        );
    }
}
