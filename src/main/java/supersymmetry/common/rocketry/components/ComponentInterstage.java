package supersymmetry.common.rocketry.components;

import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.tileentities.TileEntityCoverable;

import static supersymmetry.common.blocks.SuSyBlocks.INTERSTAGE;

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
                                                .equals(INTERSTAGE)));
    }

    @Override
    public boolean configureDefaults() {
        this.materials.add(new MaterialCost(new ItemStack(Items.DIAMOND), MaterialCost.SourceType.ITEM, 1));
        this.radius = 3.0;
        this.mass = 1000.0;
        return true;
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
            if (airLayer.isEmpty()) {
                analysis.status = BuildStat.INTERSTAGE_NOT_CYLINDRICAL;
                return Optional.empty();
            }

            if (previousAirLayer != null &&
                    !previousAirLayer.equals(airLayer.stream()
                    .map(pos -> new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()))
                    .collect(Collectors.toSet()))
            ) {
                analysis.status = BuildStat.INTERSTAGE_NOT_CYLINDRICAL;
                return Optional.empty();
            }
            for (BlockPos blockPos : airLayer) {
                // Check solid neighbors for proper covering
                for (EnumFacing facing: EnumFacing.HORIZONTALS) {
                    BlockPos neighbor = blockPos.add(facing.getDirectionVec());
                    if (connectedBlocks.contains(neighbor)) {
                        if (!analysis.world.getBlockState(neighbor).getBlock().equals(INTERSTAGE))
                        {
                            analysis.status = BuildStat.NOT_INTERSTAGE;
                            return analysis.errorPos(neighbor);
                        }
                        TileEntityCoverable tile = (TileEntityCoverable)
                                analysis.world.getTileEntity(neighbor);
                        if (tile.getCoverCount() == 0) {
                            analysis.status = BuildStat.WRONG_TILE;
                            return analysis.errorPos(neighbor);
                        }
                        for (EnumFacing otherFace: tile.getSides()) {
                            if (otherFace.getAxis().equals(EnumFacing.Axis.Y) ||
                                otherFace.getOpposite().equals(facing) ||
                                connectedBlocks.contains(neighbor.add(
                                        otherFace.getDirectionVec()
                                ))) {
                                if (tile.isCovered(otherFace)) {
                                    analysis.status = BuildStat.WRONG_TILE;
                                    return analysis.errorPos(neighbor);
                                }

                            } else if (!tile.isCovered(otherFace)) {
                                analysis.status = BuildStat.WRONG_TILE;
                                return analysis.errorPos(neighbor);
                            }
                        }
                    }
                }
            }

            int perim = analysis.getPerimeter(airLayer, StructAnalysis.layerVecs).size();
            // excludes awkward structures
            if ((perim*perim)/(double)airLayer.size() > 16) {
                analysis.status = BuildStat.INTERSTAGE_NOT_CYLINDRICAL;
                return Optional.empty();
            }
            previousAirLayer = airLayer;
        }

        this.radius = analysis.getRadius(analysis.getLowestLayer(hullBlocks));

        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("radius", radius);

        collectInfo(analysis, connectedBlocks, tag);

        writeBlocksToNBT(connectedBlocks, analysis.world);
        analysis.status = BuildStat.SUCCESS;
        return Optional.of(tag);
    }
}
