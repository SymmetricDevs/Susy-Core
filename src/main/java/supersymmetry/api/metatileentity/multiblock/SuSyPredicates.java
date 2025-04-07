package supersymmetry.api.metatileentity.multiblock;

import cam72cam.immersiverailroading.IRBlocks;
import gregtech.api.pattern.PatternStringError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import supersymmetry.SuSyValues;
import supersymmetry.common.blocks.BlockConveyor;
import supersymmetry.common.blocks.BlockCoolingCoil;
import supersymmetry.common.blocks.BlockSinteringBrick;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.Arrays;
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
                blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.sintering_bricks"));
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
                                blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.conveyor"));
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
                    ).addTooltips("gregtech.multiblock.pattern.error.conveyor")));

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
}
