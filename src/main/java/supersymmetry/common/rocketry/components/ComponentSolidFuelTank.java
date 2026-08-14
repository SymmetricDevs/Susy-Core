package supersymmetry.common.rocketry.components;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.Constants;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockCombustionChamber;
import supersymmetry.common.blocks.rocketry.BlockTankShell;

import java.sql.Struct;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;

public class ComponentSolidFuelTank extends AbstractComponent<ComponentSolidFuelTank> {
    public int volume;

    public ComponentSolidFuelTank() {
        super("solid_tank", "tank", candidate -> candidate.getSecond().stream().anyMatch(
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
        if (!blocks.equals(detectedBlocks)) {
            analysis.status = StructAnalysis.BuildStat.DISCONNECTED;
            return Optional.empty();
        }

        Set<BlockPos> hullBlocks = analysis.getOfBlockType(blocks, SuSyBlocks.TANK_SHELL).collect(Collectors.toSet());
        Set<BlockPos> nozzleBlocks = analysis.getOfBlockType(blocks, SuSyBlocks.ROCKET_NOZZLE).collect(Collectors.toSet());
        StructAnalysis.HullData hullData = analysis.checkHull(analysis.getBB(hullBlocks), blocks, false);
        StructAnalysis.HullData nozzleHullData = analysis.checkHull(analysis.getBB(nozzleBlocks), blocks, false);

        if (hullData.interior().isEmpty()) {
            analysis.status = StructAnalysis.BuildStat.HULL_WEAK;
            return Optional.empty();
        }
        if (nozzleHullData.interior().isEmpty()) {
            analysis.status = StructAnalysis.BuildStat.NOZZLE_MALFORMED;
            return Optional.empty();
        }

        Set<BlockPos> interiorAir = hullData.interior();

        if (interiorAir.size() < 2) {
            analysis.status = StructAnalysis.BuildStat.HULL_FULL;
            return Optional.empty();
        }

        Predicate<BlockPos> shellPredicate = block -> {
            IBlockState blockState = analysis.world.getBlockState(block);
            Block candidate = blockState.getBlock();
            if (!candidate.equals(SuSyBlocks.TANK_SHELL)) {
                return false;
            }
            BlockTankShell.TankCoverType blockType = ((BlockTankShell.TankCoverType) (((VariantBlock<?>) blockState.getBlock())
                    .getState(blockState)));
            return blockType.equals(BlockTankShell.TankCoverType.STEEL_SHELL);
        };
        for (BlockPos block : hullBlocks) {
            if (!shellPredicate.test(block)) {
                analysis.status = StructAnalysis.BuildStat.HULL_WEAK;
                return analysis.errorPos(block);
            }
            EnumFacing facingFromBlock = analysis.world.getBlockState(block).getValue(FACING);
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos neighbor = block.add(facing.getDirectionVec());
                if (interiorAir.contains(neighbor)) {
                    Vec3i difference = analysis.diff(neighbor, block);
                    if (!difference.equals(facingFromBlock.getOpposite().getDirectionVec())) {
                        // ideally the shell should be plates but we can't really do that
                        analysis.status = StructAnalysis.BuildStat.HULL_WEAK;
                        return analysis.errorPos(block);
                    }
                }
            }
        }

        this.radius = analysis.getRadius(blocks);
        int calculatedHeight = (int) (analysis.getBB(blocks).maxZ - analysis.getBB(blocks).minZ);
        if (calculatedHeight > radius * 2) {
            analysis.status = StructAnalysis.BuildStat.TOO_SHORT;
        }
        NBTTagCompound tag = new NBTTagCompound();

        // The scan is successful by this point
        analysis.status = StructAnalysis.BuildStat.SUCCESS;
        this.volume = interiorAir.size();
        tag.setInteger("volume", this.volume);

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
