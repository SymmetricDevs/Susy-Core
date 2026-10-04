package supersymmetry.common.rocketry.components;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.tileentities.TileEntityCoverable;

/**
 * componentLiquidFuelTank
 */
public class ComponentLiquidFuelTank extends AbstractComponent<ComponentLiquidFuelTank> implements IComponentTank {

    public int volume;

    public ComponentLiquidFuelTank() {
        super("fluid_tank", "tank", candidate -> candidate.getSecond().stream().anyMatch(
                pos -> {Block b = candidate.getFirst().world.getBlockState(pos).getBlock();
                    return b.equals(SuSyBlocks.TANK_SHELL) || b.equals(SuSyBlocks.TANK_SHELL1);}));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("volume", this.volume);
    }

    // but the Lord laughs at the wicked,
    // for he knows their day is coming.
    @Override
    public Optional<ComponentLiquidFuelTank> readFromNBT(NBTTagCompound compound) {
        if (!compound.hasKey("volume", Constants.NBT.TAG_INT)) {
            return Optional.empty();
        }
        var tank = new ComponentLiquidFuelTank();
        if (!tank.readBaseFromNBT(compound)) {
            return Optional.empty();
        }
        tank.volume = compound.getInteger("volume");
        return Optional.of(tank);
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
        List<BlockPos> detectedBlocks = analysis.getBlocks(analysis.world, aabb, true);
        if (detectedBlocks.isEmpty()) {
            analysis.status = BuildStat.ERROR;
            return Optional.empty();
        }

        Set<BlockPos> blocks = analysis.getBlockConn(aabb, detectedBlocks.get(0));
        StructAnalysis.HullData hullData = analysis.checkHull(aabb, blocks, false);

        Set<BlockPos> hullBlocks = hullData.exterior();
        Set<BlockPos> interiorAir = hullData.interior();

        if (interiorAir.size() < 2) {
            analysis.status = BuildStat.HULL_FULL;
            return Optional.empty();
        }

        Predicate<BlockPos> shellPredicate = block -> {
            Block candidate = analysis.world.getBlockState(block).getBlock();
            return candidate.equals(SuSyBlocks.TANK_SHELL) || candidate.equals(SuSyBlocks.TANK_SHELL1);
        };
        for (BlockPos block : hullBlocks) {
            if (!shellPredicate.test(block)) {
                analysis.status = BuildStat.HULL_WEAK;
                return analysis.errorPos(block);
            }
            TileEntityCoverable blockTiles = (TileEntityCoverable) analysis.world.getTileEntity(block);
            if (blockTiles == null) {
                analysis.status = BuildStat.ERROR;
                return analysis.errorPos(block);
            }
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos neighbor = block.add(facing.getDirectionVec());
                if (!interiorAir.contains(neighbor) &&
                        (analysis.world.isAirBlock(neighbor) ||
                                !StructAnalysis.blockCont(aabb, neighbor))) { // this means it should be exterior air
                    if (!blockTiles.isCovered(facing)) {
                        analysis.status = BuildStat.MISSING_TILE;
                        return analysis.errorPos(block);
                    }
                } else
                    if (hullBlocks.contains(neighbor) && blockTiles.isCovered(facing)) {
                        analysis.status = BuildStat.WRONG_TILE;
                        return analysis.errorPos(block);
                    }
            }
        }

        this.radius = analysis.getRadius(blocks);
        int calculatedHeight = (int) (analysis.getBB(blocks).maxZ - analysis.getBB(blocks).minZ);
        if (calculatedHeight > radius * 2) {
            analysis.status = BuildStat.TOO_SHORT;
        }
        NBTTagCompound tag = new NBTTagCompound();

        // The scan is successful by this point
        analysis.status = BuildStat.SUCCESS;
        this.volume = interiorAir.size();
        tag.setInteger("volume", this.volume);

        collectInfo(analysis, blocks, tag);
        return Optional.of(tag);
    }

    @Override
    public boolean configureDefaults() {
        this.materials.add(new MaterialCost(new ItemStack(Items.DIAMOND), MaterialCost.SourceType.ITEM, 1));
        this.radius = 5.0;
        this.volume = 80;
        this.mass = 3000.0;
        return true;
    }

    @Override
    public List<String> getTooltipLines(NBTTagCompound tag) {
        List<String> lines = super.getTooltipLines(tag);
        if (tag.hasKey("volume")) {
            lines.add(I18n.format("susy.rocketry.tooltip.volume", tag.getInteger("volume")));
        }
        return lines;
    }

    @Override
    public int getVolume() {
        return this.volume;
    }
}
