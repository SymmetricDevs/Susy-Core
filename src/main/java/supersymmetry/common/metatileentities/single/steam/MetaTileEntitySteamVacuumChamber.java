package supersymmetry.common.metatileentities.single.steam;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.b3d.B3DModel;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;


public class MetaTileEntitySteamVacuumChamber extends SteamMetaTileEntity{
    public MetaTileEntitySteamVacuumChamber(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, isHighPressure);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySteamVacuumChamber(this.metaTileEntityId, this.isHighPressure);
    }

    protected boolean isBrickedCasing() {
        return false;
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(4, this, false);
    }
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(1, this, true);
    }

    public ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = super.createUITemplate(player);

        builder.widget(new RecipeProgressWidget(this.workableHandler::getProgressPercent, 78, 39, 20, 20, GuiTextures.PROGRESS_BAR_COMPRESS_STEAM.get(false), ProgressWidget.MoveType.CIRCULAR, SuSyRecipeMaps.VACUUM_CHAMBER));

        builder.widget((new SlotWidget(this.importItems, 0, 30, 30, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 1, 48, 30, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 2, 30, 48, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 3, 48, 48, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.exportItems, 0, 106, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))));

        return builder.build(this.getHolder(),player);
    }

}
