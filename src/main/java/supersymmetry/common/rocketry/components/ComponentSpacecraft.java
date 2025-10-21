package supersymmetry.common.rocketry.components;

import java.util.HashMap;
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
import supersymmetry.common.tile.TileEntityCoverable;

public class ComponentSpacecraft extends AbstractComponent<ComponentSpacecraft> {

    public double radius;
    public Map<String, Integer> parts = new HashMap<>();
    public Map<String, Integer> instruments = new HashMap<>();
    public boolean hasAir;
    public double volume;

    public ComponentSpacecraft() {
        super(
                "spacecraft_hull",
                "spacecraft_hull",
                (tupl) -> tupl.getSecond().stream()
                        .anyMatch(
                                x -> tupl.getFirst().world
                                        .getBlockState(x)
                                        .getBlock()
                                        .equals(SuSyBlocks.SPACECRAFT_HULL)));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("radius", this.radius);
        tag.setDouble("volume", this.volume);
        tag.setBoolean("hasAir", this.hasAir);
        NBTTagCompound instruments = new NBTTagCompound();
        NBTTagCompound parts = new NBTTagCompound();
        for (var part : this.parts.entrySet()) {
            parts.setInteger(part.getKey(), part.getValue());
        }
        for (var instrument : this.instruments.entrySet()) {
            instruments.setInteger(instrument.getKey(), instrument.getValue());
        }
        tag.setTag("instruments", instruments);
        tag.setTag("tools", parts);
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
        if (!compound.hasKey("parts", NBT.TAG_COMPOUND)) return Optional.empty();
        if (!compound.hasKey("instruments", NBT.TAG_COMPOUND)) return Optional.empty();
        if (!compound.hasKey("materials", NBT.TAG_LIST)) return Optional.empty();
        compound
                .getTagList("materials", NBT.TAG_COMPOUND)
                .forEach(x -> spacecraft.materials.add(MaterialCost.fromNBT((NBTTagCompound) x)));

        spacecraft.radius = compound.getDouble("radius");
        spacecraft.mass = compound.getDouble("mass");
        spacecraft.volume = compound.getDouble("volume");
        spacecraft.hasAir = compound.getBoolean("hasAir");

        NBTTagCompound instrumentsList = compound.getCompoundTag("instruments");
        for (String key : instrumentsList.getKeySet()) {
            spacecraft.instruments.put(key, compound.getInteger(key));
        }

        NBTTagCompound partsList = compound.getCompoundTag("parts");
        for (String key : partsList.getKeySet()) {
            spacecraft.parts.put(key, compound.getInteger(key));
        }

        return Optional.of(spacecraft);
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
        Set<BlockPos> blocksConnected = analysis.getBlockConn(aabb,
                analysis.getBlocks(analysis.world, aabb, true).get(0));
        Tuple<Set<BlockPos>, Set<BlockPos>> hullCheck = analysis.checkHull(aabb, blocksConnected, false);
        Set<BlockPos> exterior = hullCheck.getFirst();
        Set<BlockPos> interior = hullCheck.getSecond();
        return spacecraftPattern(
                blocksConnected,
                exterior, /* <- these 2 goobers are changed in this class -> */
                interior,
                analysis);
    }

    // copied from componentControlPod because i didnt figure out how to put it into a single function
    // without it complaining
    public Optional<NBTTagCompound> spacecraftPattern(
                                                      Set<BlockPos> blocksConnected,
                                                      Set<BlockPos> interior,
                                                      Set<BlockPos> exterior,
                                                      StructAnalysis analysis) {
        Predicate<BlockPos> lifeSupportCheck = bp -> analysis.world.getBlockState(bp).getBlock()
                .equals(SuSyBlocks.LIFE_SUPPORT);
        Set<BlockPos> lifeSupports = blocksConnected.stream().filter(lifeSupportCheck).collect(Collectors.toSet());
        NBTTagCompound tag = new NBTTagCompound();

        lifeSupports.forEach(
                bp -> {
                    Block block = analysis.world.getBlockState(bp).getBlock();
                    NBTTagCompound list = tag.getCompoundTag("life_supports");
                    String part = ((VariantBlock<?>) block).getState(analysis.world.getBlockState(bp)).toString();
                    int num = list.getInteger(part); // default behavior is 0
                    list.setInteger(part, num + 1);
                    this.parts.put(part, num + 1);
                    tag.setTag("life_supports", list);
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
                {
                    Block block = analysis.world.getBlockState(bp).getBlock();
                    NBTTagCompound list = tag.getCompoundTag("instruments");
                    String part = ((VariantBlock<?>) block).getState(analysis.world.getBlockState(bp)).toString();
                    int num = list.getInteger(part); // default behavior is 0
                    list.setInteger(part, num + 1);
                    this.instruments.put(part, num + 1);
                    tag.setTag("instruments", list);
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
                        if (te.isCovered(side) ^ interior.contains(bp.add(side.getDirectionVec()))) {
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
        double mass = 0;
        for (BlockPos block : blocksConnected) {
            mass += getMass(analysis.world.getBlockState(block));
        }
        tag.setDouble("mass", mass);
        this.mass = mass;
        writeBlocksToNBT(blocksConnected, analysis.world);
        return Optional.of(tag);
    }
}
