package supersymmetry.mixins.gregtech;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.recipes.ModHandler;
import supersymmetry.loaders.recipes.handlers.RecyclingManager;

@Mixin(value = ModHandler.class, remap = false)
public abstract class ModHandlerMixin {

    @Inject(method = "addShapedRecipe(ZLjava/lang/String;Lnet/minecraft/item/ItemStack;ZZ[Ljava/lang/Object;)V",
            at = @At(target = "Lnet/minecraft/item/ItemStack;getCount()I",
                     value = "INVOKE",
                     remap = true),
            cancellable = true)
    private static void replaceWithOurs(boolean i, String dont, ItemStack result, boolean care, boolean about,
                                        Object[] recipe, CallbackInfo ci) {
        RecyclingManager.addRecycling(result, result.getCount(), recipe);
        ci.cancel();
    }
}
