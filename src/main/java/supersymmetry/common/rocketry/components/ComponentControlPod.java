package supersymmetry.common.rocketry.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

import gregtech.api.block.VariantBlock;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.tileentities.TileEntityCoverable;

public class ComponentControlPod extends AbstractComponent<ComponentControlPod> {

    public Map<String, Integer> parts = new HashMap<>();
    public Map<String, Integer> instruments = new HashMap<>();
    public boolean hasAir;
    public double volume;

    public ComponentControlPod() {
        super(
                "spacecraft_control_pod",
                "spacecraft_control_pod",
                ComponentControlPod::detect);
    }

    private static boolean detect(Tuple<StructAnalysis, List<BlockPos>> input) {
        AxisAlignedBB aabb = input.getFirst().getBB(input.getSecond());

        StructAnalysis analysis = input.getFirst();
        List<BlockPos> detectedBlocks = analysis.getBlocks(analysis.world, aabb, true);
        if (detectedBlocks.isEmpty()) {
            return false;
        }

        Set<BlockPos> blocks = analysis.getBlockConn(aabb, detectedBlocks.get(0));
        analysis.checkHull(aabb, blocks, false);
        // for some reason this is the thing that sets BuildStat status to
        // HULL_FULL

        boolean hasAir = analysis.status != BuildStat.HULL_FULL;
        boolean hasTheBlock = input.getSecond().stream()
                .anyMatch(
                        bp -> analysis.world
                                .getBlockState(bp)
                                .getBlock()
                                .equals(SuSyBlocks.ROCKET_CONTROL));

        return hasAir && hasTheBlock;
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
        Set<BlockPos> blocksConnected = analysis.getBlockConn(aabb,
                analysis.getBlocks(analysis.world, aabb, true).get(0));
        StructAnalysis.HullData hullCheck = analysis.checkHull(aabb, blocksConnected, false);
        Set<BlockPos> exterior = hullCheck.exterior();
        Set<BlockPos> interior = hullCheck.interior();
        return spacecraftPattern(blocksConnected, interior, exterior, analysis);
    }

