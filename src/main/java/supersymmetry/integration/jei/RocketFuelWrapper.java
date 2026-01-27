package supersymmetry.integration.jei;

import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;

public class RocketFuelWrapper implements IRecipeWrapper {

    private final RocketFuelEntry entry;

    private final String specificImpulse;

    public RocketFuelWrapper(RocketFuelEntry entry) {
        this.entry = entry;
        this.specificImpulse = I18n.format("susy.tooltip.specific_impulse", entry.getSpecificImpulse());
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.FLUID, entry.getComposition().stream()
                .map((t) -> t.getFirst().getFluid(t.getSecond()))
                .collect(Collectors.toList()));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        int start = 40;
        minecraft.fontRenderer.drawString(specificImpulse, 0, start, 0x111111);
    }
}
