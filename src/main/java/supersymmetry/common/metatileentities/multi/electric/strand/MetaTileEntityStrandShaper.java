package supersymmetry.common.metatileentities.multi.electric.strand;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.blocks.VariantAxialRotatableBlock;
import supersymmetry.api.capability.IStrandProvider;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.capability.StrandConversion;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.common.blocks.BlockMetallurgyRoll;
import supersymmetry.common.blocks.SuSyBlocks;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public abstract class MetaTileEntityStrandShaper extends MultiblockWithDisplayBase implements IWorkable {

    protected Strand strand;
    protected IStrandProvider input;
    protected IStrandProvider output;

    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler inputFluidInventory;
    protected IMultipleTankHandler outputFluidInventory;
    protected IEnergyContainer energyContainer;


    protected int progress;
    protected int maxProgress;
    protected boolean isActive;
    protected boolean hasNotEnoughEnergy;

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
                    hasNotEnoughEnergy = false;
                } else {
                    if (progress > 0)
                        progress--;
                    hasNotEnoughEnergy = true;
                }
            } else if (!getWorld().isRemote) {
                // Output
                output();
                this.strand = null;
                this.progress = 0;
                this.isActive = false;
            }
            if (!getWorld().isRemote) {
                this.markDirty();
            }
        }

        // Check if there is a resulting strand
        // Consume input strand if it exists
        if (!getWorld().isRemote && !isActive) {
            if (!hasRoom()) {
                return;
            }
            Strand possibleStrand = resultingStrand();
            if (getVoltage() == 0 ||(possibleStrand == null && outputsStrand()) || !consumeInputsAndSetupRecipe()) {
                return;
            }
            strand = possibleStrand;
            progress = 0;
            maxProgress /= (int) Math.pow(2, GTUtility.getTierByVoltage(getVoltage()) - 1);
            maxProgress = Math.max(1, maxProgress);
            isActive = true;
            this.markDirty();
        }
    }

    protected boolean outputsStrand() {
        return true;
    }

    protected boolean hasRoom() {
        return this.output.getStrand() == null;
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
            if (mat != null && mat.hasProperty(PropertyKey.INGOT)) {
                return stack;
            }
        }
        return null;
    }

    public long getVoltage() {
        return this.energyContainer.getInputVoltage();
    };

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
        this.outputFluidInventory = new FluidTankList(true, this.getAbilities(MultiblockAbility.EXPORT_FLUIDS));
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
        resetTileAbilities();
    }

    private void resetTileAbilities() {
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new GTItemStackHandler(this, 0);
        this.outputFluidInventory = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());

        this.input = null;
        this.output = null;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.progress);
        buf.writeInt(this.maxProgress);
        buf.writeBoolean(this.isActive);
        buf.writeCompoundTag(Strand.serialize(new NBTTagCompound(), strand));
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.progress = buf.readInt();
        this.maxProgress = buf.readInt();
        this.isActive = buf.readBoolean();
        try {
            this.strand = Strand.deserialize(buf.readCompoundTag());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("Progress", this.progress);
        data.setInteger("MaxProgress", this.maxProgress);
        data.setBoolean("IsActive", this.isActive);
        data.setTag("Strand", Strand.serialize(new NBTTagCompound(), this.strand));
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.progress = data.getInteger("Progress");
        this.maxProgress = data.getInteger("MaxProgress");
        this.isActive = data.getBoolean("IsActive");
        this.strand = Strand.deserialize(data.getCompoundTag("Strand"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        MultiblockDisplayText.builder(textList, this.isStructureFormed()).setWorkingStatus(true, isActive)
                .addEnergyUsageLine(this.energyContainer)
                .addEnergyTierLine(GTUtility.getTierByVoltage(this.energyContainer.getInputVoltage()))
                .addWorkingStatusLine().addProgressLine(this.getProgressPercent())
                .addLowPowerLine(hasNotEnoughEnergy)
                .addCustom((comps) -> {
                    Strand displayStrand = strand;
                    if (strand == null) {
                        if (output.getStrand() == null) {
                            comps.add(new TextComponentTranslation("gregtech.multiblock.strand_casting.no_strand"));
                            return;
                        }
                        comps.add(new TextComponentTranslation("gregtech.multiblock.strand_casting.output_detected"));
                        displayStrand = output.getStrand();
                    }
                    comps.add(new TextComponentTranslation("gregtech.multiblock.strand_casting.thickness", String.format("%.2f", displayStrand.thickness)));
                    comps.add(new TextComponentTranslation("gregtech.multiblock.strand_casting.width", String.format("%.2f", displayStrand.width)));

                    StrandConversion conversion = StrandConversion.getConversion(displayStrand);
                    if (conversion == null) {
                        comps.add(new TextComponentTranslation("gregtech.multiblock.strand_casting.no_conversion"));
                        return;
                    }
                    comps.add(new TextComponentTranslation("gregtech.multiblock.strand_casting.ore_prefix",
                            new TextComponentTranslation("supersymmetry.prefix." + conversion.prefix.name.toLowerCase())));
                });
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
        return isActive && this.isStructureFormed();
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        // They cannot stop it.
    }

    protected EnumFacing getRelativeFacing(RelativeDirection dir) {
        return dir.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler);
        if (checkEnergyIn) {
            predicate = predicate.or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1));
        }
        return predicate;
    }

    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true, false);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                isActive, true);
    }

    protected TraceabilityPredicate orientation(IBlockState state, RelativeDirection direction, IProperty<EnumFacing> facingProperty) {
        EnumFacing facing = getRelativeFacing(direction);

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(state.withProperty(facingProperty, facing))};
        return new TraceabilityPredicate(blockWorldState -> {
            if (blockWorldState.getBlockState() != state.withProperty(facingProperty, facing)) {
                if (blockWorldState.getBlockState().getBlock() != state.getBlock()) return false;
                getWorld().setBlockState(blockWorldState.getPos(), state.withProperty(facingProperty, facing));
            }
            return true;
        }, supplier);
    }

    protected TraceabilityPredicate axisOrientation(IBlockState state, RelativeDirection direction, IProperty<EnumFacing.Axis> facingProperty) {
        EnumFacing facing = getRelativeFacing(direction);
        EnumFacing.Axis axis = facing.getAxis();

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(state.withProperty(facingProperty, axis))};
        return new TraceabilityPredicate(blockWorldState -> {
            if (blockWorldState.getBlockState() != state.withProperty(facingProperty, axis)) {
                if (blockWorldState.getBlockState().getBlock() != state.getBlock()) return false;
                getWorld().setBlockState(blockWorldState.getPos(), state.withProperty(facingProperty, axis));
            }
            return true;
        }, supplier);
    }


    protected TraceabilityPredicate rollOrientation(RelativeDirection direction) {
        //makes sure rotor's front faces the left side (relative to the player) of controller front
        return axisOrientation(rollState(), direction, VariantAxialRotatableBlock.AXIS);
    }

    private IBlockState rollState() {
        return SuSyBlocks.METALLURGY_ROLL.getState(BlockMetallurgyRoll.BlockMetallurgyRollType.ROLL);
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }


}
