package supersymmetry.integration.jei;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.gui.GuiTextures;
import gregtech.integration.jei.basic.BasicRecipeCategory;
import gregtech.integration.jei.utils.render.FluidStackTextRenderer;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import supersymmetry.Supersymmetry;

public class RocketFuelCategory extends BasicRecipeCategory<RocketFuelWrapper, RocketFuelWrapper> {

    public static final String UID = Supersymmetry.MODID + ":rocket_fuel";

    private final FluidStack[] stacks = new FluidStack[3];
    protected final IDrawable[] slots = new IDrawable[3];

    public RocketFuelCategory(IGuiHelper guiHelper) {
        super("rocket_fuel", "rocket_fuel.name", guiHelper.createBlankDrawable(176, 90), guiHelper);
        for (int i = 0; i < slots.length; i++) {
            slots[i] = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18)
                    .setTextureSize(18, 18)
                    .build();
        }
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        for (int i = 0; i < slots.length; i++) {
            slots[i].draw(minecraft, 55 + i * 25, 9);
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RocketFuelWrapper recipeWrapper, IIngredients ingredients) {
        List<List<FluidStack>> copyFrom = ingredients.getInputs(VanillaTypes.FLUID);
        for (int i = 0; i < stacks.length && i < copyFrom.size(); i++) {
            stacks[i] = copyFrom.get(i).get(0);
        }

        IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
        for (int i = 0; i < stacks.length && i < copyFrom.size(); i++) {
            fluidStackGroup.init(i, true, 55 + i * 25, 9);
            FluidStack shifted = stacks[i].copy();
            shifted.amount = 1000; // Proper display

            fluidStackGroup.init(i, true,
                    new FluidStackTextRenderer(0, false,
                            16,
                            16, null),
                    56 + i * 25,
                    10,
                    16,
                    16, 0, 0);
            fluidStackGroup.set(i, stacks[i]);
        }
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RocketFuelWrapper recipe) {
        return recipe;
    }

    @Override
    public @NotNull String getModName() {
        return Supersymmetry.MODID;
    }
}
