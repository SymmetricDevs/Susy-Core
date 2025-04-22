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
    private int maxInputs;
    private int maxOutputs;
    private int maxFluidInputs;
    private int maxFluidOutputs;

    @Shadow
    @Final
    private boolean modifyItemInputs;
    private boolean modifyItemOutputs;
    private boolean modifyFluidInputs;
    private boolean modifyFluidOutputs;

    @Shadow
    public abstract String getUnlocalizedName();

    @Unique
    @Override
    public void modifyMaxInputs(int maxInputs) {
        if (modifyItemInputs) {
            this.maxInputs = maxInputs;
        } else {
            throw new UnsupportedOperationException("Cannot change max item input amount for " + this.getUnlocalizedName());
        }
    }
    @Unique
    @Override
    public void modifyMaxOutputs(int maxOutputs) {
        if (modifyItemOutputs) {
            this.maxOutputs = maxOutputs;
        } else {
            throw new UnsupportedOperationException("Cannot change max item output amount for " + this.getUnlocalizedName());
        }
    }
    @Unique
    @Override
    public void modifyMaxFluidInputs(int maxFluidInputs) {
        if (modifyFluidInputs) {
            this.maxFluidInputs = maxFluidInputs;
        } else {
            throw new UnsupportedOperationException("Cannot change max fluid input amount for " + this.getUnlocalizedName());
        }
    }
    @Unique
    @Override
    public void modifyMaxFluidOutputs(int maxFluidOutputs) {
        if (modifyFluidOutputs) {
            this.maxFluidOutputs = maxFluidOutputs;
        } else {
            throw new UnsupportedOperationException("Cannot change max fluid output amount for " + this.getUnlocalizedName());
        }
    }
}

