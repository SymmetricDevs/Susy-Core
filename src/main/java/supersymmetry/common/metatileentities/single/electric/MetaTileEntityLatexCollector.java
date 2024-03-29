package supersymmetry.common.metatileentities.single.electric;


import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
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
    private final int tankSize;
    private EnumFacing outputFacingFluids;

    public MetaTileEntityLatexCollector(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, SuSyRecipeMaps.LATEX_COLLECTOR_RECIPES, SusyTextures.LATEX_COLLECTOR_OVERLAY, tier, true, SuSyUtility.collectorTankSizeFunction);
        this.tankSize = 16000;
        this.initializeInventory();
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLatexCollector(this.metaTileEntityId, this.getTier());
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new FluidTank(this.tankSize));
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }


    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SusyTextures.LATEX_COLLECTOR_OVERLAY.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.isActive(), true);
        if (this.getOutputFacingFluids() != null) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacingFluids(), renderState, translation, pipeline);
        }
    }

    public void update() {
        super.update();

        if (!this.getWorld().isRemote && this.getOffsetTimer() % 5L == 0L) {
            if(this.getOutputFacingFluids() != null){
                this.pushFluidsIntoNearbyHandlers(this.getOutputFacingFluids());
            }
            this.fillContainerFromInternalTank();
        }
    }

    public void onNeighborChanged() {
        super.onNeighborChanged();
        this.checkAdjacentBlocks();
    }

    public <T> void addNotifiedInput(T input) {
        super.addNotifiedInput(input);
        this.onNeighborChanged();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("OutputFacingF", this.getOutputFacingFluids().getIndex());
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("OutputFacingF")) {
            this.outputFacingFluids = EnumFacing.byIndex(data.getInteger("OutputFacingF"));
        }
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(this.getOutputFacingFluids().getIndex());
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 100) {
            this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
            this.scheduleRenderUpdate();
        }
    }

    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (this.getOutputFacingFluids() == facing) {
                return false;
            } else if (this.hasFrontFacing() && (facing == this.getFrontFacing() || facing == this.getFrontFacing().getOpposite())) {
                return false;
            } else {
                if (!this.getWorld().isRemote) {
                    this.setOutputFacingFluids(facing);
                }
                return true;
            }
        } else {
            return super.onWrenchClick(playerIn, hand, facing, hitResult);
        }
    }

    public void setOutputFacingFluids(EnumFacing outputFacing) {
        this.outputFacingFluids = outputFacing;
        if (!this.getWorld().isRemote) {
            this.notifyBlockUpdate();
            this.writeCustomData(100, (buf) -> {
                buf.writeByte(this.outputFacingFluids.getIndex());
            });
            this.markDirty();
        }
    }

    public EnumFacing getOutputFacingFluids() {
        return this.outputFacingFluids == null ? EnumFacing.SOUTH : this.outputFacingFluids;
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.set(1, I18n.format("gregtech.machine.latex_collector.tooltip"));
    }
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
