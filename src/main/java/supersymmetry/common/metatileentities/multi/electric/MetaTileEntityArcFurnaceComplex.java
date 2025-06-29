package supersymmetry.common.metatileentities.multi.electric;

import gregicality.multiblocks.api.render.GCYMTextures;
import gregicality.multiblocks.common.block.GCYMMetaBlocks;
import gregicality.multiblocks.common.block.blocks.BlockLargeMultiblockCasing;
import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.logic.SuSyParallelLogic;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockElectrodeAssembly;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityArcFurnaceComplex extends MetaTileEntityAdvancedArcFurnace {
    public MetaTileEntityArcFurnaceComplex(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.recipeMapWorkable = new ArcFurnaceComplexLogic(this);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityArcFurnaceComplex(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("               ", "  AAA     AAA  ", "  AAA     AAA  ", "  EEE     EEE  ", "  AAA     AAA  ", "  EEE     EEE  ", "  AAA     AAA  ", "               ", "               ", "               ", "               ", "               ")
                .aisle("  AAA     AAA  ", " AAAAA   AAAAA ", " ABBBA   ABBBA ", " EBBBE   EBBBE ", " A###A   A###A ", " E###E   E###E ", " A###A   A###A ", "  AAA     AAA  ", "               ", "               ", "  AAA     AAA  ", "               ")
                .aisle(" AAAAA   AAAAA ", "AABBBAA AABBBAA", "AB###BA AB###BA", "EB###BE EB###BE", "A#C#C#A A#C#C#A", "E#C#C#E E#C#C#E", "A#C#C#A A#C#C#A", " ACACA   ACACA ", "  C C     C C  ", "  C C     C C  ", " ACACA   ACACA ", "  C C     C C  ")
                .aisle(" AAAAA   AAAAA ", "AABBBAA AABBBAA", "AB###BA AB###BA", "EB###BE EB###BE", "A#####A A#####A", "E#####E E#####E", "A#####A A#####A", " AAAAA   AAAAA ", "               ", "               ", " AAAAA   AAAAA ", "               ")
                .aisle(" AAAAA   AAAAA ", "AABBBAA AABBBAA", "AB###BA AB###BA", "EB###BE EB###BE", "A##C##A A##C##A", "E##C##E E##C##E", "A##C##A A##C##A", " AACAA   AACAA ", "   C       C   ", "   C       C   ", "  ACA     ACA  ", "   C       C   ")
                .aisle("  AAA     AAA  ", " AAAAA   AAAAA ", " ABBBA   ABBBA ", " EBBBE   EBBBE ", " ABBBA   ABBBA ", " E###E   E###E ", " A###A   A###A ", "  AAA     AAA  ", "               ", "               ", "  A A     A A  ", "               ")
                .aisle("      HHH      ", "  AAA FHF AAA  ", "  AAA  F  AAA  ", "  EEE     EEE  ", "  AAA     AAA  ", "  EEE     EEE  ", "  AAA     AAA  ", "  FFF     FFF  ", "  FFF     FFF  ", "  FFF     FFF  ", "  DDD     DDD  ", "               ")
                .aisle("      HHH      ", "      HHH      ", "      FFF      ", "       F       ", "       F       ", "       F       ", "       F       ", "  FFFFFFFFFFF  ", "               ", "               ", "               ", "               ")
                .aisle("      HHH      ", "  AAA FSF AAA  ", "  AAA  F  AAA  ", "  EEE     EEE  ", "  AAA     AAA  ", "  EEE     EEE  ", "  AAA     AAA  ", "  FFF     FFF  ", "  FFF     FFF  ", "  FFF     FFF  ", "  DDD     DDD  ", "               ")
                .aisle("  AAA     AAA  ", " AAAAA   AAAAA ", " ABBBA   ABBBA ", " EBBBE   EBBBE ", " A###A   A###A ", " E###E   E###E ", " A###A   A###A ", "  AAA     AAA  ", "               ", "               ", "  A A     A A  ", "               ")
                .aisle(" AAAAA   AAAAA ", "AABBBAA AABBBAA", "AB###BA AB###BA", "EB###BE EB###BE", "A##C##A A##C##A", "E##C##E E##C##E", "A##C##A A##C##A", " AACAA   AACAA ", "   C       C   ", "   C       C   ", " AACAA   AACAA ", "   C       C   ")
                .aisle(" AAAAA   AAAAA ", "AABBBAA AABBBAA", "AB###BA AB###BA", "EB###BE EB###BE", "A#####A A#####A", "E#####E E#####E", "A#####A A#####A", " AAAAA   AAAAA ", "               ", "               ", " AAAAA   AAAAA ", "               ")
                .aisle(" AAAAA   AAAAA ", "AABBBAA AABBBAA", "AB###BA AB###BA", "EB###BE EB###BE", "A#C#C#A A#C#C#A", "E#C#C#E E#C#C#E", "A#C#C#A A#C#C#A", " ACACA   ACACA ", "  C C     C C  ", "  C C     C C  ", " ACACA   ACACA ", "  C C     C C  ")
                .aisle("  AAA     AAA  ", " AAAAA   AAAAA ", " ABBBA   ABBBA ", " EBBBE   EBBBE ", " ABBBA   ABBBA ", " E###E   E###E ", " A###A   A###A ", "  AAA     AAA  ", "               ", "               ", "  AAA     AAA  ", "               ")
                .aisle("               ", "  AAA     AAA  ", "  AAA     AAA  ", "  EEE     EEE  ", "  AAA     AAA  ", "  EEE     EEE  ", "  AAA     AAA  ", "               ", "               ", "               ", "               ", "               ")
                .where('S', selfPredicate())
                .where('A', states(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.STRESS_PROOF_CASING)))
                .where('B', states(SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.ADVANCED_REFRACTORY_LINING)))
                .where('C', states(SuSyBlocks.ELECTRODE_ASSEMBLY.getState(BlockElectrodeAssembly.ElectrodeAssemblyType.CARBON)))
                .where('D', states(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.STRESS_PROOF_CASING)).setMinGlobalLimited(8).or(autoAbilities(true, false, false, false, false, false, false)))
                .where('E', states(GCYMMetaBlocks.UNIQUE_CASING.getState(BlockUniqueCasing.UniqueCasingType.HEAT_VENT)))
                .where('F', frames(Materials.Steel))
                .where('H', states(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.STRESS_PROOF_CASING)).setMinGlobalLimited(6).or(autoAbilities(false, true, true, true, true, true, false)))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return GCYMTextures.STRESS_PROOF_CASING;
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.parallel_pure", 256));
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.ARC_FURNACE_OVERLAY;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    private class ArcFurnaceComplexLogic extends MultiblockRecipeLogic {
        public ArcFurnaceComplexLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public int getParallelLimit() {
            return 256;
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
