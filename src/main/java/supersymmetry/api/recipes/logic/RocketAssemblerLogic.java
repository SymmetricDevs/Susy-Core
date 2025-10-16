package supersymmetry.api.recipes.logic;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.components.AbstractComponent;
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
        AbstractComponent<?> targetComponent = assembler.getCurrentCraftTarget();
        if (targetComponent == null) return null;
        List<GTRecipeInput> flatExpandedInput = targetComponent.materials.stream()
                .flatMap(
                        x -> {
                            return x.expandRecipe(RecipeMaps.ASSEMBLER_RECIPES, maxVoltage).stream();
                        })
                .collect(Collectors.toList());
        Recipe recipe = assembler.recipeMap
                .recipeBuilder()
                .inputIngredients(collapse(flatExpandedInput))
                .EUt(2 << 15) // LuV amp. this means that you need 8 4A EV energy hatches :goog:
                .duration((int) Math.ceil(targetComponent.getAssemblyDuration() * 20))
                .build()
                .getResult();
        // for (GTRecipeInput input : recipe.getInputs()) {
        // SusyLog.logger.info(
        // "amount {} item {}",
        // input.getAmount(),
        // Stream.of(input.getInputStacks())
        // .map(x -> String.format("item:%s meta:%s", x.getDisplayName(),
        // x.getMetadata()))
        // .collect(Collectors.toList()));
        // }

        return recipe;
    }

    public Recipe getComponentRecipe() {
        MetaTileEntityRocketAssembler assembler = (MetaTileEntityRocketAssembler) this.metaTileEntity;
        if (!assembler.isWorking) return null; // assume that it doesnt have a blueprint inside i guess
        AbstractComponent<?> targetComponent = assembler.getCurrentCraftTarget();
        if (targetComponent == null) return null;
        List<GTRecipeInput> flatExpandedInput = targetComponent.materials.stream()
                .flatMap(
                        x -> {
                            return x.expandRecipe(RecipeMaps.ASSEMBLER_RECIPES, 2 << 15).stream();
                        })
                .collect(Collectors.toList());
        return assembler.recipeMap
                .recipeBuilder()
                .inputIngredients(collapse(flatExpandedInput))
                .EUt(2 << 15) // LuV amp. this means that you need 8 4A EV energy hatches :goog:
                .duration((int) Math.ceil(targetComponent.getAssemblyDuration() * 20))
                .build()
                .getResult();
    }

    private List<GTRecipeInput> collapse(List<GTRecipeInput> in) {
        HashMap<ItemStack, Integer> counts = new HashMap<>();
        for (GTRecipeInput input : in) {
            for (var stack : input.getInputStacks()) {
                if (counts.containsKey(stack)) {
                    counts.put(stack, counts.get(stack) + stack.getCount());
                } else {
                    counts.put(stack, stack.getCount());
                }
            }
        }
        return counts.entrySet().stream()
                .map(
                        x -> {
                            // x.getKey().setCount(x.getValue());
                            // return x.getKey();
                            return new GTRecipeItemInput(x.getKey(), x.getValue());
                        })
                .collect(Collectors.toList());

        // List<GTRecipeInput> out = new ArrayList<>();
        // for (int i = in.size() - 1; i != 0; i--) {
        // for (int j = i; j != 0; j--) {
        // if (i != j) {
        // if (in.get(i).equalIgnoreAmount(in.get(j))) {
        // out.add(
        // in.get(i)
        // .copyWithAmount(
        // in.get(i).getAmount() + in.get(j).getAmount())); // this can and will
        // // explode, ill change it later:tm:
        // }
        // }
        // }
        // }
        // in.stream()
        // .filter(
        // x -> {
        // return out.stream()
        // .noneMatch( // check if its already in the output list to add the ones with no
        // // duplicates
        // n -> {
        // return n.equalIgnoreAmount(x);
        // });
        // })
        // .forEach(out::add);
        // return out;
    }

    // mental illness n6: this runs when a recipe with nothing in it (findrecipe returns null) is
    // "complete" too!
    @Override
    protected void completeRecipe() {
        SusyLog.logger.info(
                "progressTime:{} maxprogresstime:{}", this.progressTime, this.maxProgressTime);
        if (!(this.progressTime == 0 || this.maxProgressTime == 0)) {
            MetaTileEntityRocketAssembler assembler = (MetaTileEntityRocketAssembler) this.metaTileEntity;
            assembler.nextComponent();
        }
        super.completeRecipe();
    }
}
