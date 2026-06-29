package supersymmetry.integration.jei.category;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.gui.GuiTextures;
import gregtech.integration.jei.basic.BasicRecipeCategory;
import gregtech.integration.jei.utils.render.FluidStackTextRenderer;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import supersymmetry.api.capability.StrandConversion;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

public class StrandCategory extends BasicRecipeCategory<StrandInfo, StrandInfo> {

    private final IDrawable icon;
    protected final IDrawable slot;
    private final IDrawable arrow;
    private int slotNumber;

    public StrandCategory(IGuiHelper guiHelper) {
        super("strand_casting", "strand_casting.name", guiHelper.createBlankDrawable(176, 90), guiHelper);
        this.icon = guiHelper.createDrawableIngredient(SuSyMetaTileEntities.TURNING_ZONE.getStackForm());
        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18)
                .build();
        this.arrow = guiHelper.drawableBuilder(GuiTextures.PROGRESS_BAR_ARROW.imageLocation, 0, 20, 20, 20)
                .setTextureSize(20, 40).build();
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, StrandInfo recipeWrapper,
                          @NotNull IIngredients ingredients) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();

        fluidStackGroup.init(0, true, new FluidStackTextRenderer(recipeWrapper.fluidStack.amount, false, 18, 18, null),
                11, 30, 18, 18, 0, 0);
        fluidStackGroup.set(0, recipeWrapper.fluidStack);

        slotNumber = recipeWrapper.conversions.size();
        for (int i = 0; i < slotNumber; i++) {
            itemStackGroup.init(i, false, 80 + (i % 5) * 18, 20 + (i / 5) * 18);
            itemStackGroup.set(i, recipeWrapper.conversions.get(i));
        }
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {
        slot.draw(minecraft, 10, 29);

        for (int i = 0; i < StrandConversion.CONVERSIONS.size(); i++) {
            slot.draw(minecraft, 80 + (i % 5) * 18, 20 + (i / 5) * 18);
        }
        arrow.draw(minecraft, 50, 27);
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(StrandInfo recipe) {
        return recipe;
    }
}
