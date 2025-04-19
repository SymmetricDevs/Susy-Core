package supersymmetry.mixins.gregtech;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import org.spongepowered.asm.mixin.*;

@Mixin(RecipeMap.class)
public abstract class RecipeMapMixin<R extends RecipeBuilder<R>> {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    @Shadow
    private int maxOutputs;

    @Shadow
    @Final
    private boolean modifyItemOutputs;

    @Shadow
    public abstract String getUnlocalizedName();

    @Unique
    //@Override
    public void modifyMaxOutputs(int maxOutputs) {
        if (modifyItemOutputs) {
            this.maxOutputs = maxOutputs;
        } else {
            throw new UnsupportedOperationException("Cannot change max item output amount for " + this.getUnlocalizedName());
        }
    }
}

