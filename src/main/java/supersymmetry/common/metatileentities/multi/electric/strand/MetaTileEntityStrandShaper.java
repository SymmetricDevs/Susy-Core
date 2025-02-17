package supersymmetry.common.metatileentities.multi.electric.strand;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.capability.IStrandProvider;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;

import java.util.List;

public abstract class MetaTileEntityStrandShaper extends MultiblockWithDisplayBase implements IWorkable {

    protected Strand strand;
    protected IStrandProvider input;
    protected IStrandProvider output;

    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler inputFluidInventory;
    protected IEnergyContainer energyContainer;


    protected int progress;
    protected int maxProgress;
    protected boolean isActive;

    public MetaTileEntityStrandShaper(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        // Update progress if needed
        if (isActive) {
            if (progress <= maxProgress) {
                if (consumeEnergy()) {
                    progress++;
                } else {
                    if (progress > 0)
                        progress--;
                }
            } else if (!getWorld().isRemote) {
                // Output
                output();
                this.progress = 0;
                this.isActive = false;
            }
            if (!getWorld().isRemote) {
                this.markDirty();
            }
        }
        // Check if there is a resulting strand
        // Consume input strand if it exists
        if (!getWorld().isRemote && !isActive && resultingStrand() != null ) {
            if (!consumeInputsAndSetupRecipe()) {
                return;
            }
            strand = resultingStrand();
            isActive = true;
            this.markDirty();
        }
    }

    protected boolean consumeEnergy() {
        return energyContainer.changeEnergy(-getVoltage()) == -getVoltage();
    }

    protected FluidStack getFirstMaterialFluid() {
        for (IMultipleTankHandler.MultiFluidTankEntry tank : this.inputFluidInventory.getFluidTanks()) {
            FluidStack stack = tank.getFluid();
            if (stack == null || stack.amount == 0) {
                continue;
            }
            Material mat = FluidUnifier.getMaterialFromFluid(stack.getFluid());
            if (mat != null) {
                return stack;
            }
        }
        return null;
    }

    public abstract long getVoltage();

    protected abstract boolean consumeInputsAndSetupRecipe();

    protected abstract Strand resultingStrand();

    protected void output() {
        output.insertStrand(strand);
    }

    protected void initializeAbilities() {
        if (!this.getAbilities(SuSyMultiblockAbilities.STRAND_IMPORT).isEmpty())
            this.input = this.getAbilities(SuSyMultiblockAbilities.STRAND_IMPORT).get(0);
        if (!this.getAbilities(SuSyMultiblockAbilities.STRAND_EXPORT).isEmpty())
            this.output = this.getAbilities(SuSyMultiblockAbilities.STRAND_EXPORT).get(0);
        this.inputInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(true, this.getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        isActive = false;
        progress = 0;
        maxProgress = 0;
        super.invalidateStructure();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.progress);
        buf.writeInt(this.maxProgress);
        buf.writeBoolean(this.isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.progress = buf.readInt();
        this.maxProgress = buf.readInt();
        this.isActive = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("Progress", this.progress);
        data.setInteger("MaxProgress", this.maxProgress);
        data.setBoolean("IsActive", this.isActive);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.progress = data.getInteger("Progress");
        this.maxProgress = data.getInteger("MaxProgress");
        this.isActive = data.getBoolean("IsActive");
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        MultiblockDisplayText.builder(textList, this.isStructureFormed()).setWorkingStatus(true, isActive).addEnergyUsageLine(this.energyContainer).addEnergyTierLine(GTUtility.getTierByVoltage(this.energyContainer.getInputVoltage())).addWorkingStatusLine().addProgressLine(this.getProgressPercent());

    }

    private double getProgressPercent() {
        return progress / (double) maxProgress;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        // They cannot stop it.
    }
}
