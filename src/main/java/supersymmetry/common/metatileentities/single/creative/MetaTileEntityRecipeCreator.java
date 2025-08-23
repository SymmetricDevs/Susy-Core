package supersymmetry.common.metatileentities.single.creative;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import dev.tianmi.sussypatches.api.metatileentity.mui2.IMui2Holder;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.metatileentity.Mui2Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityRecipeCreator extends MetaTileEntity implements IMui2Holder {
    private RecipeMap<?> map;
    private int mapId;

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

    public void setRecipeMap(String mapName) {

    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        //StringSyncValue mapName = new StringSyncValue(() -> map.getLocalizedName(), this::setRecipeMap);
        RecipeCreatorSyncHandler recipeCreatorSyncHandler = new RecipeCreatorSyncHandler();
        syncManager.syncValue("recipe_creator_data", 0, recipeCreatorSyncHandler);
        IPanelHandler recipeMapSelector = syncManager.panel("recipe_map_popup",
                createMapsPopup(recipeCreatorSyncHandler), true);
        return Mui2Utils.defaultPanel(this)
                .child(Flow.row()
                        .widthRel(1.0f)
                        .marginTop(1)
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
/*                                .overlay(GTGuiTextures.SPEAKER_ICON.asIcon()
                                        .size(18))*/ // TODO: make recipemap texture
                                .addTooltipLine(IKey.lang("gregtech.gui.alarm.sounds_popup_button"))));
    }

    private static String recipeMapSearchText = "";
    private static String getRecipeMapSearchText() {
        return recipeMapSearchText;
    }
    private static void setRecipeMapSearchText(String recipeMapSearchText) {
        recipeMapSearchText = recipeMapSearchText;
    }



    protected PanelSyncHandler.IPanelBuilder createMapsPopup(@NotNull RecipeCreatorSyncHandler rcSyncHandler) {
        return (syncManager, syncHandler) -> {
            List<IWidget> mapList = new ArrayList<>();
            int id = 0;
            StringSyncValue searchValue = new StringSyncValue(MetaTileEntityRecipeCreator::getRecipeMapSearchText,
                    MetaTileEntityRecipeCreator::setRecipeMapSearchText);
            for (RecipeMap<?> map : RecipeMap.getRecipeMaps()) {
                String name = map.getUnlocalizedName();
                id++;
                int finalId = id;
                mapList.add(Flow.row()
                        .widthRel(1.0f)
                        .coverChildrenHeight()
                        .child(new ButtonWidget<>()
                                .widthRel(1.0f)
                                .onMousePressed(mouse -> {
                                    rcSyncHandler.setRecipeMap(finalId);
                                    syncHandler.closePanel();
                                    return true;
                                })
                                .setEnabledIf((widget) ->
                                    name.contains(searchValue.getValue())
                                )
                                .addTooltipLine(IKey.lang("gregtech.gui.recipe_creator.set_map"))
                                .overlay(IKey.str(name))));
            }

            return Mui2Utils.createPopupPanel("recipe_map_selector", 200, 200)
                    .child(Flow.column()
                            .margin(5)
                            .child(IKey.lang("gregtech.gui.recipe_creator.recipe_maps")
                                    .asWidget())
                            .child(new TextFieldWidget()
                                    .left(2)
                                    .right(2)
                                    .marginTop(6)
                                    .value(searchValue))
                            .child(new ListWidget<>()
                                    .left(2)
                                    .right(2)
                                    .marginTop(6)
                                    .marginBottom(4)
                                    .expanded()
                                    .children(mapList)
                                    .collapseDisabledChild()));
        };
    }

    public RecipeMap<?> getMap(int mapId) {
        return RecipeMap.getRecipeMaps().get(mapId);
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
