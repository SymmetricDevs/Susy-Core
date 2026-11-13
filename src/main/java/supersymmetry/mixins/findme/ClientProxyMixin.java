package supersymmetry.mixins.findme;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.hjae.findme.proxy.ClientProxy;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import mezz.jei.gui.recipes.RecipesGui;

/**
 * FindMe asks JEI what is under the mouse only when the open screen is a GuiContainer. JEI's recipe view is a
 * plain screen, so the fluid search key did nothing on a fluid shown in a recipe. Allow the lookup on the recipe
 * view too, for fluids only: items there are already found through their tooltip, and asking JEI as well would
 * send the search twice.
 */
@Mixin(value = ClientProxy.class, remap = false)
public class ClientProxyMixin {

    @WrapOperation(method = "keyPress",
                   at = @At(value = "CONSTANT",
                            args = "classValue=net/minecraft/client/gui/inventory/GuiContainer"))
    private boolean susy$searchFluidsInRecipeView(Object screen, Operation<Boolean> original,
                                                 @Local(ordinal = 1) boolean fluids) {
        return original.call(screen) || (fluids && screen instanceof RecipesGui);
    }
}
