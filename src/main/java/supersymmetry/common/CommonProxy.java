package supersymmetry.common;

import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.api.unification.ore.SusyStoneTypes;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.materials.SusyMaterials;
import supersymmetry.loaders.SuSyWorldLoader;
import supersymmetry.loaders.recipes.SuSyRecipeLoader;
import supersymmetry.loaders.SusyOreDictionaryLoader;

import java.util.Objects;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class CommonProxy {

    public void preLoad(){
        SusyStoneTypes.init();
        SuSyRecipeMaps.init();
    }

    public void load() {
        SuSyWorldLoader.init();
    }

    @SubscribeEvent
    public static void registerBlocks(@NotNull RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(SuSyBlocks.COOLING_COIL);
        registry.register(SuSyBlocks.SINTERING_BRICK);
        registry.register(SuSyBlocks.COAGULATION_TANK_WALL);
        for (SusyStoneVariantBlock block : SuSyBlocks.SUSY_STONE_BLOCKS.values()) registry.register(block);
        registry.register(SuSyBlocks.ALTERNATOR_COIL);
        registry.register(SuSyBlocks.TURBINE_ROTOR);
    }

    @SubscribeEvent
    public static void registerItems(@NotNull RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        SuSyMetaItems.initSubItems();

        registry.register(createItemBlock(SuSyBlocks.COOLING_COIL, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.SINTERING_BRICK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.COAGULATION_TANK_WALL, VariantItemBlock::new));
        for (SusyStoneVariantBlock block : SuSyBlocks.SUSY_STONE_BLOCKS.values()) registry.register(createItemBlock(block, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.ALTERNATOR_COIL, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.TURBINE_ROTOR, VariantItemBlock::new));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMaterials(@NotNull GregTechAPI.MaterialEvent event) {
        SusyMaterials.init();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void postRegisterMaterials(@NotNull GregTechAPI.PostMaterialEvent event) {
        MetaItems.addOrePrefix(SusyOrePrefix.catalystBed);
    }

    @SubscribeEvent()
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        SusyOreDictionaryLoader.init();
        SuSyRecipeLoader.init();
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        return itemBlock;
    }
}
