package supersymmetry.api.rocketry.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import org.jetbrains.annotations.Nullable;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.AssemblyStep;
import supersymmetry.api.rocketry.WeightedBlock;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.common.tileentities.TileEntityCoverable;

public abstract class AbstractComponent<T extends AbstractComponent<T>> implements AssemblyStep {

    private static final Map<String, Supplier<AbstractComponent<?>>> REGISTRY = new LinkedHashMap<>();
    private static boolean registryLock = false;

    protected String name;
    protected String type;
    protected double mass;
    protected double radius;
    protected List<MaterialCost> materials = new ArrayList<>();
    protected int height;

    public AbstractComponent(String name, String type,
                             Predicate<Tuple<StructAnalysis, List<BlockPos>>> detectionPredicate) {
        this.detectionPredicate = detectionPredicate;
        this.name = name;
        this.type = type;
    }

    public static void registerComponent(Supplier<AbstractComponent<?>> factory) {
        if (registryLock) {
            throw new IllegalStateException("tried to register a component after the registry was closed. dumbass.");
        }
        AbstractComponent<?> component = factory.get();
        REGISTRY.put(component.getName(), factory);
    }

    public static void lockRegistry() {
        registryLock = true;
    }

    public static Collection<String> getRegisteredNames() {
        return List.copyOf(REGISTRY.keySet());
    }

    public static List<AbstractComponent<?>> getRegistry() {
        return REGISTRY.values().stream().map(Supplier::get).collect(Collectors.toCollection(ArrayList::new));
    }

    @Nullable public static AbstractComponent<?> getComponentFromName(String name) {
        Supplier<AbstractComponent<?>> factory = REGISTRY.get(name);
        if (factory == null) {
            return null;
        }
        try {
            return factory.get();
        } catch (Exception e) {
            SusyLog.logger.error("something horrible happened during component instantiation. {} {}",
                    e.getMessage(), e.getStackTrace());
            return null;
        }
    }

    public void writeBlocksToNBT(Set<BlockPos> blocks, World world, NBTTagCompound tag) {
        Map<ItemStack, Integer> blockCounts = new Object2IntOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        Map<ItemStack, Integer> coverCounts = new Object2IntOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        for (BlockPos pos : blocks) {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            int meta = block.damageDropped(state);

            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityCoverable teCoverable) {
                ItemStack coverStack = teCoverable.getCoverItem();
                if (coverStack.getItem().getRegistryName() != Items.AIR.getRegistryName()) {
                    ItemStack key = new ItemStack(coverStack.getItem(), 1, coverStack.getMetadata());
                    coverCounts.merge(key, teCoverable.getCoverCount(), Integer::sum);
                }
            }

            ItemStack key = new ItemStack(Item.getItemFromBlock(block), 1, meta);
            blockCounts.merge(key, 1, Integer::sum);
        }

        NBTTagList list = new NBTTagList();
        for (Map.Entry<ItemStack, Integer> e : blockCounts.entrySet()) {
            list.appendTag(new MaterialCost(e.getKey(), MaterialCost.SourceType.ITEM, e.getValue()).toNBT());
        }
        for (Map.Entry<ItemStack, Integer> e : coverCounts.entrySet()) {
            list.appendTag(new MaterialCost(e.getKey(), MaterialCost.SourceType.COVER, e.getValue()).toNBT());
        }
        tag.setTag("materials", list);
    }

    public static double getMassOfBlock(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof WeightedBlock weightedBlock) {
            return weightedBlock.getMass(state);
        }
        return 50.0;
    }

    protected Predicate<Tuple<StructAnalysis, List<BlockPos>>> detectionPredicate;

    // when in the aerospace flight simulator ui, this defines if a component can be
    // put into a row
    protected Predicate<String> componentSlotValidator = name -> name.equals(this.getType()) ||
            name.equals(this.getName());

    public Predicate<String> getComponentSlotValidator() {
        return componentSlotValidator;
    }

    public List<MaterialCost> getMaterials() {
        return materials;
    }

    @Override
    public List<GTRecipeInput> getRecipeInputs() {
        return materials.stream().map(MaterialCost::toIngredient).collect(Collectors.toList());
    }

    public double getAssemblyDuration() {
        return 200;
    }

    @Override
    public double getElectrodeDamageFactor() {
        return getAssemblyDuration() + getRadius();
    }

    public static String getLocalizationKey(String type) {
        return String.format("susy.rocketry.components.%s.name", type);
    }

    public double getMass() {
        return this.mass;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public double getRadius() {
        return this.radius;
    }

    public Predicate<Tuple<StructAnalysis, List<BlockPos>>> getDetectionPredicate() {
        return this.detectionPredicate;
    }

    public abstract Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb);

    public void collectInfo(StructAnalysis analysis, Set<BlockPos> connected, NBTTagCompound tag) {
        this.mass = connected.stream().mapToDouble(block -> getMassOfBlock(analysis.world.getBlockState(block))).sum();
        this.height = analysis.getHeight(connected);
        tag.setDouble("mass", mass);
        tag.setInteger("height", height);
        tag.setString("type", type);
        tag.setString("name", name);

        if (!tag.hasKey("radius")) {
            this.radius = analysis.getRadius(connected);
            tag.setDouble("radius", radius);
        } else {
            this.radius = tag.getDouble("radius");
        }
        writeBlocksToNBT(connected, analysis.world, tag);
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("name", this.getName());
        tag.setString("type", this.getType());
        tag.setDouble("mass", this.getMass());
        tag.setInteger("height", this.height);
        tag.setDouble("radius", this.radius);
        NBTTagList list = new NBTTagList();
        for (MaterialCost material : this.getMaterials()) {
            list.appendTag(material.toNBT());
        }
        tag.setTag("materials", list);
    }

    protected boolean readBaseFromNBT(NBTTagCompound compound) {
        if (!this.type.equals(compound.getString("type")) || !this.name.equals(compound.getString("name"))) {
            return false;
        }
        if (!compound.hasKey("mass", NBT.TAG_DOUBLE) || !compound.hasKey("radius", NBT.TAG_DOUBLE) ||
                !compound.hasKey("materials", NBT.TAG_LIST)) {
            return false;
        }
        this.mass = compound.getDouble("mass");
        this.radius = compound.getDouble("radius");
        this.height = compound.getInteger("height");
        compound.getTagList("materials", NBT.TAG_COMPOUND)
                .forEach(tag -> this.materials.add(MaterialCost.fromNBT((NBTTagCompound) tag)));
        return true;
    }

    // used for subitem generation, false => no subitem
    public boolean configureDefaults() {
        return false;
    }

    public List<String> getTooltipLines(NBTTagCompound tag) {
        List<String> lines = new ArrayList<>();
        if (tag.hasKey("mass")) {
            lines.add(I18n.format("susy.rocketry.tooltip.mass", String.format("%.0f", tag.getDouble("mass"))));
        }
        if (tag.hasKey("radius")) {
            lines.add(I18n.format("susy.rocketry.tooltip.radius", String.format("%.1f", tag.getDouble("radius"))));
        }
        return lines;
    }

    public abstract Optional<T> readFromNBT(NBTTagCompound compound);

    public double getHeight() {
        return this.height;
    }
}
