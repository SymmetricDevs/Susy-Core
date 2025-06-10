package supersymmetry.mixins.gregtech;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.AssemblerRecipeBuilder;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.loaders.recipes.handlers.RecyclingManager;

@Mixin(value = RecipeMaps.class, remap = false)
public abstract class RecipeMapsMixin {

    /// Note: in ceu 2.9 the method should be 'lambda$static$2'
    @WrapOperation(method = "lambda$static$1",
            at = @At(target = "Lgregtech/api/recipes/builders/AssemblerRecipeBuilder;isWithRecycling()Z",
                    value = "INVOKE"))
    private static boolean replaceWithOurs(AssemblerRecipeBuilder builder, Operation<Boolean> who_cares) {
        if (builder.isWithRecycling()) {
            ItemStack output = builder.getOutputs().get(0);
            RecyclingManager.addRecycling(output, output.getCount(), builder.getInputs());
        }
        return false;
    }
}
