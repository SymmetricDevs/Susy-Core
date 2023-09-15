package supersymmetry.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.api.unification.ore.SusyOrePrefix;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FRAME;

public class SuSyMetaBlocks {
    public static final Map<Material, BlockSheetedFrame> SHEETED_FRAMES = new HashMap<>();
    public static final List<BlockSheetedFrame> SHEETED_FRAME_BLOCKS = new ArrayList<>();
    public SuSyMetaBlocks() {}

    public static void init() {
        createGeneratedBlock(m -> m.hasProperty(PropertyKey.DUST) && m.hasFlag(GENERATE_FRAME), SuSyMetaBlocks::createSheetedFrameBlock);
    }

    public static void createSheetedFrameBlock(Material[] materials, int index) {
        BlockSheetedFrame block = new BlockSheetedFrame(materials);
        block.setRegistryName("meta_block_sheeted_frame_" + index);

        for (Material material : materials) {
            SHEETED_FRAMES.put(material, block);
        }

        SHEETED_FRAME_BLOCKS.add(block);
    }

    protected static void createGeneratedBlock(Predicate<Material> materialPredicate, BiConsumer<Material[], Integer> blockGenerator) {
        Map<Integer, Material[]> blocksToGenerate = new TreeMap<>();

        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            if (materialPredicate.test(material)) {
                int id = material.getId();
                //all bits more significant than last four = metaBlockID = key in blocksToGenerate map
                //least significant four bits = subID (index in material[] element)
                int metaBlockID = id / 4; // -> >>> 2
                int subBlockID = id % 4;  // -> & 3
                if (!blocksToGenerate.containsKey(metaBlockID)) {
                    Material[] materials = new Material[4];
                    Arrays.fill(materials, Materials.NULL);
                    blocksToGenerate.put(metaBlockID, materials);
                }

                (blocksToGenerate.get(metaBlockID))[subBlockID] = material;
            }
        }

        blocksToGenerate.forEach((key, value) -> blockGenerator.accept(value, key));
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        //registers blockstates with associated models properly by calling sheeted frame's model register recipes
        SHEETED_FRAMES.values().stream().distinct().forEach(BlockSheetedFrame::onModelRegister);

    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(Block block) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(state.getProperties())));
        }
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModelWithOverride(Block block, Map<IProperty<?>, Comparable<?>> stateOverrides, Predicate<IBlockState> condition) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            if (!condition.test(state)) continue;
            HashMap<IProperty<?>, Comparable<?>> stringProperties = new HashMap<>(state.getProperties());
            stringProperties.putAll(stateOverrides);
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(stringProperties)));
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerColors() {
        BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
        ItemColors itemColors = Minecraft.getMinecraft().getItemColors();

        for (BlockSheetedFrame block : SHEETED_FRAME_BLOCKS) {
            blockColors.registerBlockColorHandler((s, w, p, i) ->
                    block.getGtMaterial(block.getMetaFromState(s)).getMaterialRGB(), block);
            itemColors.registerItemColorHandler((s, i) ->
                    block.getGtMaterial(s.getMetadata()).getMaterialRGB(), block);
        }

        /*
        SHEETED_FRAMES.values().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(FRAME_BLOCK_COLOR, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(FRAME_ITEM_COLOR, block);
        });
         */
    }

    public static void registerOreDict() {
        for (Entry<Material, BlockSheetedFrame> entry : SHEETED_FRAMES.entrySet()) {
            Material material = entry.getKey();
            if (material == Materials.NULL) continue;

            BlockSheetedFrame block = entry.getValue();
            ItemStack itemStack = block.getItem(material);
            OreDictUnifier.registerOre(itemStack, SusyOrePrefix.sheetedFrame, material);
            OreDictUnifier.registerOre(itemStack, new ItemMaterialInfo(new MaterialStack(material, 1)));
        }


    }

    public static String statePropertiesToString(Map<IProperty<?>, Comparable<?>> properties) {
        StringBuilder stringbuilder = new StringBuilder();

        List<Entry<IProperty<?>, Comparable<?>>> entries = properties.entrySet().stream()
                .sorted(Comparator.comparing(c -> c.getKey().getName()))
                .collect(Collectors.toList());

        for (Map.Entry<IProperty<?>, Comparable<?>> entry : entries) {
            if (stringbuilder.length() != 0) {
                stringbuilder.append(",");
            }

            IProperty<?> property = entry.getKey();
            stringbuilder.append(property.getName());
            stringbuilder.append("=");
            stringbuilder.append(getPropertyName(property, entry.getValue()));
        }

        if (stringbuilder.length() == 0) {
            stringbuilder.append("normal");
        }

        return stringbuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }

}
