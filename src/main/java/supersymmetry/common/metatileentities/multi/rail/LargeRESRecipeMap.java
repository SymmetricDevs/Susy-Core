package supersymmetry.common.metatileentities.multi.rail;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.core.sound.GTSoundEvents;

public class LargeRESRecipeMap {

    public static final RecipeMap<SimpleRecipeBuilder> RES_RECIPES = new RecipeMap<>(
            "large_railroad_engineering_station",
            25,
            1,
            3,
            0,
            new SimpleRecipeBuilder(),
            false)
                    .setSlotOverlay(false, false, GuiTextures.SLOT)
                    .setSlotOverlay(true, false, GuiTextures.SLOT)
                    .setSound(GTSoundEvents.ASSEMBLER)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL);
}
