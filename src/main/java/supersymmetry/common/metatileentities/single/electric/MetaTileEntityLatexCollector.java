package supersymmetry.common.metatileentities.single.electric;


import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import supersymmetry.common.materials.SusyMaterials;

public class MetaTileEntityLatexCollector extends TieredMetaTileEntity {
    private final int tankSize;
    private final long latexCollectionAmount;
    private final long euT;
    private int numberRubberLogs;

    public MetaTileEntityLatexCollector(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.tankSize = 16000;
        this.latexCollectionAmount = 5L * (long)tier;
        this.euT = GTValues.V[tier];
        this.initializeInventory();
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLatexCollector(this.metaTileEntityId, this.getTier());
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = (new TankWidget(this.exportFluids.getTankAt(0), 69, 52, 18, 18)).setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 16777215);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 16777215);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 16777215);
        return builder.label(6, 6, this.getMetaFullName()).widget((new FluidContainerSlotWidget(this.importItems, 0, 90, 17, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY})).widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON)).widget((new SlotWidget(this.exportItems, 0, 90, 54, true, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY})).bindPlayerInventory(entityPlayer.inventory).build(this.getHolder(), entityPlayer);
    }

    public void onNeighborChanged() {
        super.onNeighborChanged();
        this.checkAdjacentBlocks();
    }

    public void checkAdjacentBlocks(){
        if(this.getWorld() != null){
            this.numberRubberLogs = 0;
            if(!this.getWorld().isRemote) {
                EnumFacing[] facings = EnumFacing.VALUES;
                int numFacings = facings.length;

                for (int i = 0; i < numFacings; ++i) {
                    EnumFacing side = facings[i];

                    if (side != this.frontFacing && !side.getAxis().isVertical()) {
                        Block block = this.getWorld().getBlockState(this.getPos().offset(side)).getBlock();
                        if (block == MetaBlocks.RUBBER_LOG) {
                            ++numberRubberLogs;
                        }
                    }
                }
            }
        }
    }

    public <T> void addNotifiedInput(T input) {
        super.addNotifiedInput(input);
        this.onNeighborChanged();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("numberRubberLogs", this.numberRubberLogs);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("numberRubberLogs")) {
            this.numberRubberLogs = data.getInteger("numberRubberLogs");
        }
    }
    public void update() {
        super.update();

        if (!this.getWorld().isRemote && this.energyContainer.getEnergyStored() >= this.euT && this.numberRubberLogs != 0) {
            FluidStack latexStack = SusyMaterials.Latex.getFluid((int) this.latexCollectionAmount * this.numberRubberLogs);
            NonNullList<FluidStack> fluidStacks = NonNullList.create();
            fluidStacks.add(latexStack);
            if (GTTransferUtils.addFluidsToFluidHandler(this.exportFluids, true, fluidStacks)) {
                GTTransferUtils.addFluidsToFluidHandler(this.exportFluids, false, fluidStacks);
                this.energyContainer.removeEnergy(this.euT);
            }

        }


        if (!this.getWorld().isRemote && this.getOffsetTimer() % 5L == 0L) {
            this.pushItemsIntoNearbyHandlers(new EnumFacing[]{this.getFrontFacing()});
            this.fillContainerFromInternalTank();

        }
    }
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new IFluidTank[]{new FluidTank(this.tankSize)});
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.latex_collector.tooltip", this.latexCollectionAmount));

        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", new Object[]{this.energyContainer.getInputVoltage(), GTValues.VNF[this.getTier()]}));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", new Object[]{this.energyContainer.getEnergyCapacity()}));
    }

    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
