package supersymmetry.mixins.gregtech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import gregtech.api.gui.ModularUI;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.recipes.RecipeMap;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.gui.MachineGuiLayout;

import java.util.function.DoubleSupplier;

@Mixin(value = RecipeMap.class, remap = false)
public abstract class RecipeMapCircuitAssemblerGuiMixin {

    @ModifyArgs(method = "addInventorySlotGroup",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/api/recipes/RecipeMap;addSlot(Lgregtech/api/gui/ModularUI$Builder;IIILnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/impl/FluidTankList;ZZ)V"),
               remap = false)
    private void susy$placeMachineSlots(Args args, ModularUI.Builder builder,
                                        IItemHandlerModifiable itemHandler,
                                        FluidTankList fluidHandler, boolean isOutputs,
                                        int yOffset) {
        RecipeMap<?> recipeMap = (RecipeMap<?>) (Object) this;
        if ("crystallizer".equals(recipeMap.unlocalizedName) && !isOutputs &&
                (Boolean) args.get(6) && fluidHandler.getTanks() == 3) {
            int slotIndex = (Integer) args.get(3);
            args.set(1, MachineGuiLayout.fluidLeftOfItemsX(itemHandler.getSlots()) +
                    (slotIndex % 2) * 18);
            args.set(2, (Integer) args.get(2) + (slotIndex / 2) * 18);
            return;
        }
        if ("circuit_assembler".equals(recipeMap.unlocalizedName) && !isOutputs) {
            int slotIndex = (Integer) args.get(3);
            boolean isFluid = (Boolean) args.get(6);
            if (isFluid && fluidHandler.getTanks() == 4) {
                // GregTech wraps the fourth tank because it uses i % 3 and i / 3.
                // Keep all four tanks on the row below the item inputs instead.
                args.set(1, 6 + slotIndex * 18);
                args.set(2, (Integer) args.get(2) - (slotIndex / 3) * 18);
            } else {
                args.set(1, (Integer) args.get(1) +
                        MachineGuiLayout.inputXShift(itemHandler.getSlots(), fluidHandler.getTanks()));
            }
        }
    }

    @ModifyArg(method = "createUITemplate",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/api/gui/ModularUI;defaultBuilder(I)Lgregtech/api/gui/ModularUI$Builder;"),
               index = 0,
               remap = false)
    private int susy$extendCircuitAssemblerBackground(int yOffset, DoubleSupplier progressSupplier,
                                                       IItemHandlerModifiable importItems,
                                                       IItemHandlerModifiable exportItems,
                                                       FluidTankList importFluids,
                                                       FluidTankList exportFluids, int recipeYOffset) {
        RecipeMap<?> recipeMap = (RecipeMap<?>) (Object) this;
        if ("circuit_assembler".equals(recipeMap.unlocalizedName) ||
                "crystallizer".equals(recipeMap.unlocalizedName)) {
            int baseYOffset = recipeMap.getMaxInputs() >= 6 || recipeMap.getMaxFluidInputs() >= 6 ||
                    recipeMap.getMaxOutputs() >= 6 || recipeMap.getMaxFluidOutputs() >= 6 ? 9 : 0;
            int controlsShift = MachineGuiLayout.controlsYShift(
                    importItems.getSlots(), importFluids.getTanks(), baseYOffset,
                    MachineGuiLayout.fluidColumns(recipeMap.unlocalizedName, importFluids.getTanks()));
            if (controlsShift > 0) {
                return baseYOffset + controlsShift;
            }
        }
        return yOffset;
    }
}
