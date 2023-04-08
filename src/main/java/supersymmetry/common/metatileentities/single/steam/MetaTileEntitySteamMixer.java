package supersymmetry.common.metatileentities.single.steam;

import gregtech.api.GTValues;
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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.gui.SusyGuiTextures;


public class MetaTileEntitySteamMixer extends SteamMetaTileEntity{
    public MetaTileEntitySteamMixer(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, RecipeMaps.MIXER_RECIPES, Textures.MIXER_OVERLAY, isHighPressure);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySteamMixer(this.metaTileEntityId, this.isHighPressure);
    }

    protected boolean isBrickedCasing() {
        return false;
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(6, this, false);
    }
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(1, this, true);
    }

    public FluidTankList createImportFluidHandler() {
        super.createImportFluidHandler();
        return new FluidTankList(false, new IFluidTank[]{this.steamFluidTank, new FluidTank(16000), new FluidTank(16000)});
    }
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new IFluidTank[]{new FluidTank(16000)});
    }

    public ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = super.createUITemplate(player);

        builder.widget(new RecipeProgressWidget(this.workableHandler::getProgressPercent, 78, 39, 20, 20, SusyGuiTextures.PROGRESS_BAR_MIXER_STEAM.get(false), ProgressWidget.MoveType.CIRCULAR, RecipeMaps.MIXER_RECIPES));

        builder.widget((new SlotWidget(this.importItems, 0, 12, 21, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 1, 30, 21, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 2, 48, 21, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 3, 12, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 4, 30, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new SlotWidget(this.importItems, 5, 48, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new TankWidget(this.importFluids.getTankAt(1), 30, 57, 18, 18)).setAlwaysShowFull(true).setBackgroundTexture(SusyGuiTextures.FLUID_SLOT_STEAM.get(false)).setContainerClicking(true, true))
        .widget((new TankWidget(this.importFluids.getTankAt(2), 48, 57, 18, 18)).setAlwaysShowFull(true).setBackgroundTexture(SusyGuiTextures.FLUID_SLOT_STEAM.get(false)).setContainerClicking(true, true))
        .widget((new SlotWidget(this.exportItems, 0, 106, 39, true, true).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false))))
        .widget((new TankWidget(this.exportFluids.getTankAt(0),124, 39, 18, 18)).setAlwaysShowFull(true).setBackgroundTexture(SusyGuiTextures.FLUID_SLOT_STEAM.get(false)).setContainerClicking(false, true));

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
