package supersymmetry.common.metatileentities.single.electric;


import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import supersymmetry.api.metatileentity.PseudoMultiMachineMetaTileEntity;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.util.SuSyUtility;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityLatexCollector extends PseudoMultiMachineMetaTileEntity {

    private final int tankSize = 16000;

    public MetaTileEntityLatexCollector(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, SuSyRecipeMaps.LATEX_COLLECTOR_RECIPES, SusyTextures.LATEX_COLLECTOR_OVERLAY, tier, true, SuSyUtility.collectorTankSizeFunction);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLatexCollector(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new FluidTank(this.tankSize));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SusyTextures.LATEX_COLLECTOR_OVERLAY.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.isActive(), true);
    }

    @Override
    public <T> void addNotifiedInput(T input) {
        super.addNotifiedInput(input);
        this.onNeighborChanged();
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return super.isValidFrontFacing(facing) && facing != getOutputFacingFluids().getOpposite() && facing != getOutputFacingItems().getOpposite();
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.getOutputFacingFluids() == frontFacing.getOpposite() || this.getOutputFacingItems() == frontFacing.getOpposite()) {
            this.setOutputFacing(frontFacing.rotateY());
        }
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (this.getOutputFacing() == facing) {
                return false;
            } else if (this.hasFrontFacing() && facing == this.getFrontFacing() || facing == this.getFrontFacing().getOpposite()) {
                return false;
            } else {
                if (!this.getWorld().isRemote) {
                    this.setOutputFacing(facing);
                }
                return true;
            }
        } else {
            return super.onWrenchClick(playerIn, hand, facing, hitResult);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.set(1, I18n.format("gregtech.machine.latex_collector.tooltip"));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
