package supersymmetry.common.rocketry.components;

import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;

import java.util.*;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.Constants;

import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.tile.TileEntityCoverable;

/** componentLiquidFuelTank */
public class ComponentLiquidFuelTank extends AbstractComponent<ComponentLiquidFuelTank> {

    public int volume;
    public double radius;

    public ComponentLiquidFuelTank() {
        super(
                "fluid_tank",
                "tank",
                thing -> {
                    return thing.getSecond().stream()
                            .anyMatch(
                                    bp -> thing.getFirst().world
                                            .getBlockState(bp)
                                            .getBlock()
                                            .equals(SuSyBlocks.TANK_SHELL));
                });
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("radius", this.radius);
        tag.setDouble("mass", this.mass);
        tag.setInteger("volume", this.volume);
    }

    // but the Lord laughs at the wicked,
    // for he knows their day is coming.
    @Override
    public Optional<ComponentLiquidFuelTank> readFromNBT(NBTTagCompound compound) {
        if (compound.getString("type") != this.type || compound.getString("name") != this.name)
            Optional.empty();
        ComponentLiquidFuelTank tank = new ComponentLiquidFuelTank();
        if (!compound.hasKey("mass", Constants.NBT.TAG_DOUBLE)) Optional.empty();
        if (!compound.hasKey("radius", Constants.NBT.TAG_DOUBLE)) Optional.empty();
        if (!compound.hasKey("area_ratio", Constants.NBT.TAG_DOUBLE)) Optional.empty();
        if (!compound.hasKey("materials", Constants.NBT.TAG_LIST)) return Optional.empty();
        compound
                .getTagList("materials", Constants.NBT.TAG_COMPOUND)
                .forEach(x -> tank.materials.add(MaterialCost.fromNBT((NBTTagCompound) x)));
        tank.volume = compound.getInteger("volume");
        tank.radius = compound.getDouble("radius");
        tank.mass = compound.getDouble("mass");

        return Optional.of(tank);
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
        Set<BlockPos> blocks = analysis.getBlockConn(aabb, analysis.getBlocks(analysis.world, aabb, true).get(0));
        Tuple<Set<BlockPos>, Set<BlockPos>> hullData = analysis.checkHull(aabb, blocks, false);

        Set<BlockPos> hullBlocks = hullData.getFirst();
        Set<BlockPos> interiorAir = hullData.getSecond();

        if (interiorAir.size() < 2) {
            analysis.status = BuildStat.HULL_FULL;
            return Optional.empty();
        }

        Predicate<BlockPos> fuelPredicate = (block) -> {
            Block b = analysis.world.getBlockState(block).getBlock();
            return b.equals(SuSyBlocks.TANK_SHELL) || b.equals(SuSyBlocks.TANK_SHELL1);
        };
        for (BlockPos block : hullBlocks) {
            if (!fuelPredicate.test(block)) {
                analysis.status = BuildStat.HULL_WEAK;
                return Optional.empty();
            }
            TileEntityCoverable blockTiles = (TileEntityCoverable) analysis.world.getTileEntity(block);
            if (blockTiles == null) {
                analysis.status = BuildStat.ERROR;
                return Optional.empty();
            }
            EnumFacing dir = analysis.world.getBlockState(block).getValue(FACING);
            // ArrayList<BlockPos> neighbors = analysis.getBlockNeighbors(block, StructAnalysis.orthVecs);
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos neighbor = block.add(facing.getDirectionVec());
                if (interiorAir.contains(neighbor)) {
                    Vec3i diff = analysis.diff(neighbor, block);
                    if (!diff.equals(dir.getOpposite().getDirectionVec())) { // incorrect with honeycombs
                        analysis.status = BuildStat.HULL_WEAK;
                        return Optional.empty();
                    }
                } else if (!interiorAir.contains(neighbor) &&
                        (analysis.world.isAirBlock(neighbor) || !StructAnalysis.blockCont(
                                aabb, neighbor))) { // this means it should be exterior air
                                    if (!blockTiles.isCovered(facing)) {
                                        analysis.status = BuildStat.MISSING_TILE;
                                        return Optional.empty();
                                    }
                                }
            }
        }

        double radius = analysis.getApproximateRadius(blocks);
        int height = (int) (analysis.getBB(blocks).maxZ - analysis.getBB(blocks).minZ);
        if (height > radius * 2) {
            analysis.status = BuildStat.TOO_SHORT;
        }
        NBTTagCompound tag = new NBTTagCompound();

        // The scan is successful by this point
        analysis.status = BuildStat.SUCCESS;
        tag.setInteger("volume", ((Integer) interiorAir.size()));
        this.volume = interiorAir.size();
        tag.setString("type", this.type);
        tag.setString("name", this.name);
        tag.setDouble("radius", Double.valueOf(radius));
        this.radius = radius;
        double mass = 0;
        for (BlockPos block : blocks) {
            mass += getMass(analysis.world.getBlockState(block));
        }
        tag.setDouble("mass", Double.valueOf(mass));
        this.mass = mass;
        writeBlocksToNBT(blocks, analysis.world);
        return Optional.of(tag);
    }
}
