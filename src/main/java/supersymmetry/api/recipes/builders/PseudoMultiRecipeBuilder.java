package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import supersymmetry.api.recipes.properties.PseudoMultiProperty;
import supersymmetry.api.recipes.properties.PseudoMultiPropertyValues;

import java.util.ArrayList;

public class PseudoMultiRecipeBuilder extends RecipeBuilder<PseudoMultiRecipeBuilder>{
    public PseudoMultiRecipeBuilder() {
    }

    @SuppressWarnings("unused")
    public PseudoMultiRecipeBuilder(Recipe recipe, RecipeMap<PseudoMultiRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public PseudoMultiRecipeBuilder(RecipeBuilder<PseudoMultiRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    public PseudoMultiRecipeBuilder copy() {
        return new PseudoMultiRecipeBuilder(this);
    }

    public PseudoMultiRecipeBuilder blockStates(String blockGroupName, IBlockState... blockStates) {
        //apply property with acceptable blocks to recipe and return builder
        this.applyProperty(PseudoMultiProperty.getInstance(), new PseudoMultiPropertyValues(blockGroupName, blockStates));
        return this;
    }

    public PseudoMultiRecipeBuilder blockStates(String blockGroupName, BlockStateContainer blockStates) {
        ArrayList<IBlockState> blockStatesArrayList = new ArrayList<>(blockStates.getValidStates().size());
        for (IBlockState blockState : blockStates.getValidStates()) {
            blockStatesArrayList.add(blockState);
        }

        this.applyProperty(PseudoMultiProperty.getInstance(), new PseudoMultiPropertyValues(blockGroupName, blockStatesArrayList));
        return this;
    }

}
