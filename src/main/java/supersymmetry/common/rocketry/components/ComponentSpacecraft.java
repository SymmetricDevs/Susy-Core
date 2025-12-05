package supersymmetry.common.rocketry.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
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
import supersymmetry.common.tile.TileEntityCoverable;

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
        Set<BlockPos> lifeSupports = blocksConnected.stream().filter(lifeSupportCheck).collect(Collectors.toSet());
        NBTTagCompound tag = new NBTTagCompound();

        lifeSupports.forEach(
                bp -> {
                    Block block = analysis.world.getBlockState(bp).getBlock();
                    NBTTagCompound partsTag = tag.getCompoundTag(PARTS_KEY);
                    String part = ((VariantBlock<?>) block).getState(analysis.world.getBlockState(bp)).toString();
                    int count = partsTag.getInteger(part); // default behavior is 0
                    partsTag.setInteger(part, count + 1);
                    this.parts.put(part, count + 1);
                    tag.setTag(PARTS_KEY, partsTag);
                });

        for (BlockPos bp : exterior) {
            if (analysis.world.getBlockState(bp).getBlock().equals(SuSyBlocks.SPACECRAFT_HULL)) {
                TileEntityCoverable te = (TileEntityCoverable) analysis.world.getTileEntity(bp);
                for (EnumFacing side : EnumFacing.VALUES) {
                    // If it is both covered but facing another hull block
                    // or not covered but facing air, then fail.
                    if (te.isCovered(side) == exterior.contains(bp.add(side.getDirectionVec()))) {
                        analysis.status = BuildStat.HULL_WEAK;
                        return Optional.empty();
                    }
                }
            } else if (analysis.world.getBlockState(bp).getBlock().equals(SuSyBlocks.SPACE_INSTRUMENT)) {
                {
                    Block block = analysis.world.getBlockState(bp).getBlock();
                    NBTTagCompound instrumentsTag = tag.getCompoundTag(INSTRUMENTS_KEY);
                    String part = ((VariantBlock<?>) block).getState(analysis.world.getBlockState(bp)).toString();
                    int count = instrumentsTag.getInteger(part); // default behavior is 0
                    instrumentsTag.setInteger(part, count + 1);
                    this.instruments.put(part, count + 1);
                    tag.setTag(INSTRUMENTS_KEY, instrumentsTag);
                }
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
                            return Optional.empty();
                        }
                    }
                }
            }
            tag.setBoolean("hasAir", true);
            this.hasAir = true;
        }
        double radius = analysis.getApproximateRadius(blocksConnected);

        // The scan is successful by this point
        analysis.status = BuildStat.SUCCESS;
        tag.setString("type", type);
        tag.setString("name", name);
        tag.setDouble("radius", (radius));
        this.radius = radius;
        double mass = blocksConnected.stream()
                .mapToDouble(block -> getMassOfBlock(analysis.world.getBlockState(block)))
                .sum();
        tag.setDouble("mass", mass);
        this.mass = mass;
        writeBlocksToNBT(blocksConnected, analysis.world);
        return Optional.of(tag);
    }
}
