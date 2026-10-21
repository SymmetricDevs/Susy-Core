package supersymmetry.mixins.gregtech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import supersymmetry.api.gui.MachineGuiLayout;

@Mixin(value = SimpleMachineMetaTileEntity.class, remap = false)
public abstract class SimpleMachineMetaTileEntityGuiMixin {

    @ModifyExpressionValue(method = "createGuiTemplate",
                           at = @At(value = "INVOKE",
                                    target = "Lgregtech/api/recipes/RecipeMap;createUITemplate(Ljava/util/function/DoubleSupplier;Lnet/minecraftforge/items/IItemHandlerModifiable;Lnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/impl/FluidTankList;Lgregtech/api/capability/impl/FluidTankList;I)Lgregtech/api/gui/ModularUI$Builder;"),
                           remap = false)
    private ModularUI.Builder susy$growMachineBackground(ModularUI.Builder builder,
                                                         @Local(index = 2) RecipeMap<?> recipeMap) {
        if ("circuit_assembler".equals(recipeMap.unlocalizedName) ||
                "crystallizer".equals(recipeMap.unlocalizedName)) {
            int baseYOffset = recipeMap.getMaxInputs() >= 6 || recipeMap.getMaxFluidInputs() >= 6 ||
                    recipeMap.getMaxOutputs() >= 6 || recipeMap.getMaxFluidOutputs() >= 6 ? 9 : 0;
            int controlsShift = MachineGuiLayout.controlsYShift(
                    recipeMap.getMaxInputs(), recipeMap.getMaxFluidInputs(), baseYOffset,
                    MachineGuiLayout.fluidColumns(recipeMap.unlocalizedName, recipeMap.getMaxFluidInputs()));
            ((ModularUIBuilderAccessor) builder).susy$setHeight(166 + baseYOffset + controlsShift);
        }
        return builder;
    }

    @ModifyArg(method = "createGuiTemplate",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/api/recipes/RecipeMap;createUITemplate(Ljava/util/function/DoubleSupplier;Lnet/minecraftforge/items/IItemHandlerModifiable;Lnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/impl/FluidTankList;Lgregtech/api/capability/impl/FluidTankList;I)Lgregtech/api/gui/ModularUI$Builder;"),
               index = 5,
               remap = false)
    private int susy$addSpaceAboveTargetMachineSlots(int yOffset, @Local(index = 2) RecipeMap<?> recipeMap) {
        if ("circuit_assembler".equals(recipeMap.unlocalizedName) ||
                "crystallizer".equals(recipeMap.unlocalizedName)) {
            return yOffset + MachineGuiLayout.recipeYShift(
                    recipeMap.getMaxInputs(), recipeMap.getMaxFluidInputs(), yOffset);
        }
        return yOffset;
    }

    @ModifyVariable(method = "createGuiTemplate",
                    at = @At(value = "INVOKE",
                             target = "Lgregtech/api/recipes/RecipeMap;createUITemplate(Ljava/util/function/DoubleSupplier;Lnet/minecraftforge/items/IItemHandlerModifiable;Lnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/impl/FluidTankList;Lgregtech/api/capability/impl/FluidTankList;I)Lgregtech/api/gui/ModularUI$Builder;",
                             shift = At.Shift.AFTER),
                    ordinal = 0,
                    remap = false)
    private int susy$moveCircuitAssemblerControls(int yOffset, @Local(index = 2) RecipeMap<?> recipeMap) {
        if ("circuit_assembler".equals(recipeMap.unlocalizedName) ||
                "crystallizer".equals(recipeMap.unlocalizedName)) {
            return yOffset + MachineGuiLayout.controlsYShift(
                    recipeMap.getMaxInputs(), recipeMap.getMaxFluidInputs(), yOffset,
                    MachineGuiLayout.fluidColumns(recipeMap.unlocalizedName, recipeMap.getMaxFluidInputs()));
        }
        return yOffset;
    }
}
