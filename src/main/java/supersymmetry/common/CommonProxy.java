package supersymmetry.common;

import gregtech.api.block.VariantBlock;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.modules.ModuleContainerRegistryEvent;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
import supersymmetry.modules.SuSyModules;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static supersymmetry.common.blocks.SuSyMetaBlocks.SHEETED_FRAMES;

import static supersymmetry.common.blocks.SuSyBlocks.*;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class CommonProxy {

    public void preLoad() {
        GeckoLib.initialize();
        SusyStoneTypes.init();
        SuSyRecipeMaps.init();
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
        for (SusyStoneVariantBlock block : SuSyBlocks.SUSY_STONE_BLOCKS.values()) registry.register(block);
        Block[] toBeRegistered = {
                ALTERNATOR_COIL, COAGULATION_TANK_WALL, DRILL_HEAD, DRILL_BIT,
                TURBINE_ROTOR, SEPARATOR_ROTOR, STRUCTURAL_BLOCK, STRUCTURAL_BLOCK_1,
                DEPOSIT_BLOCK, RESOURCE_BLOCK, RESOURCE_BLOCK_1, HOME,
                INTERSTAGE, TANK_SHELL, OUTER_HATCH, FAIRING_HULL, ROCKET_CONTROL,
                ROCKET_NOZZLE, COMBUSTION_CHAMBER, TURBOPUMP, ROOM_PADDING,
                FAIRING_CONNECTOR, LIFE_SUPPORT, SPACECRAFT_HULL, HARDBLOCKS,
                CUSTOMSHEETS, CONVEYOR_BELT, ROCKET_ASSEMBLER_CASING, MULTIBLOCK_CASING,
                MULTIBLOCK_TANK, EVAPORATION_BED, ELECTRODE_ASSEMBLY, SERPENTINE,
                COOLING_COIL, SINTERING_BRICK
        };
        Arrays.stream(toBeRegistered).forEach(registry::register);

        SHEETED_FRAMES.values().stream().distinct().forEach(registry::register);
    }

    @SubscribeEvent
    public static void registerItems(@NotNull RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        SuSyMetaItems.initSubItems();

        for (SusyStoneVariantBlock block : SuSyBlocks.SUSY_STONE_BLOCKS.values())
            registry.register(createItemBlock(block, VariantItemBlock::new));
        VariantBlock[] toBeRegistered = {
                ALTERNATOR_COIL, COAGULATION_TANK_WALL, DRILL_HEAD, DRILL_BIT,
                TURBINE_ROTOR, SEPARATOR_ROTOR, STRUCTURAL_BLOCK, STRUCTURAL_BLOCK_1,
                DEPOSIT_BLOCK, RESOURCE_BLOCK, RESOURCE_BLOCK_1, HOME,
                INTERSTAGE, TANK_SHELL, OUTER_HATCH, FAIRING_HULL, ROCKET_CONTROL,
                ROCKET_NOZZLE, COMBUSTION_CHAMBER, TURBOPUMP, ROOM_PADDING,
                FAIRING_CONNECTOR, LIFE_SUPPORT, SPACECRAFT_HULL, HARDBLOCKS,
                CUSTOMSHEETS, CONVEYOR_BELT, ROCKET_ASSEMBLER_CASING, MULTIBLOCK_CASING,
                MULTIBLOCK_TANK, EVAPORATION_BED, ELECTRODE_ASSEMBLY, SERPENTINE,
                COOLING_COIL, SINTERING_BRICK
        };
        Arrays.stream(toBeRegistered).distinct().forEach(vb -> registry.register(createItemBlock(vb, VariantItemBlock::new)));
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

        //SusyMaterials.removeFlags();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void itemToolTip(ItemTooltipEvent event) {
        handleCoilTooltips(event);
        addTooltip(event, "gregtech.machine.steam_extractor", TooltipHelper.BLINKING_ORANGE + I18n.format("gregtech.machine.steam_extractor_cannot_melt_items.warning"), 2);
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
}