    public Optional<NBTTagCompound> spacecraftPattern(
            Set<BlockPos> blocksConnected,
            Set<BlockPos> interior,
            Set<BlockPos> exterior,
            StructAnalysis analysis) {
        NBTTagCompound tag = new NBTTagCompound();

        Predicate<BlockPos> lifeSupportCheck = bp -> analysis.world.getBlockState(bp).getBlock()
                .equals(SuSyBlocks.LIFE_SUPPORT);
        Set<BlockPos> lifeSupports = blocksConnected.stream().filter(lifeSupportCheck).collect(Collectors.toSet());


        lifeSupports.forEach(
                bp -> {
                    Block block = analysis.world.getBlockState(bp).getBlock();
                    NBTTagCompound list = tag.getCompoundTag(AbstractComponent.PARTS_KEY);
                    String part = ((VariantBlock<?>) block).getState(analysis.world.getBlockState(bp)).toString();
                    int num = list.getInteger(part); // default behavior is 0
                    list.setInteger(part, num + 1);
                    this.parts.put(part, num + 1);
                    tag.setTag(AbstractComponent.PARTS_KEY, list);
                });

        for (BlockPos bp : exterior) {
            if (analysis.world.getBlockState(bp).getBlock().equals(SuSyBlocks.SPACECRAFT_HULL)) {
                TileEntityCoverable te = (TileEntityCoverable) analysis.world.getTileEntity(bp);
                for (EnumFacing side : EnumFacing.VALUES) {
                    // either it must be facing the outside without a cover or it must have
                    if (te.isCovered(side) ^ exterior.contains(bp.add(side.getDirectionVec()))) {
                        analysis.status = BuildStat.HULL_WEAK;
                        return Optional.empty();
                    }
                }
            } else if (analysis.world.getBlockState(bp).getBlock().equals(SuSyBlocks.SPACE_INSTRUMENT)) {
                Block block = analysis.world.getBlockState(bp).getBlock();
                NBTTagCompound list = tag.getCompoundTag(AbstractComponent.INSTRUMENTS_KEY);
                String part = ((VariantBlock<?>) block).getState(analysis.world.getBlockState(bp)).toString();
                int num = list.getInteger(part); // default behavior is 0
                list.setInteger(part, num + 1);
                this.instruments.put(part, num + 1);
                tag.setTag(AbstractComponent.PARTS_KEY, list);
            } else {
                analysis.status = BuildStat.HULL_WEAK;
            }
        }

        if (lifeSupports.isEmpty()) {
            // no airspace necessary
            if (!interior.isEmpty()) {
                analysis.status = BuildStat.SPACECRAFT_HOLLOW;
                return Optional.empty();
            }
            tag.setBoolean("hasAir", false);
            this.hasAir = false; // goog..?
        } else {
            int volume = interior.size();
            tag.setInteger("volume", volume);
            Set<BlockPos> container = analysis.getPerimeter(interior, StructAnalysis.orthVecs);
            for (BlockPos bp : container) {
                Block block = analysis.world.getBlockState(bp).getBlock();
                if (block.equals(SuSyBlocks.LIFE_SUPPORT)) {
                    continue; // we did the math on this earlier
                } else if (analysis.world.getTileEntity(bp) != null) {
                    if (analysis.world.getTileEntity(bp) instanceof TileEntityCoverable) {
                        TileEntityCoverable te = (TileEntityCoverable) analysis.world.getTileEntity(bp);
                        if (block.equals(SuSyBlocks.ROOM_PADDING)) {
                            for (EnumFacing side : EnumFacing.VALUES) {
                                if (te.isCovered(side) ^ interior.contains(bp.add(side.getDirectionVec()))) {
                                    analysis.status = BuildStat.WEIRD_PADDING;
                                    return Optional.empty();
                                }
                            }
                        }
                    }
                }
            }
            tag.setBoolean("hasAir", true);
            this.hasAir = true;
        }
        double radius = analysis.getRadius(blocksConnected);

        // The scan is successful by this point
        analysis.status = BuildStat.SUCCESS;
        collectInfo(analysis, blocksConnected, tag);

        writeBlocksToNBT(blocksConnected, analysis.world);
        return Optional.of(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("radius", this.radius);
        tag.setDouble("volume", this.volume);
        tag.setBoolean("hasAir", this.hasAir);
        NBTTagCompound instrumentsTag = new NBTTagCompound();
        NBTTagCompound partsTag = new NBTTagCompound();
        for (var part : this.parts.entrySet()) {
            partsTag.setInteger(part.getKey(), part.getValue());
        }
        for (var instrument : this.instruments.entrySet()) {
            instrumentsTag.setInteger(instrument.getKey(), instrument.getValue());
        }
        tag.setTag(AbstractComponent.INSTRUMENTS_KEY, instrumentsTag);
        tag.setTag("tools", partsTag);
    }

    @Override
    public Optional<ComponentControlPod> readFromNBT(NBTTagCompound compound) {
        ComponentControlPod controlPod = new ComponentControlPod();

        if (compound.getString("name").isEmpty()) return Optional.empty();
        if (compound.getString("type").isEmpty()) return Optional.empty();
        if (!compound.hasKey("radius", NBT.TAG_DOUBLE)) return Optional.empty();
        if (!compound.hasKey("mass", NBT.TAG_DOUBLE)) return Optional.empty();
        if (!compound.hasKey("hasAir")) return Optional.empty();
        if (!compound.hasKey("volume", NBT.TAG_DOUBLE)) return Optional.empty();
        if (!compound.hasKey(AbstractComponent.PARTS_KEY, NBT.TAG_COMPOUND)) return Optional.empty();
        if (!compound.hasKey(AbstractComponent.INSTRUMENTS_KEY, NBT.TAG_COMPOUND)) return Optional.empty();

        if (!compound.hasKey("materials", NBT.TAG_LIST)) return Optional.empty();
        compound
                .getTagList("materials", NBT.TAG_COMPOUND)
                .forEach(tag -> controlPod.materials.add(MaterialCost.fromNBT((NBTTagCompound) tag)));
        controlPod.radius = compound.getDouble("radius");
        controlPod.mass = compound.getDouble("mass");
        controlPod.volume = compound.getDouble("volume");
        controlPod.hasAir = compound.getBoolean("hasAir");

        NBTTagCompound instrumentsList = compound.getCompoundTag(AbstractComponent.INSTRUMENTS_KEY);
        for (String key : instrumentsList.getKeySet()) {
            controlPod.instruments.put(key, instrumentsList.getInteger(key));
        }

        NBTTagCompound partsList = compound.getCompoundTag(AbstractComponent.PARTS_KEY);
        for (String key : partsList.getKeySet()) {
            controlPod.parts.put(key, partsList.getInteger(key));
        }

        return Optional.of(controlPod);
    }
}
