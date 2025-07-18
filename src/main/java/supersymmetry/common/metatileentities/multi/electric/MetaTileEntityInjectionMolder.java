package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.logic.SuSyParallelLogic;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityInjectionMolder extends RecipeMapMultiblockController {

    public MetaTileEntityInjectionMolder(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.INJECTION_MOLDER);
        this.recipeMapWorkable = new InjectionMolderLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityInjectionMolder(metaTileEntityId);
    }


    @Override
    protected @NotNull BlockPattern createStructurePattern() {

        TraceabilityPredicate casingPredicate = states(getCasingState()).setMinGlobalLimited(35);

        return FactoryBlockPattern.start()
                .aisle("CCCCCC", "CCCCCC", "III   ")
                .aisle("CCCCCC", "IPPKGO", "I#ICCC")
                .aisle("CCCCCC", "CSCCCC", "III   ")
                /*.aisle("CCC", "CKC", " C ")
                .aisle("CCC", "CGC", " C ")
                .aisle("CCC", "COC", " C ") */
                .where('S', selfPredicate())
                .where('C', casingPredicate.or(autoAbilities(true, true, false, false, false, false, false)))
                .where('K', states(MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.CUPRONICKEL)))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('P', states(getPipeCasingState()))
                .where('I', casingPredicate.or(autoAbilities(false, false, true,false, false, false, false)))
                .where('O', casingPredicate.or(autoAbilities(false, false, false,true, false, false, false)))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    protected EnumFacing getRelativeFacing(RelativeDirection dir) {
        return dir.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.INJECTION_MOLDER_OVERLAY;
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.parallel_pure", 16));
    }

    private class InjectionMolderLogic extends MultiblockRecipeLogic {
        public InjectionMolderLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public int getParallelLimit() {
            return 16;
        }

        @Override
        protected long getMaxParallelVoltage() {
            return 2147432767L;
        }

        @Override
        public Recipe findParallelRecipe(@NotNull Recipe currentRecipe, @NotNull IItemHandlerModifiable inputs, @NotNull IMultipleTankHandler fluidInputs, @NotNull IItemHandlerModifiable outputs, @NotNull IMultipleTankHandler fluidOutputs, long maxVoltage, int parallelLimit) {
            if (parallelLimit > 1 && this.getRecipeMap() != null) {
                RecipeBuilder<?> parallelBuilder;
                parallelBuilder = SuSyParallelLogic.pureParallelRecipe(currentRecipe, this.getRecipeMap(), inputs, fluidInputs, outputs, fluidOutputs, parallelLimit, maxVoltage, this.getMetaTileEntity());

                if (parallelBuilder == null) {
                    this.invalidateInputs();
                    return null;
                } else if (parallelBuilder.getParallel() == 0) {
                    this.invalidateOutputs();
                    return null;
                } else {
                    this.setParallelRecipesPerformed(parallelBuilder.getParallel());
                    this.applyParallelBonus(parallelBuilder);
                    return parallelBuilder.build().getResult();
                }
            } else {
                return currentRecipe;
            }
        }

    }
}
