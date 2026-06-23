package supersymmetry.mixins.gregtech;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.recipes.RecipeMap;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredientRenderer;
import supersymmetry.api.recipes.SuSyRecipeMaps;

@Mixin(value = RecipeMapCategory.class, remap = false)
public class RecipeMapCategoryMixin {

    @Final
    @Shadow
    private RecipeMap<?> recipeMap;

    @Unique
    private static final int SUSY$MAX_COLUMNS = 4;
    @Unique
    private static final int SUSY$SLOT_SPACING = 18;

    @Unique
    private int susy$outputBaseX = -1;
    @Unique
    private int susy$outputBaseY = -1;
    @Unique
    private int susy$outputSlotCount = 0;

    @Inject(method = "setRecipe*", at = @At("HEAD"), remap = false)
    private void susy$resetOutputState(CallbackInfo ci) {
        susy$outputSlotCount = 0;
    }

    /*
     * Reflows item output slots for the ore sorter recipes that would overflow
     * the JEI panel into a MAX_COLUMNS wide grid anchored at the first output
     * slot's position. Input slots are passed through unchanged.
     *
     * This is done here rather than in a JEI mixin to avoid creating possible
     * bugs for other recipes.
     */
    @Redirect(
              method = "setRecipe*",
              at = @At(
                       value = "INVOKE",
                       target = "Lmezz/jei/api/gui/IGuiItemStackGroup;init(IZLmezz/jei/api/ingredients/IIngredientRenderer;IIIIII)V"),
              remap = false)
    private void susy$fixOutputSlotPositions(IGuiItemStackGroup group, int slotIndex, boolean input,
                                             IIngredientRenderer<?> renderer, int x, int y, int width, int height,
                                             int xPadding, int yPadding) {
        if (!input && recipeMap == SuSyRecipeMaps.ORE_SORTER_RECIPES) {
            if (susy$outputSlotCount == 0) {
                susy$outputBaseX = x;
                susy$outputBaseY = y;
            }
            int col = susy$outputSlotCount % SUSY$MAX_COLUMNS;
            int row = susy$outputSlotCount / SUSY$MAX_COLUMNS;
            x = susy$outputBaseX + col * SUSY$SLOT_SPACING;
            y = susy$outputBaseY + row * SUSY$SLOT_SPACING;
            susy$outputSlotCount++;
        }

        group.init(slotIndex, input, (IIngredientRenderer<ItemStack>) renderer, x, y, width, height, xPadding,
                yPadding);
    }
}
