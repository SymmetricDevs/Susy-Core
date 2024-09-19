package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import supersymmetry.api.recipes.properties.LatexCollectorMultiProperty;
import supersymmetry.api.recipes.properties.LatexCollectorMultiPropertyValues;

import java.util.ArrayList;

public class LatexCollectorMultiRecipeBuilder extends RecipeBuilder<LatexCollectorMultiRecipeBuilder>{
    public LatexCollectorMultiRecipeBuilder() {
    }

    @SuppressWarnings("unused")
    public LatexCollectorMultiRecipeBuilder(Recipe recipe, RecipeMap<LatexCollectorMultiRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public LatexCollectorMultiRecipeBuilder(RecipeBuilder<LatexCollectorMultiRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    public LatexCollectorMultiRecipeBuilder copy() {
        return new LatexCollectorMultiRecipeBuilder(this);
    }

    public LatexCollectorMultiRecipeBuilder blockStates(String blockGroupName, IBlockState... blockStates) {
        //apply property with acceptable blocks to recipe and return builder
        this.applyProperty(LatexCollectorMultiProperty.getInstance(), new LatexCollectorMultiPropertyValues(blockGroupName, blockStates));
        return this;
    }

    public LatexCollectorMultiRecipeBuilder blockStates(String blockGroupName, BlockStateContainer... blockStates) {
        ArrayList<IBlockState> blockStatesArrayList = new ArrayList<>(blockStates.length * 16);
        for (BlockStateContainer blockStateContainer : blockStates) {

            blockStatesArrayList.addAll(blockStateContainer.getValidStates());
        }

        this.applyProperty(LatexCollectorMultiProperty.getInstance(), new LatexCollectorMultiPropertyValues(blockGroupName, blockStatesArrayList));
        return this;
    }



}
