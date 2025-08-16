package supersymmetry.mixins.gregtech;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

@Mixin(value = RecipeBuilder.class, remap = false)
public interface RecipeBuilderAccessor {

    @Accessor("onBuildAction")
    Consumer<RecipeBuilder<?>> getOnBuildAction();

    @Accessor("recipeMap")
    RecipeMap<?> getRecipeMap();
}
