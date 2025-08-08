package supersymmetry.common;

import gregtech.api.block.VariantItemBlock;
import gregtech.api.modules.ModuleContainerRegistryEvent;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.items.MetaItems;
import gregtech.modules.ModuleManager;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.GeckoLib;
import supersymmetry.Supersymmetry;
import supersymmetry.api.blocks.VariantItemBlockFalling;
import supersymmetry.api.SusyLog;
import supersymmetry.api.event.MobHordeEvent;
import supersymmetry.api.fluids.SusyGeneratedFluidHandler;
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.api.unification.ore.SusyStoneTypes;
import supersymmetry.common.blocks.SheetedFrameItemBlock;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SuSyMetaBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.materials.SusyMaterials;
import supersymmetry.common.world.biome.BiomeLunarMaria;
import supersymmetry.common.world.SuSyDimensions;
import supersymmetry.common.world.biome.BiomeLunarHighlands;
import supersymmetry.common.world.SuSyBiomes;
import supersymmetry.loaders.SuSyWorldLoader;
import supersymmetry.loaders.SusyOreDictionaryLoader;
import supersymmetry.loaders.recipes.SuSyRecipeLoader;
import supersymmetry.modules.SuSyModules;

import java.util.Objects;
import java.util.function.Function;

import static supersymmetry.common.blocks.SuSyMetaBlocks.SHEETED_FRAMES;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class CommonProxy {

    public void preLoad() {
        GeckoLib.initialize();
        SusyStoneTypes.init();
    }

    /**
     * Checks for a canary file in the config directory and deletes it if found.
     * Also cleans up the Groovy cache folder to prevent update issues.
     */
    public void checkCanaryFile() {
        try {
            // Handle canary file in config/susy/
            File configDir = new File(Loader.instance().getConfigDir(), "susy");
            if (configDir.exists() || configDir.mkdirs()) {
                File canaryFile = new File(configDir, "susy_canary");
                if (canaryFile.exists()) {
                    SusyLog.logger.info("Found canary file for update - performing cleanup actions");
                    if (!canaryFile.delete()) {
                        SusyLog.logger.warn("Failed to delete canary file: {}", canaryFile.getAbsolutePath());
                    }
                }
            } else {
                SusyLog.logger.warn("Failed to access or create susy config directory");
            }

            // Clean up Groovy cache
            File gameDir = Loader.instance().getConfigDir().getParentFile();
            File groovyCacheDir = new File(gameDir, "cache/groovy");
            if (groovyCacheDir.exists() && groovyCacheDir.isDirectory()) {
                SusyLog.logger.info("Cleaning up Groovy cache at: {}", groovyCacheDir.getAbsolutePath());
                deleteDirectory(groovyCacheDir);
            }
        } catch (Exception e) {
            SusyLog.logger.error("Error during cleanup operations", e);
        }
    }

    /**
     * Recursively deletes a directory and all its contents.
     *
     * @param directory the directory to delete
     * @return true if the directory was successfully deleted, false otherwise
     */
    private boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return directory.delete();
    }

    public void load() {
        SuSyWorldLoader.init();
        if ((boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
            new MobHordeEvent((p) -> new EntityZombie(p.world), 4, 8, "zombies").setMaximumDistanceUnderground(10).setNightOnly(true);
        }
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
        registry.register(SuSyBlocks.DRILL_BIT);
        registry.register(SuSyBlocks.DEPOSIT_BLOCK);
        registry.register(SuSyBlocks.RESOURCE_BLOCK);
        registry.register(SuSyBlocks.RESOURCE_BLOCK_1);
        registry.register(SuSyBlocks.HOME);
        registry.register(SuSyBlocks.MULTIBLOCK_TANK);
        registry.register(SuSyBlocks.EVAPORATION_BED);
        registry.register(SuSyBlocks.ELECTRODE_ASSEMBLY);
        registry.register(SuSyBlocks.MULTIBLOCK_CASING);
        registry.register(SuSyBlocks.SERPENTINE);
        registry.register(SuSyBlocks.HARDBLOCKS);
        registry.register(SuSyBlocks.HARDBLOCKS1);
        registry.register(SuSyBlocks.CUSTOMSHEETS);
        registry.register(SuSyBlocks.METALLURGY);
        registry.register(SuSyBlocks.METALLURGY_2);
        registry.register(SuSyBlocks.METALLURGY_ROLL);
        registry.register(SuSyBlocks.CONVEYOR_BELT);
        registry.register(SuSyBlocks.ROCKET_ASSEMBLER_CASING);
        registry.register(SuSyBlocks.REGOLITH);
        registry.register(SuSyBlocks.FAKEWOOL);

        SHEETED_FRAMES.values().stream().distinct().forEach(registry::register);
    }

    @SubscribeEvent
    public static void registerItems(@NotNull RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        SuSyMetaItems.initSubItems();

        registry.register(createItemBlock(SuSyBlocks.COOLING_COIL, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.SINTERING_BRICK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.COAGULATION_TANK_WALL, VariantItemBlock::new));
        for (SusyStoneVariantBlock block : SuSyBlocks.SUSY_STONE_BLOCKS.values())
            registry.register(createItemBlock(block, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.ALTERNATOR_COIL, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.DRILL_HEAD, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.DRILL_BIT, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.TURBINE_ROTOR, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.SEPARATOR_ROTOR, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.STRUCTURAL_BLOCK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.STRUCTURAL_BLOCK_1, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.DEPOSIT_BLOCK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.RESOURCE_BLOCK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.RESOURCE_BLOCK_1, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.HOME, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.EVAPORATION_BED, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.MULTIBLOCK_TANK, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.ELECTRODE_ASSEMBLY, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.MULTIBLOCK_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.SERPENTINE, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.HARDBLOCKS, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.HARDBLOCKS1, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.CUSTOMSHEETS, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.METALLURGY, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.METALLURGY_2, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.METALLURGY_ROLL, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.CONVEYOR_BELT, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.ROCKET_ASSEMBLER_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(SuSyBlocks.REGOLITH, VariantItemBlockFalling::new));
        registry.register(createItemBlock(SuSyBlocks.FAKEWOOL, VariantItemBlock::new));

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
        MetaItems.addOrePrefix(SusyOrePrefix.dustWet);
        MetaItems.addOrePrefix(SusyOrePrefix.electrode);

        Materials.Aluminium.addFlags("continuously_cast");
        SusyGeneratedFluidHandler.init();

        //SusyMaterials.removeFlags();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void itemToolTip(ItemTooltipEvent event) {
        handleCoilTooltips(event);
        addTooltip(event, "susy.machine.steam_extractor", TooltipHelper.BLINKING_ORANGE + I18n.format("susy.machine.steam_extractor_cannot_melt_items.warning"), 2);
    }

    private static void handleCoilTooltips(ItemTooltipEvent event) {
        Block block = Block.getBlockFromItem(event.getItemStack().getItem());
        if(block instanceof BlockWireCoil && TooltipHelper.isShiftDown()) {
            ItemStack itemStack = event.getItemStack();
            Item item = itemStack.getItem();
            BlockWireCoil wireCoilBlock = (BlockWireCoil)block;
            VariantItemBlock itemBlock = (VariantItemBlock)item;
            BlockWireCoil.CoilType coilType = (BlockWireCoil.CoilType)wireCoilBlock.getState(itemBlock.getBlockState(itemStack));
            event.getToolTip().add(I18n.format("tile.wire_coil.tooltip_evaporation", new Object[0]));
            event.getToolTip().add(I18n.format("tile.wire_coil.tooltip_energy_evaporating", new Object[]{coilType.getCoilTemperature()/1000}));
        }
    }

    // Since this function checks if the key is in the translation key, you can sometimes add tooltips to multiple items
    //   with a single call of the function. Useful for hitting both basic and high pressure steam machines, for example.
    private static void addTooltip(ItemTooltipEvent event, String key, String toolTip, int index) {
        if(event.getItemStack().getTranslationKey().contains(key)) {
            event.getToolTip().add(index, toolTip);
        }
    }

    @SubscribeEvent()
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        SusyOreDictionaryLoader.init();
        SuSyMetaBlocks.registerOreDict();
        SuSyRecipeLoader.init();
    }

    @SubscribeEvent
    public static void registerModuleContainer(ModuleContainerRegistryEvent event) {
        ModuleManager.getInstance().registerContainer(new SuSyModules());
    }


    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        return itemBlock;
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Biome> evt) {
        SuSyBiomes.LUNAR_HIGHLANDS = new BiomeLunarHighlands(new Biome.BiomeProperties("Lunar Highlands").setRainDisabled().setBaseHeight(1f).setHeightVariation(0.2f).setRainfall(0).setTemperature(0.3f));
        SuSyBiomes.LUNAR_HIGHLANDS.setRegistryName(Supersymmetry.MODID, "moon");
        evt.getRegistry().register(SuSyBiomes.LUNAR_HIGHLANDS);
        BiomeDictionary.addTypes(SuSyBiomes.LUNAR_HIGHLANDS, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.VOID);

        SuSyBiomes.LUNAR_MARIA = new BiomeLunarMaria(new Biome.BiomeProperties("Lunar Maria").setRainDisabled().setBaseHeight(0f).setHeightVariation(0.1f).setRainfall(0).setTemperature(0.3f));
        SuSyBiomes.LUNAR_MARIA.setRegistryName(Supersymmetry.MODID, "maria");
        evt.getRegistry().register(SuSyBiomes.LUNAR_MARIA);
        BiomeDictionary.addTypes(SuSyBiomes.LUNAR_MARIA, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.VOID);

        SuSyDimensions.init();
    }
}
