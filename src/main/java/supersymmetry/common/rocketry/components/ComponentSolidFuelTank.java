package supersymmetry.common.rocketry.components;

import static java.lang.Math.pow;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import gregtech.api.block.VariantBlock;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockTankShell1;

public class ComponentSolidFuelTank extends AbstractComponent<ComponentSolidFuelTank> {

    public int volume;

    public ComponentSolidFuelTank() {
        super("solid_tank", "solid_tank", candidate -> candidate.getSecond().stream().anyMatch(
                pos -> candidate.getFirst().world.getBlockState(pos).getBlock().equals(SuSyBlocks.BLOCK_IGNITER)));
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
    public Optional<ComponentSolidFuelTank> readFromNBT(NBTTagCompound compound) {
        if (compound.getString("type").isEmpty() || compound.getString("name").isEmpty()) {
            return Optional.empty();
        }
        if (!compound.hasKey("mass")) {
            return Optional.empty();
        }
        if (!compound.hasKey("radius")) {
            return Optional.empty();
        }
        if (!compound.hasKey("volume")) {
            return Optional.empty();
        }
        if (!compound.hasKey("materials")) {
            return Optional.empty();
        }

        ComponentSolidFuelTank tank = new ComponentSolidFuelTank();
        compound.getTagList("materials", Constants.NBT.TAG_COMPOUND)
                .forEach(tag -> tank.materials.add(MaterialCost.fromNBT((NBTTagCompound) tag)));

        tank.volume = compound.getInteger("volume");
        tank.radius = compound.getDouble("radius");
        tank.mass = compound.getDouble("mass");
        return Optional.of(tank);
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
        List<BlockPos> detectedBlocks = analysis.getBlocks(analysis.world, aabb, true);
        if (detectedBlocks.isEmpty()) {
            analysis.status = StructAnalysis.BuildStat.ERROR;
            return Optional.empty();
        }

        Set<BlockPos> blocks = analysis.getBlockConn(aabb, detectedBlocks.get(0));
        if (!blocks.equals(new HashSet<>(detectedBlocks))) {
            analysis.status = StructAnalysis.BuildStat.DISCONNECTED;
            return Optional.empty();
        }

        Set<BlockPos> tankBlocks = analysis.getOfBlockType(blocks, SuSyBlocks.TANK_SHELL1).collect(Collectors.toSet());
        Set<BlockPos> nozzleBlocks = analysis.getOfBlockType(blocks, SuSyBlocks.ROCKET_NOZZLE)
                .collect(Collectors.toSet());
        List<BlockPos> igniterBlocks = analysis.getOfBlockType(blocks, SuSyBlocks.BLOCK_IGNITER).toList();
        ArrayList<Integer> tankAreas = new ArrayList<>();
        AxisAlignedBB tankBB = analysis.getBB(tankBlocks);

        if (igniterBlocks.size() != 1) {
            analysis.status = StructAnalysis.BuildStat.IGNITER_WRONG;
            if (igniterBlocks.isEmpty())
                return Optional.empty();
            else {
                return analysis.errorPos(igniterBlocks.getFirst());
            }
        }
        for (int i = (int) tankBB.maxY - 1; i >= (int) tankBB.minY; i--) {
            Set<BlockPos> airLayer = analysis.getLayerAir(tankBB, i);
            if (airLayer == null) { // there should be some air here
                analysis.status = StructAnalysis.BuildStat.HULL_WEAK;
                return Optional.empty();
            }
            double welzlRadius = analysis.getRadius(airLayer);
            if (pow(welzlRadius, 2) * Math.PI - 2 > airLayer.size()) {
                // circular pattern
                analysis.status = StructAnalysis.BuildStat.HULL_WEAK;
                int finalI = i;
                // works because the airLayer is not null and the structure is connected
                return analysis.errorPos(nozzleBlocks.stream().filter(b -> b.getY() == finalI)
                        .toList().getFirst());
            }
            if (i == (int) tankBB.maxY - 1) {
                if (!airLayer.contains(igniterBlocks.getFirst().add(0, -1, 0))) {
                    analysis.status = StructAnalysis.BuildStat.IGNITER_WRONG;
                    return analysis.errorPos(igniterBlocks.getFirst());
                }
                if (airLayer.size() != 1) {
                    analysis.status = StructAnalysis.BuildStat.HULL_WEAK;
                    return analysis.errorPos(new ArrayList<>(airLayer).getFirst());
                }
            }
            tankAreas.add(airLayer.size());
        }

        int interiorAir = tankAreas.stream().reduce(0, Integer::sum);

        if (interiorAir < 2) {
            analysis.status = StructAnalysis.BuildStat.HULL_FULL;
            return Optional.empty();
        }

        Predicate<BlockPos> shellPredicate = block -> {
            IBlockState blockState = analysis.world.getBlockState(block);
            Block candidate = blockState.getBlock();
            if (!candidate.equals(SuSyBlocks.TANK_SHELL1)) {
                return false;
            }
            BlockTankShell1.TankCoverType blockType = ((BlockTankShell1.TankCoverType) (((VariantBlock<?>) blockState
                    .getBlock())
                    .getState(blockState)));
            return blockType.equals(BlockTankShell1.TankCoverType.STEEL_SHELL);
        };
        for (BlockPos block : tankBlocks) {
            if (!shellPredicate.test(block)) {
                analysis.status = StructAnalysis.BuildStat.HULL_WEAK;
                return analysis.errorPos(block);
            }
        }

        ArrayList<Integer> nozzleAreas = new ArrayList<>();
        AxisAlignedBB nozzleBB = analysis.getBB(nozzleBlocks);

        for (int i = (int) nozzleBB.maxY - 1; i >= (int) nozzleBB.minY; i--) {
            Set<BlockPos> airLayer = analysis.getLayerAir(nozzleBB, i);
            if (airLayer == null) { // there should be an error here
                analysis.status = StructAnalysis.BuildStat.NOZZLE_MALFORMED;
                return Optional.empty();
            }
            double welzlRadius = analysis.getRadius(airLayer);
            if (pow(welzlRadius, 2) * Math.PI - 2.5 > airLayer.size()) {
                // circular pattern
                analysis.status = StructAnalysis.BuildStat.NOZZLE_MALFORMED;
                int finalI = i;
                // works because the airLayer is not null and the structure is connected
                return analysis.errorPos(nozzleBlocks.stream().filter(b -> b.getY() == finalI)
                        .toList().getFirst());
            }
            nozzleAreas.add((int) (airLayer.size() + welzlRadius * Math.PI));
        }

        int initial = nozzleAreas.get(0);
        int fin = initial;

        for (int a : nozzleAreas) {
            if (fin <= a) {
                fin = a;
            } else {
                analysis.status = StructAnalysis.BuildStat.NOT_LAVAL;
                return Optional.empty();
            }
        }

        float computedAreaRatio = ((float) fin) / initial;
        if (computedAreaRatio < 1.5) {
            analysis.status = StructAnalysis.BuildStat.NOT_LAVAL;
            return Optional.empty();
        }

        this.radius = analysis.getRadius(nozzleBlocks);
        int calculatedHeight = (int) (analysis.getBB(blocks).maxZ - analysis.getBB(blocks).minZ);
        if (calculatedHeight > radius * 2) {
            analysis.status = StructAnalysis.BuildStat.TOO_SHORT;
        }
        NBTTagCompound tag = new NBTTagCompound();

        // The scan is successful by this point
        analysis.status = StructAnalysis.BuildStat.SUCCESS;
        // The center column of the rocket shouldn't be counted
        this.volume = interiorAir - (int) (analysis.getBB(tankBlocks).maxZ - analysis.getBB(tankBlocks).minZ);
        tag.setInteger("volume", this.volume);
        tag.setDouble("area_ratio", computedAreaRatio);

        collectInfo(analysis, blocks, tag);
        writeBlocksToNBT(blocks, analysis.world);
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
}
