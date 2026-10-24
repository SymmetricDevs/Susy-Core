package supersymmetry.common.metatileentities.unpowered;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.metatileentity.UnpoweredMetaTileEntity;

public class SuSyFurnace extends UnpoweredMetaTileEntity {

    public SuSyFurnace(ResourceLocation metaTileEntityId, ICubeRenderer casingRenderer, ICubeRenderer overlayRenderer) {
        super(metaTileEntityId, RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES, casingRenderer, overlayRenderer);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SuSyFurnace(metaTileEntityId, casingRenderer, overlayRenderer);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 2, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 2, this, true);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        return createUITemplate(player)
                .slot(this.importItems, 0, 53, 25, GuiTextures.SLOT_STEAM.get(true), GuiTextures.FURNACE_OVERLAY_STEAM.get(true))
                .slot(this.importItems, 1, 53, 43, GuiTextures.SLOT_STEAM.get(true), GuiTextures.FURNACE_OVERLAY_STEAM.get(true))
                .progressBar(workableHandler::getProgressPercent, 78, 25, 20, 18,
                        GuiTextures.PROGRESS_BAR_ARROW_STEAM.get(true), ProgressWidget.MoveType.HORIZONTAL, workableHandler.getRecipeMap())
                .slot(this.exportItems, 0, 107, 25, true, false, GuiTextures.SLOT_STEAM.get(true))
                .slot(this.exportItems, 1, 107, 43, true, false, GuiTextures.SLOT_STEAM.get(true))
                .build(getHolder(), player);
    }
}
