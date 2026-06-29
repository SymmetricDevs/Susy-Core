package supersymmetry.api.rocketry.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

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

import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.WeightedBlock;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.tileentities.TileEntityCoverable;

public abstract class AbstractComponent<T extends AbstractComponent<T>> {

    protected static final String PARTS_KEY = "parts";
    protected static final String INSTRUMENTS_KEY = "instruments";
    private static final Set<AbstractComponent<?>> registry = new HashSet<>();
    private static boolean registryLock = false;
    private static final Map<String, Class<? extends AbstractComponent<?>>> nameToComponentRegistry = new HashMap<>();

    protected String name;
    protected String type;
    protected BuildStat status = BuildStat.ERROR;
    protected double mass;
    protected double radius;
    public List<MaterialCost> materials = new ArrayList<>();
    protected int height;

    public AbstractComponent(
                             String name,
                             String type,
                             Predicate<Tuple<StructAnalysis, List<BlockPos>>> detectionPredicate) {
        this.detectionPredicate = detectionPredicate;
        this.name = name;
        this.type = type;
    }

    public static Map<String, Class<? extends AbstractComponent<?>>> getNameRegistry() {
        return new HashMap<>(nameToComponentRegistry);
    }

    public static boolean nameRegistered(String name) {
        return nameToComponentRegistry.containsKey(name) && registry.stream().anyMatch(x -> x.getName().equals(name));
    }

    public static AbstractComponent<?> getComponentFromName(String name) {
        if (nameToComponentRegistry.containsKey(name)) {
            try {
                return nameToComponentRegistry.get(name).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                SusyLog.logger.error(
                        "something horrible happened during component instantiation. {} {}",
                        e.getMessage(),
                        e.getStackTrace());
            }
        } else {
            throw new IllegalStateException("tried to get a non existing component");
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static void registerComponent(AbstractComponent<?> component) {
        if (!getRegistryLock()) {
            nameToComponentRegistry.put(
                    component.getName(), (Class<? extends AbstractComponent<?>>) component.getClass());
            if (registry.stream().noneMatch(x -> x.getName().equals(component.getName()))) {
                registry.add(component);
            }
        } else {
            throw new IllegalStateException(
                    "tried to register a component after the registry was closed. dumbass.");
        }
    }

    public static void lockRegistry() {
        registryLock = true;
    }

    public static boolean getRegistryLock() {
        return registryLock;
    }

    public static Set<AbstractComponent<?>> getRegistry() {
        return new HashSet<>(registry);
    }

    public void writeBlocksToNBT(Set<BlockPos> blocks, World world) {
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

        for (Map.Entry<ItemStack, Integer> e : blockCounts.entrySet()) {
            materials.add(new MaterialCost(e.getKey(), MaterialCost.SourceType.ITEM, e.getValue()));
        }
        for (Map.Entry<ItemStack, Integer> e : coverCounts.entrySet()) {
            materials.add(new MaterialCost(e.getKey(), MaterialCost.SourceType.COVER, e.getValue()));
        }
    }

    public static double getMassOfBlock(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof WeightedBlock weightedBlock) {
            return weightedBlock.getMass(state);
        }
        return 50.0;
    }

    protected Predicate<Tuple<StructAnalysis, List<BlockPos>>> detectionPredicate;

    // meant to verify the compatability between the component and the entire rocket stage so that you
    // dont end up with a liquid fuel engine on a solid fuel tank, return true if everything is fine
    protected Predicate<Map<String, AbstractComponent<?>>> compatabilityValidationPredicate = t -> {
        return true;
    };
    // when in the aerospace flight simulator ui, this defines if a component can be put into a row
    protected Predicate<String> componentSlotValidator = name -> name.equals(this.getType()) ||
            name.equals(this.getName());

    public List<MaterialCost> getMaterials() {
        return materials;
    }

    public double getAssemblyDuration() {
        return 10f;
    }

    public void setMaterials(List<MaterialCost> materials) {
        this.materials = materials;
    }

    public String getLocalizationKey() {
        return "susy.rocketry.components.name." + this.getName();
    }

    public Predicate<String> getComponentSlotValidator() {
        return componentSlotValidator;
    }

    public void setComponentSlotValidator(Predicate<String> componentSlotValidator) {
        this.componentSlotValidator = componentSlotValidator;
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

    public Predicate<Map<String, AbstractComponent<?>>> getCompatabilityValidationPredicate() {
        return compatabilityValidationPredicate;
    }

    public void setCompatabilityValidationPredicate(
                                                    Predicate<Map<String, AbstractComponent<?>>> compatabilityValidationPredicate) {
        this.compatabilityValidationPredicate = compatabilityValidationPredicate;
    }

    public Predicate<Tuple<StructAnalysis, List<BlockPos>>> getDetectionPredicate() {
        return this.detectionPredicate;
    }

    public void setDetectionPredicate(Predicate<Tuple<StructAnalysis, List<BlockPos>>> predicate) {
        this.detectionPredicate = predicate;
    }

    public abstract Optional<NBTTagCompound> analyzePattern(
                                                            StructAnalysis analysis, AxisAlignedBB aabb);

    public void collectInfo(StructAnalysis analysis, Set<BlockPos> connected, NBTTagCompound tag) {
        // These are sometimes done separately.
        if (!tag.hasKey("radius")) {
            this.radius = analysis.getRadius(connected);
            tag.setDouble("radius", radius);
        }
        if (!tag.hasKey("height")) {
            this.height = analysis.getHeight(connected);
            tag.setInteger("height", height);
        }
        this.mass = connected.stream()
                .mapToDouble(block -> getMassOfBlock(analysis.world.getBlockState(block)))
                .sum();
        tag.setDouble("mass", mass);
        tag.setString("type", type);
        tag.setString("name", name);
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("name", this.getName());
        tag.setString("type", this.getType());
        tag.setDouble("mass", this.getMass());
        NBTTagList list = new NBTTagList();
        for (MaterialCost material : this.getMaterials()) {
            list.appendTag(material.toNBT());
        }
        tag.setTag("materials", list);
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
