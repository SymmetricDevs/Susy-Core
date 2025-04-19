package supersymmetry.mixins.gregtech;

import gregtech.api.recipes.RecipeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import supersymmetry.api.capability.RecipeMapExtension;

@Mixin(value = RecipeMap.class, remap = false)
public abstract class RecipeMapMixin implements RecipeMapExtension {

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

