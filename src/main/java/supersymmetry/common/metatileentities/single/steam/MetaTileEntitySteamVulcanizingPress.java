package supersymmetry.common.metatileentities.single.steam;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;


public class MetaTileEntitySteamVulcanizingPress extends SteamMetaTileEntity{
    public MetaTileEntitySteamVulcanizingPress(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, SuSyRecipeMaps.VULCANIZATION_RECIPES, SusyTextures.VULCANIZING_PRESS_OVERLAY, isHighPressure);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySteamVulcanizingPress(this.metaTileEntityId, this.isHighPressure);
    }

    protected boolean isBrickedCasing() {
        return true;
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(4, this, false);
    }
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(2, this, true);
    }

    public FluidTankList createImportFluidHandler() {
        super.createImportFluidHandler();
        return new FluidTankList(false, this.steamFluidTank, new FluidTank(16000), new FluidTank(16000));
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new FluidTank(16000));
    }

    public ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = super.createUITemplate(player);

        builder.widget(new RecipeProgressWidget(this.workableHandler::getProgressPercent, 76, 39, 20, 15, GuiTextures.PROGRESS_BAR_COMPRESS_STEAM.get(false), ProgressWidget.MoveType.HORIZONTAL, SuSyRecipeMaps.VULCANIZATION_RECIPES));

        builder.widget((new SlotWidget(this.importItems, 0, 30, 30, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 1, 48, 30, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 2, 30, 48, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 3, 48, 48, true, true).setBackgroundTexture(SusyGuiTextures.MOLD_OVERLAY_STEAM.get(false))))
        .widget((new TankWidget(this.importFluids.getTankAt(1), 12, 30, 18, 18)).setAlwaysShowFull(true).setBackgroundTexture(SusyGuiTextures.FLUID_SLOT_STEAM.get(false)).setContainerClicking(true, true))
        .widget((new TankWidget(this.importFluids.getTankAt(2), 12, 48, 18, 18)).setAlwaysShowFull(true).setBackgroundTexture(SusyGuiTextures.FLUID_SLOT_STEAM.get(false)).setContainerClicking(true, true))
        .widget((new SlotWidget(this.exportItems, 0, 106, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.exportItems, 1, 124, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new TankWidget(this.exportFluids.getTankAt(0),142, 39, 18, 18)).setAlwaysShowFull(true).setBackgroundTexture(SusyGuiTextures.FLUID_SLOT_STEAM.get(false)).setContainerClicking(false, true));

        return builder.build(this.getHolder(),player);
    }

    @SideOnly(Side.CLIENT)
    protected void randomDisplayTick(float x, float y, float z, EnumParticleTypes flame, EnumParticleTypes smoke) {
        super.randomDisplayTick(x, y, z, flame, smoke);
        if (GTValues.RNG.nextBoolean()) {
            this.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, (double)(y + 0.5F), z, 0.0, 0.0, 0.0);
        }

    }
}
