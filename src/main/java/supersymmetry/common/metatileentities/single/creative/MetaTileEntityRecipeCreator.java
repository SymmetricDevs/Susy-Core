package supersymmetry.common.metatileentities.single.creative;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.LongValue;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import dev.tianmi.sussypatches.api.metatileentity.mui2.IMui2Holder;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.ClipboardUtil;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.integration.RecipeCompatUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.metatileentity.Mui2Utils;
import supersymmetry.common.mui.widget.GTFluidSlot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetaTileEntityRecipeCreator extends MetaTileEntity implements IMui2Holder {
    private RecipeMap<?> map;
    private int mapId;
    private long EUt;
    private int duration;

    public MetaTileEntityRecipeCreator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityRecipeCreator(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] renderPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[GTValues.LV].render(renderState, translation, renderPipeline);
    }

    @Override
    protected void initializeInventory() {
        this.importItems = new ItemStackHandler(16);
        NotifiableFluidTank[] fluidImports = new NotifiableFluidTank[8];
        for (int i = 0; i < fluidImports.length; i++) {
            NotifiableFluidTank filteredFluidHandler = new NotifiableFluidTank(
                    256000, this, false);
            fluidImports[i] = filteredFluidHandler;
        }
        this.importFluids = new FluidTankList(false, fluidImports);

        this.exportItems = new ItemStackHandler(16);
        NotifiableFluidTank[] fluidExports = new NotifiableFluidTank[8];
        for (int i = 0; i < fluidImports.length; i++) {
            NotifiableFluidTank filteredFluidHandler = new NotifiableFluidTank(
                    256000, this, true);
            fluidExports[i] = filteredFluidHandler;
        }
        this.exportFluids = new FluidTankList(false, fluidExports);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        return null;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        //StringSyncValue mapName = new StringSyncValue(() -> map.getLocalizedName(), this::setRecipeMap);
        RecipeCreatorSyncHandler recipeCreatorSyncHandler = new RecipeCreatorSyncHandler();
        syncManager.syncValue("recipe_creator_data", 0, recipeCreatorSyncHandler);
        IPanelHandler recipeMapSelector = syncManager.panel("recipe_map_popup",
                createMapsPopup(recipeCreatorSyncHandler), true);
        return Mui2Utils.createPanel(this, 310, 300)
                .child(Flow.row()
                        .widthRel(1.0f)
                        .margin(5)
                        .coverChildrenHeight()
                        .child(new ButtonWidget<>()
                                .marginRight(4)
                                .size(16)
                                .onMousePressed(mouse -> {
                                    if (recipeMapSelector.isPanelOpen()) {
                                        recipeMapSelector.closePanel();
                                    } else {
                                        recipeMapSelector.openPanel();
                                    }
                                    return true;
                                })
                               .overlay(SusyGuiTextures.SEARCH.asIcon())
                                .addTooltipLine(IKey.lang("susy.gui.recipe_creator.recipe_map_popup_button")))
                        .child(IKey.lang(() -> map == null ? "susy.gui.recipe_creator.unselected" : map.getUnlocalizedName())
                                .asWidget()
                                .alignY(0.5f)
                                .expanded()
                                .addTooltipLine(IKey.lang("susy.gui.recipe_creator.selected_recipe_map"))))
                .child(Flow.column()
                        .margin(5)
                        .collapseDisabledChild()
                        .heightRel(0.08f)
                        .alignY(0.08f)
                        .coverChildrenWidth()
                        .child(IKey.lang("susy.gui.recipe_creator.import_items").asWidget().alignX(Alignment.CenterLeft)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxInputs() > 0))
                        .child(createIngredientRow(16, (i) -> new ItemSlot().slot(this.importItems, i)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxInputs() > i))
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxInputs() > 0))
                        .child(IKey.lang("gregtech.gui.recipe_creator.import_fluids").asWidget().alignX(Alignment.CenterLeft)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidInputs() > 0))
                        .child(createIngredientRow(8, (i) -> new GTFluidSlot()
                                .syncHandler(GTFluidSlot.sync(importFluids.getTankAt(i)).drawAlwaysFull(true))
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidInputs() > i))
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidInputs() > 0))
                        .child(IKey.lang("susy.gui.recipe_creator.export_items").asWidget().alignX(Alignment.CenterLeft)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxOutputs() > 0))
                        .child(createIngredientRow(16, (i) -> new ItemSlot().slot(this.exportItems, i)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxOutputs() > i))
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxOutputs() > 0))
                        .child(IKey.lang("susy.gui.recipe_creator.export_fluids").asWidget().alignX(Alignment.CenterLeft)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidOutputs() > 0))
                        .child(createIngredientRow(8, (i) -> new GTFluidSlot()
                                .syncHandler(GTFluidSlot.sync(exportFluids.getTankAt(i)).drawAlwaysFull(true))
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidOutputs() > i))
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidOutputs() > 0))
                        .child(Flow.row()
                                .margin(5)
                                .childPadding(5)
                                .child(IKey.lang("susy.gui.recipe_creator.duration").asWidget())
                                .child(new TextFieldWidget().setNumbers()
                                        .value(new IntValue.Dynamic(() -> duration, this::setDuration)).width(100))
                                .setEnabledIf((w) -> this.map != null))
                        .child(Flow.row()
                                .margin(5)
                                .childPadding(5)
                                .child(IKey.lang("susy.gui.recipe_creator.eut").asWidget())
                                .child(new TextFieldWidget().setNumbers()
                                        .value(new LongValue.Dynamic(() -> EUt, this::setEUt)).width(100))
                                .setEnabledIf((w) -> this.map != null)))

                .child(new ButtonWidget<>().overlay(IKey.lang("susy.gui.recipe_creator.copy_script")).widthRel(0.2f).onMousePressed((button) -> {
                    if (button != 0) return false;

                    ClipboardUtil.copyToClipboard(getGroovyScript());
                    return true;
                }).setEnabledIf((w) -> this.map != null).alignX(Alignment.BottomRight).alignY(0.65f).marginRight(5))
                .bindPlayerInventory();
    }

    public String getGroovyScript() {
        StringBuilder result = new StringBuilder();
        result.append("recipemap('").append(this.map.getUnlocalizedName()).append("').recipeBuilder()");
        for (int i = 0; i < map.getMaxInputs(); i++) {
            if (this.importItems.getStackInSlot(i).isEmpty()) {
                continue;
            }
            result.append("\n\t.inputs(").append(getImportItemGroovy(this.importItems.getStackInSlot(i))).append(")");
        }
        for (int i = 0; i < map.getMaxFluidInputs(); i++) {
            if (this.importFluids.getTankAt(i).getFluid() == null || this.importFluids.getTankAt(i).getFluidAmount() == 0) {
                continue;
            }
            result.append("\n\t.fluidInputs(").append(getFluidGroovy(this.importFluids.getTankAt(i).getFluid())).append(")");
        }
        for (int i = 0; i < map.getMaxOutputs(); i++) {
            if (this.exportItems.getStackInSlot(i).isEmpty()) {
                continue;
            }
            result.append("\n\t.outputs(").append(getExportItemGroovy(this.exportItems.getStackInSlot(i))).append(")");
        }
        for (int i = 0; i < map.getMaxFluidOutputs(); i++) {
            if (this.exportFluids.getTankAt(i).getFluid() == null || this.exportFluids.getTankAt(i).getFluidAmount() == 0) {
                continue;
            }
            result.append("\n\t.fluidOutputs(").append(getFluidGroovy(this.exportFluids.getTankAt(i).getFluid())).append(")");
        }
        result.append("\n\t.duration(").append(this.duration).append(")");
        result.append("\n\t.EUt(").append(this.EUt).append(")");
        result.append("\n\t.buildAndRegister()");

        return result.toString();
    }

    private String getImportItemGroovy(ItemStack stack) {
        OrePrefix orePrefix = OreDictUnifier.getPrefix(stack);
        if (orePrefix == null) {
            return getExportItemGroovy(stack);
        }
        return addAmount("ore('" + orePrefix + "')", stack.getCount());
    }

    private String getExportItemGroovy(ItemStack stack) {
        return addAmount("metaitem('" + RecipeCompatUtil.getMetaItemId(stack) + "')", stack.getCount());
    }

    private String getFluidGroovy(FluidStack stack) {
        return addAmount("fluid('" + stack.getFluid().getName() + "')", stack.amount);
    }

    private String addAmount(String string, int amount) {
        if (amount == 1) return string;
        return string + " * " + amount;
    }

    public Flow createIngredientRow(int amount, Function<Integer, IWidget> widgetSupplier) {
        Flow result = Flow.row()
                .widthRel(1.0f)
                .margin(5);
        for (int i = 0; i < amount; i++) {
            result.child(widgetSupplier.apply(i));
        }
        return result;
    }

    private static String recipeMapSearchText = "";

    private static String getRecipeMapSearchText() {
        return recipeMapSearchText;
    }

    private static void setRecipeMapSearchText(String newText) {
        recipeMapSearchText = newText;
    }


    protected PanelSyncHandler.IPanelBuilder createMapsPopup(@NotNull RecipeCreatorSyncHandler rcSyncHandler) {
        return (syncManager, syncHandler) -> {
            List<IWidget> mapList = new ArrayList<>();
            StringSyncValue searchValue = new StringSyncValue(MetaTileEntityRecipeCreator::getRecipeMapSearchText,
                    MetaTileEntityRecipeCreator::setRecipeMapSearchText);

            TextFieldWidget textFieldWidget = new TextFieldWidget()
                    .left(2)
                    .right(2)
                    .marginTop(6)
                    .value(searchValue);

            for (RecipeMap<?> map :
                    RecipeMap.getRecipeMaps().stream().sorted(Comparator.comparing(RecipeMap::getUnlocalizedName)).collect(Collectors.toList())) {
                String name = map.getUnlocalizedName();
                int id = RecipeMap.getRecipeMaps().indexOf(map);
                mapList.add(Flow.row()
                        .widthRel(1.0f)
                        .coverChildrenHeight()
                        .child(new ButtonWidget<>()
                                .widthRel(1.0f)
                                .onMousePressed(mouse -> {
                                    rcSyncHandler.setRecipeMap(id);
                                    syncHandler.closePanel();
                                    return true;
                                })
                                .setEnabledIf((widget1) ->
                                        name.contains(textFieldWidget.getText())
                                )
                                .addTooltipLine(IKey.lang("gregtech.gui.recipe_creator.set_map"))
                                .overlay(IKey.str(name)))
                        .setEnabledIf((widget) ->
                                name.contains(textFieldWidget.getText())
                        ));
            }

            return Mui2Utils.createPopupPanel("recipe_map_selector", 200, 200)
                    .child(Flow.column()
                            .margin(5)
                            .child(IKey.lang("gregtech.gui.recipe_creator.recipe_maps")
                                    .asWidget())
                            .child(textFieldWidget)
                            .child(new ListWidget<>()
                                    .left(2)
                                    .right(2)
                                    .marginTop(6)
                                    .marginBottom(4)
                                    .expanded()
                                    .children(mapList)
                                    .collapseDisabledChild()
                                    .onUpdateListener((widget) -> {
                                        widget.getScrollData().clamp(widget.getScrollArea());
                                    }))
                    );
        };
    }

    public RecipeMap<?> getMap(int mapId) {
        return RecipeMap.getRecipeMaps().get(mapId);
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == SuSyDataCodes.UPDATE_RECIPE_MAP) {
            this.mapId = buf.readInt();
            this.map = getMap(mapId);
        }
    }

    public void setEUt(long EUt) {
        this.EUt = EUt;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private class RecipeCreatorSyncHandler extends SyncHandler {
        @Override
        public void readOnClient(int id, PacketBuffer buf) throws IOException {

        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) throws IOException {
            if (id == 1) {
                mapId = buf.readInt();
                map = getMap(mapId);
                writeCustomData(SuSyDataCodes.UPDATE_RECIPE_MAP,
                        toClients -> toClients.writeInt(mapId));
                markDirty();
            }
        }

        @SideOnly(Side.CLIENT)
        public void setRecipeMap(@NotNull int id) {
            if (id == mapId) return;
            syncToServer(1, buf -> buf.writeInt(id));
        }
    }
}
