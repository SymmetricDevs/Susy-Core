package supersymmetry.common;

import gregtech.api.block.VariantItemBlock;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;
import gregtech.common.items.MetaItems;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.GeckoLib;
import supersymmetry.Supersymmetry;
import supersymmetry.api.event.MobHordeEvent;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.api.unification.ore.SusyStoneTypes;
import supersymmetry.common.blocks.SheetedFrameItemBlock;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SuSyMetaBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.materials.SusyMaterials;
import supersymmetry.loaders.SuSyWorldLoader;
import supersymmetry.loaders.SusyOreDictionaryLoader;
import supersymmetry.loaders.recipes.SuSyRecipeLoader;

import java.util.Objects;
import java.util.function.Function;

import static supersymmetry.common.blocks.SuSyMetaBlocks.SHEETED_FRAMES;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class CommonProxy {

    public void preLoad(){
        GeckoLib.initialize();
        SusyStoneTypes.init();
        SuSyRecipeMaps.init();
    }

    public void load() {
        SuSyWorldLoader.init();
        new MobHordeEvent((p) -> new EntityZombie(p.world), 4, 8, "zombies").setMaximumDistanceUnderground(10).setNightOnly(true);
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
        registry.register(SuSyBlocks.SEPARATOR_ROTOR);
        registry.register(SuSyBlocks.STRUCTURAL_BLOCK);
        registry.register(SuSyBlocks.STRUCTURAL_BLOCK_1);
        registry.register(SuSyBlocks.DRILL_HEAD);
        registry.register(SuSyBlocks.DEPOSIT_BLOCK);
        registry.register(SuSyBlocks.RESOURCE_BLOCK);
        registry.register(SuSyBlocks.HOME);
        registry.register(SuSyBlocks.MULTIBLOCK_TANK);
        registry.register(SuSyBlocks.EVAPORATION_BED);
        registry.register(SuSyBlocks.ELECTRODE_ASSEMBLY);
        registry.register(SuSyBlocks.MULTIBLOCK_CASING);
        registry.register(SuSyBlocks.SERPENTINE);

        SHEETED_FRAMES.values().stream().distinct().forEach(registry::register);
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
        registry.register(createItemBlock(SuSyBlocks.DRILL_HEAD, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.TURBINE_ROTOR, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.SEPARATOR_ROTOR, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.STRUCTURAL_BLOCK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.STRUCTURAL_BLOCK_1, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.DEPOSIT_BLOCK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.RESOURCE_BLOCK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.HOME, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.EVAPORATION_BED, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.MULTIBLOCK_TANK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.ELECTRODE_ASSEMBLY, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.MULTIBLOCK_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.SERPENTINE, VariantItemBlock::new));


        SHEETED_FRAMES.values()
                .stream().distinct()
                .map(block -> createItemBlock(block, SheetedFrameItemBlock::new))
                .forEach(registry::register);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMaterials(@NotNull MaterialEvent event) {
        SusyMaterials.init();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void postRegisterMaterials(@NotNull PostMaterialEvent event) {
        MetaItems.addOrePrefix(SusyOrePrefix.catalystPellet);
        MetaItems.addOrePrefix(SusyOrePrefix.catalystBed);
        MetaItems.addOrePrefix(SusyOrePrefix.flotated);
        MetaItems.addOrePrefix(SusyOrePrefix.sifted);
        MetaItems.addOrePrefix(SusyOrePrefix.concentrate);
        MetaItems.addOrePrefix(SusyOrePrefix.fiber);
        MetaItems.addOrePrefix(SusyOrePrefix.wetFiber);
        MetaItems.addOrePrefix(SusyOrePrefix.thread);

        //SusyMaterials.removeFlags();
    }

    @SubscribeEvent()
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        SusyOreDictionaryLoader.init();
        SuSyMetaBlocks.registerOreDict();
        SuSyRecipeLoader.init();
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        return itemBlock;
    }
}
