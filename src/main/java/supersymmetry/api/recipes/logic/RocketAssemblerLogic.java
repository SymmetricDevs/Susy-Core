package supersymmetry.api.recipes.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import supersymmetry.common.metatileentities.multi.rocket.MetaTileEntityRocketAssembler;

public class RocketAssemblerLogic extends MultiblockRecipeLogic {

    public RocketAssemblerLogic(MetaTileEntityRocketAssembler assembler) {
        super(assembler);
    }

    @Override
    protected @Nullable Recipe findRecipe(
                                          long maxVoltage, IItemHandlerModifiable inputs,
                                          IMultipleTankHandler fluidInputs) {
        Recipe r = super.findRecipe(maxVoltage, inputs, fluidInputs);
        if (r != null) return r; // unlikely for this thing
        MetaTileEntityRocketAssembler assembler = (MetaTileEntityRocketAssembler) this.metaTileEntity;
        if (!assembler.isWorking) return null; // assume that it doesnt have a blueprint inside i guess
        var targetComponent = assembler.getCurrentCraftTarget();
        if (targetComponent == null) return null;
        List<GTRecipeInput> flatExpandedInput = targetComponent.materials.stream()
                .flatMap(
                        x -> {
                            return x.expandRecipe(RecipeMaps.ASSEMBLER_RECIPES, maxVoltage).stream();
                        })
                .collect(Collectors.toList());
        return assembler.recipeMap
                .recipeBuilder()
                .inputIngredients(collapse(flatExpandedInput))
                .EUt(2 << 15) // LuV amp. this means that you need 8 4A EV energy hatches :goog:
                .duration((int) Math.ceil(assembler.getCurrentCraftTarget().getAssemblyDuration() * 20))
                .build()
                .getResult();
    }

    private List<GTRecipeInput> collapse(List<GTRecipeInput> in) {
        List<GTRecipeInput> out = new ArrayList<>();
        for (int i = in.size(); i != 0; i--) {
            for (int j = i; j != 0; j--) {
                if (i != j) {
                    if (in.get(i).equalIgnoreAmount(in.get(j))) {
                        out.add(in.get(i).copyWithAmount(in.get(i).getAmount() + in.get(j).getAmount())); // this can
                                                                                                          // explode
                    }
                }
            }
        }
        in.stream()
                .filter(
                        x -> {
                            return out.stream()
                                    .noneMatch( // check if its already in the output list to add the ones with no
                                            // duplicates
                                            n -> {
                                                return n.equalIgnoreAmount(x);
                                            });
                        })
                .forEach(out::add);

        return out;
    }

    @Override
    protected void completeRecipe() {
        var assembler = (MetaTileEntityRocketAssembler) this.metaTileEntity;
        super.completeRecipe();
        assembler.nextComponent();
    }
}
