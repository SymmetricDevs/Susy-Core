package supersymmetry.common.rocketry.components;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

import gregtech.api.block.VariantBlock;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockGuidanceSystem;
import supersymmetry.common.tileentities.TileEntityCoverable;

public class ComponentSpacecraft extends AbstractComponent<ComponentSpacecraft> {

    public Map<String, Integer> parts = new HashMap<>();
    public Map<String, Integer> instruments = new HashMap<>();
    public boolean hasAir;
    public double volume;

    public ComponentSpacecraft() {
        super(
                "spacecraft_hull",
                "spacecraft_hull",
                tuple -> tuple.getSecond().stream()
                        .anyMatch(
                                pos -> tuple
                                        .getFirst().world
                                                .getBlockState(pos)
                                                .getBlock()
                                                .equals(SuSyBlocks.SPACECRAFT_HULL)));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("radius", this.radius);
        tag.setDouble("volume", this.volume);
        tag.setBoolean("hasAir", this.hasAir);
        NBTTagCompound instrumentsTag = new NBTTagCompound();
        NBTTagCompound partsTag = new NBTTagCompound();
        for (Entry<String, Integer> part : this.parts.entrySet()) {
            partsTag.setInteger(part.getKey(), part.getValue());
        }
        for (Entry<String, Integer> instrument : this.instruments.entrySet()) {
            instrumentsTag.setInteger(instrument.getKey(), instrument.getValue());
        }
        tag.setTag(INSTRUMENTS_KEY, instrumentsTag);
        tag.setTag(PARTS_KEY, partsTag);
    }

    @Override
    public Optional<ComponentSpacecraft> readFromNBT(NBTTagCompound compound) {
        ComponentSpacecraft spacecraft = new ComponentSpacecraft();

        if (!compound.getString("name").equals(spacecraft.name)) return Optional.empty();
        if (!compound.getString("type").equals(spacecraft.type)) return Optional.empty();
        if (!compound.hasKey("radius", NBT.TAG_DOUBLE)) return Optional.empty();
        if (!compound.hasKey("mass", NBT.TAG_DOUBLE)) return Optional.empty();
        if (!compound.hasKey("hasAir")) return Optional.empty();
        if (!compound.hasKey("volume", NBT.TAG_DOUBLE)) return Optional.empty();
        if (!compound.hasKey(AbstractComponent.PARTS_KEY, NBT.TAG_COMPOUND)) return Optional.empty();
        if (!compound.hasKey(AbstractComponent.INSTRUMENTS_KEY, NBT.TAG_COMPOUND)) return Optional.empty();
        if (!compound.hasKey("materials", NBT.TAG_LIST)) return Optional.empty();
        compound
                .getTagList("materials", NBT.TAG_COMPOUND)
                .forEach(x -> spacecraft.materials.add(MaterialCost.fromNBT((NBTTagCompound) x)));

        spacecraft.radius = compound.getDouble("radius");
        spacecraft.mass = compound.getDouble("mass");
        spacecraft.volume = compound.getDouble("volume");
        spacecraft.hasAir = compound.getBoolean("hasAir");

        NBTTagCompound instrumentsList = compound.getCompoundTag(AbstractComponent.INSTRUMENTS_KEY);
        for (String key : instrumentsList.getKeySet()) {
            spacecraft.instruments.put(key, compound.getInteger(key));
        }

        NBTTagCompound partsList = compound.getCompoundTag(AbstractComponent.PARTS_KEY);
        for (String key : partsList.getKeySet()) {
            spacecraft.parts.put(key, partsList.getInteger(key));
        }

        return Optional.of(spacecraft);
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
        Set<BlockPos> blocksConnected = analysis.getBlockConn(aabb,
                analysis.getBlocks(analysis.world, aabb, true).get(0));
        StructAnalysis.HullData hullCheck = analysis.checkHull(aabb, blocksConnected, false);
        Set<BlockPos> exterior = hullCheck.exterior();
        Set<BlockPos> interior = hullCheck.interior();
        return spacecraftPattern(
                blocksConnected,
                exterior,
                interior,
                analysis);
    }

