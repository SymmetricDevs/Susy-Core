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
import supersymmetry.api.recipe.SuSyRecipeMaps;
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
        return new NotifiableItemStackHandler(3, this, false);
    }
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(2, this, true);
    }

    public FluidTankList createImportFluidHandler() {
        super.createImportFluidHandler();
        return new FluidTankList(false, new IFluidTank[]{this.steamFluidTank, new FluidTank(16000)});
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new IFluidTank[]{new FluidTank(16000)});
    }

    public ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = super.createUITemplate(player);

        builder.widget(new RecipeProgressWidget(this.workableHandler::getProgressPercent, 76, 39, 20, 15, GuiTextures.PROGRESS_BAR_COMPRESS_STEAM.get(false), ProgressWidget.MoveType.HORIZONTAL, SuSyRecipeMaps.VULCANIZATION_RECIPES));

        builder.widget((new SlotWidget(this.importItems, 0, 12, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 1, 30, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 2, 48, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new TankWidget(this.importFluids.getTankAt(1), 48, 57, 18, 18)).setAlwaysShowFull(true).setBackgroundTexture(GuiTextures.FLUID_SLOT).setContainerClicking(true, true))
        .widget((new SlotWidget(this.exportItems, 0, 106, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.exportItems, 1, 124, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new TankWidget(this.exportFluids.getTankAt(0),142, 39, 18, 18)).setAlwaysShowFull(true).setBackgroundTexture(GuiTextures.FLUID_SLOT).setContainerClicking(false, true));

        return builder.build(this.getHolder(),player);
    }

    @SideOnly(Side.CLIENT)
    protected void randomDisplayTick(float x, float y, float z, EnumParticleTypes flame, EnumParticleTypes smoke) {
        super.randomDisplayTick(x, y, z, flame, smoke);
        if (GTValues.RNG.nextBoolean()) {
            this.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)x, (double)(y + 0.5F), (double)z, 0.0, 0.0, 0.0, new int[0]);
        }

    }
}
