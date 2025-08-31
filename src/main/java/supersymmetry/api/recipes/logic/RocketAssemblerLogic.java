package supersymmetry.api.recipes.logic;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;
import supersymmetry.common.metatileentities.multi.rocket.MetaTileEntityRocketAssembler;

public class RocketAssemblerLogic extends MultiblockRecipeLogic {
  public RocketAssemblerLogic(MetaTileEntityRocketAssembler assembler) {
    super(assembler);
  }

  @Override
  protected @Nullable Recipe findRecipe(
      long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
    Recipe r = super.findRecipe(maxVoltage, inputs, fluidInputs);
    if (r != null) return r; // unlikely for this thing
    var assembler = (MetaTileEntityRocketAssembler) this.metaTileEntity;
    if (!assembler.isWorking) return null; // assume that it doesnt have a blueprint inside i guess
    var input =
        assembler.getCurrentCraftTarget().materials.stream()
            .flatMap(
                x -> {
                  return x.expandRecipe(RecipeMaps.ASSEMBLER_RECIPES, maxVoltage).stream();
                })
            .collect(Collectors.toList());
    return assembler
        .recipeMap
        .recipeBuilder()
        .inputIngredients(collapse(input))
        .EUt(4 << 8)
        .duration((int) Math.ceil(assembler.getCurrentCraftTarget().getAssemblyDuration()))
        .build()
        .getResult(); // .collect(Collectors.toList());
  }

  private List<GTRecipeInput> collapse(List<GTRecipeInput> in) {
    List<GTRecipeInput> out = new ArrayList<>();
    for (int i = in.size(); i != 0; i--) {
      for (int j = i; j != 0; j--) {
        if (i != j) {
          if (in.get(i).equalIgnoreAmount(in.get(j))) {
            out.add(in.get(i).copyWithAmount(in.get(i).getAmount() + in.get(j).getAmount()));
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
}
