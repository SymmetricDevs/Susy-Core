package supersymmetry.common.metatileentities.multi.rail;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;

public class LargeRESRecipeMap {

    public static final RecipeMap<SimpleRecipeBuilder> RES_RECIPES = new RecipeMap<>(
            "large_railroad_engineering_station",
            5000,
            1,
            5000,
            0,
            new SimpleRecipeBuilder(),
            false)
                    .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY_1)
                    .setSlotOverlay(true, false, GuiTextures.FURNACE_OVERLAY_2)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);
}
