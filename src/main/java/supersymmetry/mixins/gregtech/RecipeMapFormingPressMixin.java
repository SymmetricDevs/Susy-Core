package supersymmetry.mixins.gregtech;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.machines.RecipeMapFormingPress;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Deprecated
@ScheduledForRemoval(inVersion = "Next CEu update") // not needed after MUI migration
@Mixin(value = RecipeMapFormingPress.class)
public class RecipeMapFormingPressMixin extends RecipeMap<SimpleRecipeBuilder> {

    public RecipeMapFormingPressMixin(@NotNull String unlocalizedName, int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs, @NotNull SimpleRecipeBuilder defaultRecipeBuilder, boolean isHidden) {
        super(unlocalizedName, maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs, defaultRecipeBuilder, isHidden);
    }

    @Inject(method = "addSlot", at = @At("HEAD"), cancellable = true, remap = false)
    protected void addFluidSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isFluid, boolean isOutputs, CallbackInfo ci) {
        if (isFluid) {
            super.addSlot(builder, x, y, slotIndex, itemHandler, fluidHandler, true, isOutputs);
            ci.cancel();
        }
    }
}
