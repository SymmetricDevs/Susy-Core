package supersymmetry.common.metatileentities.multi.electric;

import gregicality.multiblocks.common.block.GCYMMetaBlocks;
import gregicality.multiblocks.common.block.blocks.BlockLargeMultiblockCasing;
import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
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
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityMetallurgicalConverter extends RecipeMapMultiblockController {

    public MetaTileEntityMetallurgicalConverter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.METALLURGICAL_CONVERTER);
        this.recipeMapWorkable = new MetallurgicalConverterLogic(this);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.RIGHT)
                .aisle(" F   F ", " F   F ", " F   F ", " F   F ", " FF FF ", "  FAF  ", "  AGA  ", "   A   ", "       ", "       ")
                .aisle("HHHHHH ", "HFFFFF ", "H ###  ", "  ###  ", "  BBB  ", "  VVV  ", "  BBB  ", "  VVV  ", "  BBB  ", "       ")
                .aisle(" HHHHHH", " H###FH", " ##### ", " #BBB# ", " BRRRB ", " VRRRV ", " BRRRB ", " VRRRV ", " BRRRB ", "  BBB  ")
                .aisle("  HHHHH", "  #P#FS", " ##P## ", " #BBB# ", " BRRRB ", " VR#RV ", " BR#RB ", " VR#RV ", " BR#RB ", "  BMB  ")
                .aisle(" HHHHHH", " H###FH", " ##### ", " #BBB# ", " BRRRB ", " VRRRV ", " BRRRB ", " VRRRV ", " BRRRB ", "  BBB  ")
                .aisle("HHHHHH ", "HFFFFF ", "H ###  ", "  ###  ", "  BBB  ", "  VVV  ", "  BBB  ", "  VVV  ", "  BBB  ", "       ")
                .aisle(" F   F ", " F   F ", " F   F ", " F   F ", " FF FF ", "  FAF  ", "  AGA  ", "   A   ", "       ", "       ")
                .where('#', air())
                .where('S', selfPredicate())
                .where('R', states(getRefractoryState()))
                .where('H', states(getCasingState()).setMinGlobalLimited(31).or(autoAbilities(true, true, true, true, true, true, false)))
                .where('A', states(getCasingState()))
                .where('B', states(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.STRESS_PROOF_CASING)))
                .where('F', frames(Materials.Steel))
                .where('V', states(GCYMMetaBlocks.UNIQUE_CASING.getState(BlockUniqueCasing.UniqueCasingType.HEAT_VENT)))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where(' ', any())
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

    private IBlockState getRefractoryState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.ADVANCED_REFRACTORY_LINING);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMetallurgicalConverter(metaTileEntityId);
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.METALLURGICAL_CONVERTER_OVERLAY;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.parallel_pure", 64));
    }

    private class MetallurgicalConverterLogic extends MultiblockRecipeLogic {
        public MetallurgicalConverterLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public int getParallelLimit() {
            return 64;
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
