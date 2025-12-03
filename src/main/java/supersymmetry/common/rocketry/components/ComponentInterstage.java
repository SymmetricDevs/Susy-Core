package supersymmetry.common.rocketry.components;

import java.util.*;
import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;

public class ComponentInterstage extends AbstractComponent<ComponentInterstage> {

    public ComponentInterstage() {
        super(
                "interstage",
                "interstage",
                tuple -> {
                    return tuple.getSecond().stream()
                            .anyMatch(
                                    pos -> tuple
                                            .getFirst().world
                                                    .getBlockState(pos)
                                                    .getBlock()
                                                    .equals(SuSyBlocks.INTERSTAGE));
                });
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("mass", this.mass);
        tag.setDouble("radius", this.radius);
    }

    @Override
    public Optional<ComponentInterstage> readFromNBT(NBTTagCompound compound) {
        ComponentInterstage interstage = new ComponentInterstage();
        if (compound.getString("type") != this.type || compound.getString("name") != this.name)
            Optional.empty();
        if (!compound.hasKey("mass", NBT.TAG_DOUBLE)) Optional.empty();
        if (!compound.hasKey("radius", NBT.TAG_DOUBLE)) Optional.empty();
        if (!compound.hasKey("materials", NBT.TAG_LIST)) return Optional.empty();
        compound
                .getTagList("materials", NBT.TAG_COMPOUND)
                .forEach(x -> interstage.materials.add(MaterialCost.fromNBT((NBTTagCompound) x)));

        interstage.radius = compound.getDouble("radius");
        interstage.mass = compound.getDouble("mass");
        return Optional.of(interstage);
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
        Set<BlockPos> blocks = analysis.getBlockConn(aabb, analysis.getBlocks(analysis.world, aabb, true).get(0));
        StructAnalysis.HullData hullData = analysis.checkHull(aabb, blocks, false);

        Set<BlockPos> hullBlocks = hullData.exterior();
        Set<BlockPos> prevAir = null;
        if (!hullBlocks.containsAll(blocks)) {
            analysis.status = BuildStat.INTERSTAGE_NOT_CYLINDRICAL;
            return Optional.empty();
        }
        AxisAlignedBB bb = analysis.getBB(blocks);
        for (int i = (int) bb.minY; i < (int) bb.maxY; i++) {
            Set<BlockPos> air = analysis.getLayerAir(bb, i);
            if (prevAir != null) {
                for (BlockPos b : air) {
                    if (!prevAir.contains(b.add(0, -1, 0))) {
                        analysis.status = BuildStat.INTERSTAGE_NOT_CYLINDRICAL;
                        return Optional.empty();
                    }
                }
            }
            if (analysis.getPerimeter(air, StructAnalysis.layerVecs).size() >= Math.sqrt(air.size()) * 4) {
                analysis.status = BuildStat.INTERSTAGE_NOT_CYLINDRICAL;
                return Optional.empty();
            }
            prevAir = air;
        }
        double radius = analysis.getApproximateRadius(analysis.getLowestLayer(hullBlocks));
        double mass = 0;
        for (BlockPos block : blocks) {
            mass += getMassOfBlock(analysis.world.getBlockState(block));
        }
        NBTTagCompound tag = new NBTTagCompound();

        tag.setDouble("mass", Double.valueOf(mass));
        tag.setDouble("radius", Double.valueOf(radius));
        tag.setString("type", this.type);
        tag.setString("name", this.name);

        writeBlocksToNBT(blocks, analysis.world);

        analysis.status = BuildStat.SUCCESS;

        return Optional.of(tag);
    }
}
