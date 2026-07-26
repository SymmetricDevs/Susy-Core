package supersymmetry.mixins.gregtech;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.PrimitiveRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import supersymmetry.common.util.RecipeCheckUtils;

@Mixin(PrimitiveRecipeLogic.class)
public abstract class PrimitiveRecipeLogicMixin extends AbstractRecipeLogic {

    public PrimitiveRecipeLogicMixin(RecipeMapPrimitiveMultiblockController tileEntity, RecipeMap<?> recipeMap) {
        super(tileEntity, recipeMap);
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        return super.checkRecipe(recipe) && RecipeCheckUtils.checkAtmosphere(this.metaTileEntity, true);
    }
}
