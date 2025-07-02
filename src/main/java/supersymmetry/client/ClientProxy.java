package supersymmetry.client;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.input.KeyBind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.SuSyValues;
import supersymmetry.Supersymmetry;
import supersymmetry.api.recipes.catalysts.CatalystGroup;
import supersymmetry.api.recipes.catalysts.CatalystInfo;
import supersymmetry.common.CommonProxy;
import supersymmetry.common.SusyMetaEntities;
import supersymmetry.common.blocks.SheetedFrameItemBlock;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SuSyMetaBlocks;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.item.behavior.PipeNetWalkerBehavior;
import supersymmetry.loaders.SuSyFluidTooltipLoader;
import supersymmetry.loaders.SuSyIRLoader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static supersymmetry.common.EventHandlers.FIRST_SPAWN;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {
    private static double titleRenderTimer = -1;

    public void preLoad() {
        super.preLoad();
        SusyMetaEntities.initRenderers();
        SuSyIRLoader.initEntityRenderers();
    }

    @Override
    public void load() {
        super.load();
        SuSyMetaBlocks.registerColors();
        SuSyFluidTooltipLoader.registerTooltips();
    }

    @SubscribeEvent
    public static void addMaterialFormulaHandler(@Nonnull ItemTooltipEvent event) {
        //ensure itemstack is a sheetedframe
        ItemStack itemStack = event.getItemStack();
        if (!(itemStack.getItem() instanceof SheetedFrameItemBlock)) return;

        UnificationEntry unificationEntry = OreDictUnifier.getUnificationEntry(itemStack);

        //ensure chemical composition does exist to be added
        if (unificationEntry != null && unificationEntry.material != null) {
            if (unificationEntry.material.getChemicalFormula() != null && !unificationEntry.material.getChemicalFormula().isEmpty())
                //pretty YELLOW is being auto-converted to a string
                event.getToolTip().add(TextFormatting.YELLOW + unificationEntry.material.getChemicalFormula());
        }
    }

    @SubscribeEvent
    public static void addPipelinerTooltip(@Nonnull ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<String> tooltips = event.getToolTip();

        if (stack.getItem() instanceof IGTTool tool
                && tool.getToolStats().getBehaviors().contains(PipeNetWalkerBehavior.INSTANCE)) {
            tooltips.add(I18n.format("item.susy.tool.tooltip.pipeliner",
                    GameSettings.getKeyDisplayString(KeyBind.TOOL_AOE_CHANGE.toMinecraft().getKeyCode())));
        }
    }

    @SubscribeEvent
    public static void addCatalystTooltipHandler(@Nonnull ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        // Handles Item tooltips
        Collection<String> tooltips = new ArrayList<>();

        if (itemStack.getItem() instanceof MetaOreDictItem oreDictItem) { // Test for OreDictItems
            Optional<String> oreDictName = OreDictUnifier.getOreDictionaryNames(itemStack).stream().findFirst();
            if (oreDictName.isPresent() && oreDictItem.OREDICT_TO_FORMULA.containsKey(oreDictName.get()) && !oreDictItem.OREDICT_TO_FORMULA.get(oreDictName.get()).isEmpty()) {
                tooltips.add(TextFormatting.YELLOW + oreDictItem.OREDICT_TO_FORMULA.get(oreDictName.get()));
            }
        }

        for (CatalystGroup group :
                CatalystGroup.getCatalystGroups()) {
            ItemStack is = itemStack.copy();
            is.setCount(1);
            CatalystInfo catalystInfo = group.getCatalystInfos().get(is);
            if (catalystInfo != null) {
                tooltips.add(TextFormatting.UNDERLINE + (TextFormatting.BLUE + I18n.format("gregtech.catalyst_group." + group.getName() + ".name")));
                if(catalystInfo.getTier() == CatalystInfo.NO_TIER){
                    tooltips.add(TextFormatting.RED + "Disclaimer: Catalyst bonuses for non-tiered catalysts have not yet been implemented.");
                    tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.yield", catalystInfo.getYieldEfficiency()));
                    tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.energy", catalystInfo.getEnergyEfficiency()));
                    tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.speed", catalystInfo.getSpeedEfficiency()));
                } else {
                    tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.tier", GTValues.V[catalystInfo.getTier()], GTValues.VNF[catalystInfo.getTier()]));
                    tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.yield.tiered", catalystInfo.getYieldEfficiency()));
                    tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.energy.tiered", catalystInfo.getEnergyEfficiency()));
                    tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.speed.tiered", catalystInfo.getSpeedEfficiency()));
                }
            }
        }

        event.getToolTip().addAll(tooltips);
    }

    @SubscribeEvent
    public static void registerModels(@NotNull ModelRegistryEvent event) {
        SuSyBlocks.registerItemModels();
        SuSyMetaBlocks.registerItemModels();
    }

    @SubscribeEvent
    public static void bakeModel(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
        try {
            IModel model = OBJLoader.INSTANCE.loadModel(new ResourceLocation(Supersymmetry.MODID, "models/entity/soyuz.obj"));
            registry.putObject(SuSyValues.modelRocket, model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void stitchTexture(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        map.registerSprite(new ResourceLocation(Supersymmetry.MODID, "entities/soyuz"));
        map.registerSprite(new ResourceLocation(Supersymmetry.MODID, "armor/jet_wingpack"));
        SuSyMetaItems.armorItem.registerIngameModels(map);
    }


    @SubscribeEvent
    public static void onRenderGameOverlay(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        if (titleRenderTimer >= 0) {
            GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
            titleRenderTimer++;
            if (titleRenderTimer % 150 == 0) {
                int i = (int) titleRenderTimer / 150; // 0 doesn't happen
                // This is literally how you have to use this method. I'm sorry.
                gui.displayTitle(null,
                        null, 20, 100, 30);
                gui.displayTitle(null,
                        I18n.format("supersymmetry.subtitle." + i), 20, 100, 30);
                gui.displayTitle(I18n.format("supersymmetry.title." + i),
                        null, 20, 100, 30);
                if (i == 3) { // Three messages
                    titleRenderTimer = -1;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        NBTTagCompound playerData = event.player.getEntityData();
        NBTTagCompound data = playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG) ? playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG) : new NBTTagCompound();
        if (!data.getBoolean(FIRST_SPAWN)) {
            // Set up title cards
            titleRenderTimer = 0;
        }
    }
}