    public Optional<NBTTagCompound> spacecraftPattern(
                                                      Set<BlockPos> blocksConnected,
                                                      Set<BlockPos> exterior,
                                                      Set<BlockPos> interior,
                                                      StructAnalysis analysis) {
        Predicate<BlockPos> lifeSupportCheck = bp -> analysis.world.getBlockState(bp).getBlock()
                .equals(SuSyBlocks.LIFE_SUPPORT);
        Predicate<BlockPos> guidanceComputerCheck = bp -> analysis.world.getBlockState(bp).getBlock()
                .equals(SuSyBlocks.GUIDANCE_SYSTEM);

        Set<BlockPos> lifeSupports = blocksConnected.stream().filter(lifeSupportCheck).collect(Collectors.toSet());
        List<BlockPos> guidanceComputers = blocksConnected.stream().filter(guidanceComputerCheck).collect(Collectors.toList());
        NBTTagCompound tag = new NBTTagCompound();

        lifeSupports.forEach(
                bp -> includePart(analysis, bp, tag, PARTS_KEY, this.parts));

        for (BlockPos bp : exterior) {
            if (analysis.world.getBlockState(bp).getBlock().equals(SuSyBlocks.SPACECRAFT_HULL)) {
                TileEntityCoverable te = (TileEntityCoverable) analysis.world.getTileEntity(bp);
                for (EnumFacing side : EnumFacing.VALUES) {
                    // If it is both covered but facing another hull block
                    // or not covered but facing air, then fail.
                    if (!te.isCovered(side) &&
                            !exterior.contains(bp.add(side.getDirectionVec())) &&
                            !interior.contains(bp.add(side.getDirectionVec()))) {
                        analysis.status = BuildStat.HULL_WEAK;
                        return analysis.errorPos(bp);
                    }
                }
            } else if (analysis.world.getBlockState(bp).getBlock().equals(SuSyBlocks.SPACE_INSTRUMENT)) {
                includePart(analysis, bp, tag, INSTRUMENTS_KEY, this.instruments);
            } else {
                analysis.status = BuildStat.HULL_WEAK;
            }
        }

        if (guidanceComputers.isEmpty()) {
            analysis.status = BuildStat.NO_GUIDANCE;
            return Optional.empty();
        } else if (guidanceComputers.size() > 1) {
            analysis.status = BuildStat.TOO_MUCH_GUIDANCE;
            return Optional.empty();
        }
        IBlockState guidanceBlock = analysis.world.getBlockState(guidanceComputers.get(0));
        tag.setString("guidance", SuSyBlocks.GUIDANCE_SYSTEM.getState(guidanceBlock).toString());

        if (lifeSupports.isEmpty()) {
            // no airspace necessary
            if (!interior.isEmpty()) {
                analysis.status = BuildStat.SPACECRAFT_HOLLOW;
                return Optional.empty();
            }
            tag.setBoolean("hasAir", false);
            this.hasAir = false; // goog..?
        } else {
            if (interior.size() < 2) {
                analysis.status = BuildStat.HULL_FULL;
                return Optional.empty();
            }
            int volume = interior.size();
            tag.setInteger("volume", volume);
            Set<BlockPos> container = analysis.getPerimeter(interior, StructAnalysis.orthVecs);
            for (BlockPos bp : container) {
                Block block = analysis.world.getBlockState(bp).getBlock();
                if (block.equals(SuSyBlocks.LIFE_SUPPORT)) {
                    continue;
                }
                if (analysis.world.getTileEntity(bp) == null ||
                        !(analysis.world.getTileEntity(bp) instanceof TileEntityCoverable)) {
                    continue;
                }
                TileEntityCoverable te = (TileEntityCoverable) analysis.world.getTileEntity(bp);
                if (block.equals(SuSyBlocks.ROOM_PADDING)) {
                    for (EnumFacing side : EnumFacing.VALUES) {
                        if (te.isCovered(side) == interior.contains(bp.add(side.getDirectionVec()))) {
                            analysis.status = BuildStat.WEIRD_PADDING;
                            return analysis.errorPos(bp);
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
        tag.setString("type", type);
        tag.setString("name", name);
        tag.setDouble("radius", radius);
        this.radius = radius;
        double mass = blocksConnected.stream()
                .mapToDouble(block -> getMassOfBlock(analysis.world.getBlockState(block)))
                .sum();
        tag.setDouble("mass", mass);
        this.mass = mass;
        writeBlocksToNBT(blocksConnected, analysis.world);
        return Optional.of(tag);
    }

    private void includePart(StructAnalysis analysis, BlockPos bp, NBTTagCompound tag, String key,
                             Map<String, Integer> instruments) {
        Block block = analysis.world.getBlockState(bp).getBlock();
        NBTTagCompound subTag = tag.getCompoundTag(key);
        String part = ((VariantBlock<?>) block).getState(analysis.world.getBlockState(bp)).toString();
        int count = subTag.getInteger(part); // default behavior is 0
        subTag.setInteger(part, count + 1);
        instruments.put(part, count + 1);
        tag.setTag(key, subTag);
    }
}
