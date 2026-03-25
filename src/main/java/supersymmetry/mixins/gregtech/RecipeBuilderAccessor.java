package supersymmetry.mixins.gregtech;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;

@Mixin(value = RecipeBuilder.class, remap = false)
public interface RecipeBuilderAccessor {

    @Accessor("onBuildAction")
    Consumer<RecipeBuilder<?>> getOnBuildAction();

    @Accessor("recipeMap")
    RecipeMap<?> getRecipeMap();
}
