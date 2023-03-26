package supersymmetry.common;

import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantItemBlock;
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
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.api.unification.ore.SusyStoneTypes;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.materials.SusyMaterials;
import supersymmetry.loaders.SuSyRecipeLoader;
import supersymmetry.loaders.SusyOreDictionaryLoader;

import java.util.Objects;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class CommonProxy {

    public void preLoad(){
        SusyOrePrefix.init();
        SusyStoneTypes.init();
    }
    @SubscribeEvent
    public static void registerBlocks(@NotNull RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(SuSyBlocks.COOLING_COIL);
        registry.register(SuSyBlocks.SINTERING_BRICK);
        registry.register(SuSyBlocks.COAGULATION_TANK_WALL);
        registry.register(SuSyBlocks.SUSY_STONE_SMOOTH);

    }

    @SubscribeEvent
    public static void registerItems(@NotNull RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        SuSyMetaItems.initSubItems();

        registry.register(createItemBlock(SuSyBlocks.COOLING_COIL, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.SINTERING_BRICK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.COAGULATION_TANK_WALL, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.SUSY_STONE_SMOOTH, VariantItemBlock::new));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMaterials(@NotNull GregTechAPI.MaterialEvent event) {
        SusyMaterials.init();
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
