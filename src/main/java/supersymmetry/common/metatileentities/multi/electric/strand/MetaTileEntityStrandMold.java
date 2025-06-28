package supersymmetry.common.metatileentities.multi.electric.strand;

import gregicality.multiblocks.api.fluids.GCYMFluidStorageKeys;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.List;

public abstract class MetaTileEntityStrandMold extends MetaTileEntityStrandShaper {
    private static final FluidStack COOLANT = Materials.Water.getFluid(50);
    private static final FluidStack HOT_COOLANT = Materials.Steam.getFluid(50 * 960);

    public MetaTileEntityStrandMold(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    protected abstract int getRequiredMetal();
    protected abstract double getOutputThickness();
    protected abstract double getOutputWidth();

    @Override
    protected boolean consumeInputsAndSetupRecipe() {
        FluidStack stack = getFirstMaterialFluid();
        if (stack == null || stack.amount < getRequiredMetal()) return false;
        stack = stack.copy();
        stack.amount = getRequiredMetal();
        this.inputFluidInventory.drain(stack, true);
        this.inputFluidInventory.drain(COOLANT, true);
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
        if (mat == null || !mat.hasProperty(PropertyKey.INGOT) || !mat.hasFlag(SuSyMaterialFlags.CONTINUOUSLY_CAST)) {
            return null;
        }
        if (mat.getFluid(GCYMFluidStorageKeys.MOLTEN) != null) {
            if (stack.getFluid() != mat.getFluid(GCYMFluidStorageKeys.MOLTEN)) {
                return null;
            }
        } else if (mat.getFluid(FluidStorageKeys.LIQUID) != null) {
            if (stack.getFluid() != mat.getFluid(FluidStorageKeys.LIQUID)) {
                return null;
            }
        }
        FluidStack fluidStack = this.inputFluidInventory.drain(COOLANT, false);
        if (fluidStack == null || fluidStack.amount < 50) {
            return null;
        }
        return new Strand(getOutputThickness(), getOutputWidth(), false, mat, stack.getFluid().getTemperature());
    }

    @Override
    public void output() {
        super.output();
        this.outputFluidInventory.fill(HOT_COOLANT, true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart part) {
        if (part instanceof IMultiblockAbilityPart<?> abilityPart) {
            MultiblockAbility<?> ability = abilityPart.getAbility();
            if (ability == MultiblockAbility.IMPORT_FLUIDS
                    || ability == MultiblockAbility.EXPORT_FLUIDS
                    || ability == SuSyMultiblockAbilities.STRAND_EXPORT) {
                return SusyTextures.CONDUCTIVE_COPPER_PIPE;
            }
        }
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

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.strand_mold.tooltip"));
    }
}
