package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;

public class ParticleSourceRecipeBuilder extends RecipeBuilder<ParticleSourceRecipeBuilder> {

    public ParticleSourceRecipeBuilder() {}

    @SuppressWarnings("unused")
    public ParticleSourceRecipeBuilder(Recipe recipe, RecipeMap<ParticleSourceRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public ParticleSourceRecipeBuilder(RecipeBuilder<ParticleSourceRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }
}
