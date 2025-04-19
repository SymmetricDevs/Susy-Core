package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.*;

import java.util.function.Supplier;

public abstract class MetaTileEntityStrandMold extends MetaTileEntityStrandShaper {
    private static final FluidStack COOLANT = Materials.Water.getFluid(50);
    private static final FluidStack HOT_COOLANT = Materials.Steam.getFluid(50 * 960);

    public MetaTileEntityStrandMold(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public long getVoltage() {
        return energyContainer.getInputVoltage();
    }

    protected abstract int getRequiredMetal();
    protected abstract double getOutputThickness();
    protected abstract double getOutputWidth();

    @Override
    protected boolean consumeInputsAndSetupRecipe() {
        if (output.getStrand() != null) return false;
        FluidStack stack = getFirstMaterialFluid();
        if (stack == null || stack.amount < getRequiredMetal()) return false;
        stack = stack.copy();
        stack.amount = getRequiredMetal();
        this.inputFluidInventory.drain(stack, true);
        this.maxProgress = 40;
        return true;
    }


    @Override
    protected Strand resultingStrand() {
        FluidStack stack = getFirstMaterialFluid();
        if (stack == null || stack.amount < getRequiredMetal()) {
            return null;
        }
        Material mat = FluidUnifier.getMaterialFromFluid(stack.getFluid());
        if (mat == null || !mat.hasProperty(PropertyKey.INGOT)) {
            return null;
        }
        if (this.inputFluidInventory.drain(COOLANT, false).amount < 1000) {
            return null;
        }
        return new Strand(getOutputThickness(), getOutputWidth(), false, mat, stack.getFluid().getTemperature());
    }

    @Override
    public void output() {
        super.output();
        this.outputFluidInventory.fill(HOT_COOLANT, true);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.FLUID_SOLIDIFIER_OVERLAY;
    }

    protected IBlockState getPipeCasingState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.COPPER_PIPE);
    }


    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

}
