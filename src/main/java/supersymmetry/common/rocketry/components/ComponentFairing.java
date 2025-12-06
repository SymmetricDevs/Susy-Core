package supersymmetry.common.rocketry.components;

import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Constants.NBT;

import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockFairingConnector;
import supersymmetry.common.tile.TileEntityCoverable;

/**
 * ComponentFairing
 */
public class ComponentFairing extends AbstractComponent<ComponentFairing> {

    // Note: the radius is the radius of the bottom of the fairing
    public int height;

    public ComponentFairing() {
        super(
                "alu_fairing",
                "fairing",
                t -> {
                    return t.getSecond().stream()
                            .anyMatch(
                                    bp -> t.getFirst().world
                                            .getBlockState(bp)
                                            .getBlock()
                                            .equals(SuSyBlocks.FAIRING_HULL));
                });
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("bottom_radius", radius);
        tag.setInteger("height", height);
    }

    @Override
    public Optional<ComponentFairing> readFromNBT(NBTTagCompound compound) {
        ComponentFairing fairing = new ComponentFairing();
        if (compound.getString("type").isEmpty() || compound.getString("name").isEmpty())
            return Optional.empty();
        if (!compound.hasKey("height", Constants.NBT.TAG_INT)) return Optional.empty();
        if (!compound.hasKey("bottom_radius", Constants.NBT.TAG_DOUBLE)) return Optional.empty();
        if (!compound.hasKey("materials", NBT.TAG_LIST)) return Optional.empty();
        compound
                .getTagList("materials", NBT.TAG_COMPOUND)
                .forEach(x -> fairing.materials.add(MaterialCost.fromNBT((NBTTagCompound) x)));
        fairing.radius = compound.getDouble("bottom_radius");
        fairing.height = compound.getInteger("height");
        return Optional.of(fairing);
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB interiorBB) {
        Set<BlockPos> blocksConnected = analysis.getBlockConn(
                interiorBB, analysis.getBlocks(analysis.world, interiorBB, true).get(0));

        World world = analysis.world;
        AxisAlignedBB fairingBB = analysis.getBB(blocksConnected);
        Predicate<BlockPos> connCheck = bp -> world.getBlockState(bp).getBlock().equals(SuSyBlocks.FAIRING_CONNECTOR);
        Set<BlockPos> connectorBlocks = blocksConnected.stream().filter(connCheck).collect(Collectors.toSet());

        // If there are more than 2 partitions of air in the bounding box, then there's an abnormality
        // with the hull shape.
        // If there's only one partition, then the hull has a hole in it.
        // We know already that the exterior loop is touching the edge of the bounding box.
        // So, if there are 2 partitions, then the fairing hull is of the desired shape.

        List<HashSet<BlockPos>> partitions = analysis.getPartitions(fairingBB);
        if (partitions.size() != 2) {
            analysis.status = BuildStat.WEIRD_FAIRING;
            return Optional.empty();
        }
        // Objective: We don't really want tiles on the inside of the fairing - that wouldn't make any
        // sense!
        // So, we're going to obtain the fairing with a ceiling (if it didn't have one, it wouldn't have
        // )
        // Note that
        Set<BlockPos> intPartition;
        Set<BlockPos> extPartition;
        if (analysis.getBB(partitions.get(0)).maxY > analysis.getBB(partitions.get(1)).maxY) {
            intPartition = partitions.get(1);
            extPartition = partitions.get(0);
        } else {
            intPartition = partitions.get(0);
            extPartition = partitions.get(1);
        }

        // Checks if all connectors are facing the same plane
        BlockPos next = connectorBlocks.iterator().next();
        BlockPos start = next;
        EnumFacing dir = world.getBlockState(next).getValue(FACING);
        int axisPos = analysis.getCoordOfAxis(start);
        for (BlockPos blockFace : connectorBlocks) {
            EnumFacing dir_general = world.getBlockState(blockFace).getValue(FACING);
            if (analysis.getCoordOfAxis(blockFace) != axisPos || !dir_general.equals(dir)) {
                analysis.status = BuildStat.CONN_UNALIGNED;
                return Optional.empty();
            } else {
                BlockPos pointingTo = blockFace.add(dir_general.getDirectionVec());
                if (intPartition.contains(pointingTo) || extPartition.contains(pointingTo) ||
                        intPartition.contains(blockFace.add(dir_general.getOpposite().getDirectionVec()))) {
                    analysis.status = BuildStat.CONN_WRONG_DIR;
                    return Optional.empty();
                }
            }
        }

        // These connector blocks should form a line
        // Each connector should neighbor one/two

        // This will keep track of what we've covered
        Set<BlockPos> collectedConnectors = new HashSet<>(Collections.singleton(next));
        collectedConnectors.add(start);
        Set<BlockPos> toConnect = new HashSet<>();
        toConnect.add(start);
        while (!collectedConnectors.containsAll(connectorBlocks)) {
            if (toConnect
                    .isEmpty()) { // either there's one connector, or the connector set is disconnected
                analysis.status = BuildStat.WEIRD_FAIRING;
                return Optional.empty();
            }
            Set<BlockPos> newNeighbors = new HashSet<>();
            for (BlockPos initiate : toConnect) {
                Set<BlockPos> blockNeighbors = analysis
                        .getBlockNeighbors(initiate, fairingBB, StructAnalysis.neighborVecs).stream()
                        .filter(connectorBlocks::contains)
                        .collect(Collectors.toSet());
                // Ensures that there's one line of connectors (no branching)
                if (blockNeighbors.size() > 2) {
                    analysis.status = BuildStat.WEIRD_FAIRING;
                    return Optional.empty();
                }
                newNeighbors.addAll(
                        blockNeighbors.stream()
                                .filter(p -> !collectedConnectors.contains(p))
                                .collect(Collectors.toSet()));
            }
            collectedConnectors.addAll(toConnect);
            toConnect.clear();
            toConnect.addAll(newNeighbors);
        }

        // Step 3: Verify that all block faces exposed to the top partition are covered
        for (BlockPos bp : blocksConnected) {
            Block b = world.getBlockState(bp).getBlock();
            if (!(world.getTileEntity(bp) instanceof TileEntityCoverable)) {
                analysis.status = BuildStat.WEIRD_FAIRING;
                return Optional.empty();
            }
            TileEntityCoverable te = (TileEntityCoverable) world.getTileEntity(bp);

            // Takes all orth neighbors which are blocks or are interior neighbors
            List<BlockPos> solidNeighbors = analysis.getBlockNeighbors(bp, interiorBB, StructAnalysis.orthVecs).stream()
                    .filter(pos -> !world.isAirBlock(pos))
                    .collect(Collectors.toList());
            List<BlockPos> intAirNeighbors = analysis.getBlockNeighbors(bp, interiorBB, StructAnalysis.orthVecs)
                    .stream()
                    .filter(intPartition::contains)
                    .collect(Collectors.toList());
            for (EnumFacing facing : EnumFacing.VALUES) {
                boolean expectation = true;
                BlockPos pointingTo = bp.add(facing.getDirectionVec());
                if (pointingTo.getY() < fairingBB.minY) {
                    expectation = false;
                }
                if (solidNeighbors.contains(pointingTo) || intAirNeighbors.contains(pointingTo)) {
                    expectation = false;
                }
                if (world.getBlockState(bp).getValue(FACING).equals(facing) && b instanceof BlockFairingConnector) {
                    expectation = false;
                }
                if (expectation ^ te.isCovered(
                        facing)) { // xor: one is true, one is false, therefore the expectation is wrong
                    analysis.status = BuildStat.WRONG_TILE;
                    return Optional.empty();
                }
            }
        }
        double radius = analysis.getRadius(blocksConnected);
        int calculatedHeight = (int) (fairingBB.maxY - fairingBB.minY);
        if (calculatedHeight > radius * 2) {
            analysis.status = BuildStat.TOO_SHORT;
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("num_conns", connectorBlocks.size());
        tag.setInteger("volume", intPartition.size());

        // If it really is a semicircle, then the tightest radius has to be the same as the semicircle's.
        double bottomRadius = analysis.getRadius(analysis.getLowestLayer(blocksConnected));
        tag.setDouble("bottom_radius", bottomRadius);

        collectInfo(analysis, blocksConnected, tag);
        analysis.status = BuildStat.SUCCESS;
        writeBlocksToNBT(blocksConnected, analysis.world);

        return Optional.of(tag);
    }
}
