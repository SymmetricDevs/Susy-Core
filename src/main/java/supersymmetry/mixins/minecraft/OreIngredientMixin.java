package supersymmetry.mixins.minecraft;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import supersymmetry.common.item.ItemToolbelt;

/**
 * Lets a toolbelt satisfy an ore-dictionary ingredient with a tool it contains.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0).
 */
@Mixin(OreIngredient.class)
public abstract class OreIngredientMixin {

    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void susy$toolbeltIngredient(ItemStack input, CallbackInfoReturnable<Boolean> cir) {
        if (ItemToolbelt.checkIngredientAgainstToolbelt(input, (OreIngredient) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
