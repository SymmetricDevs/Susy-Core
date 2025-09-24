package supersymmetry.mixins.gregtech;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.loaders.recipe.RecyclingRecipes;
import supersymmetry.loaders.recipes.handlers.RecyclingManager;

@Mixin(value = RecyclingRecipes.class, remap = false)
public abstract class RecyclingRecipesMixin {

    @Inject(method = "init", at = @At("HEAD"))
    private static void initUnificationInfo(CallbackInfo ci) {
        RecyclingManager.init();
    }

    @Redirect(method = { "registerMaceratorRecycling", "registerArcRecycling" },
              at = @At(target = "Lgregtech/api/recipes/RecipeBuilder;buildAndRegister()V",
                       value = "INVOKE",
                       ordinal = 0))
    private static void registerRecyclingRecipe(RecipeBuilder<?> builder, @Local(argsOnly = true) ItemStack input) {
        RecyclingManager.buildAndRegister(builder, input);
    }
}
