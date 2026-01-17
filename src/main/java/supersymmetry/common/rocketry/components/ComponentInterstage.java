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
                tuple -> tuple.getSecond().stream()
                        .anyMatch(
                                pos -> tuple
                                        .getFirst().world
                                                .getBlockState(pos)
                                                .getBlock()
                                                .equals(SuSyBlocks.INTERSTAGE)));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("mass", this.mass);
        tag.setDouble("radius", this.radius);
    }

    @Override
    public Optional<ComponentInterstage> readFromNBT(NBTTagCompound compound) {
        if (!this.type.equals(compound.getString("type")) || !this.name.equals(compound.getString("name"))) {
            return Optional.empty();
        }
        if (!compound.hasKey("mass", NBT.TAG_DOUBLE)) {
            return Optional.empty();
        }
        if (!compound.hasKey("radius", NBT.TAG_DOUBLE)) {
            return Optional.empty();
        }
        if (!compound.hasKey("materials", NBT.TAG_LIST)) {
            return Optional.empty();
        }

        ComponentInterstage interstage = new ComponentInterstage();
        compound
                .getTagList("materials", NBT.TAG_COMPOUND)
                .forEach(tag -> interstage.materials.add(MaterialCost.fromNBT((NBTTagCompound) tag)));

        interstage.radius = compound.getDouble("radius");
        interstage.mass = compound.getDouble("mass");
        return Optional.of(interstage);
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB bounds) {
        List<BlockPos> initialBlocks = analysis.getBlocks(analysis.world, bounds, true);
        if (initialBlocks.isEmpty()) {
            analysis.status = BuildStat.ERROR;
            return Optional.empty();
        }

        Set<BlockPos> connectedBlocks = analysis.getBlockConn(bounds, initialBlocks.get(0));
        StructAnalysis.HullData hullData = analysis.checkHull(bounds, connectedBlocks, false);

        Set<BlockPos> hullBlocks = hullData.exterior();
        if (!hullBlocks.containsAll(connectedBlocks)) {
            analysis.status = BuildStat.INTERSTAGE_NOT_CYLINDRICAL;
            return Optional.empty();
        }

        Set<BlockPos> previousAirLayer = null;
        AxisAlignedBB hullBounds = analysis.getBB(connectedBlocks);
        for (int y = (int) hullBounds.minY; y < (int) hullBounds.maxY; y++) {
            Set<BlockPos> airLayer = analysis.getLayerAir(hullBounds, y);
            if (previousAirLayer != null) {
                for (BlockPos blockPos : airLayer) {
                    if (!previousAirLayer.contains(blockPos.add(0, -1, 0))) {
                        analysis.status = BuildStat.INTERSTAGE_NOT_CYLINDRICAL;
                        return analysis.errorPos(blockPos);
                    }
                }
            }

            int perim = analysis.getPerimeter(airLayer, StructAnalysis.layerVecs).size();
            // excludes squares. don't ask
            if (airLayer.size() / (double) (perim * perim) < 0.07 / (1 + Math.exp(-0.17 * perim))) {
                analysis.status = BuildStat.INTERSTAGE_NOT_CYLINDRICAL;
                return Optional.empty();
            }
            previousAirLayer = airLayer;
        }

        double radius = analysis.getRadius(analysis.getLowestLayer(hullBlocks));

        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("radius", radius);

        collectInfo(analysis, connectedBlocks, tag);

        writeBlocksToNBT(connectedBlocks, analysis.world);
        analysis.status = BuildStat.SUCCESS;
        return Optional.of(tag);
    }
}
