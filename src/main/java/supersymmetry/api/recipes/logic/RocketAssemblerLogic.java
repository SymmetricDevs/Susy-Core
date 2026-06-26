package supersymmetry.api.recipes.logic;

import static gregtech.api.GTValues.LuV;
import static gregtech.api.GTValues.VA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.common.entities.EntityTransporterErector;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.item.behavior.ElectrodeDurabilityManager;
import supersymmetry.common.metatileentities.multi.rocket.MetaTileEntityRocketAssembler;

public class RocketAssemblerLogic extends MultiblockRecipeLogic {

    private List<Integer> electrodeSlotCache = new ArrayList<>();
    public boolean hasEnoughElectrodes = true;

    public RocketAssemblerLogic(MetaTileEntityRocketAssembler assembler) {
        super(assembler);
    }

    public void setInputsValid() {
        this.invalidInputsForRecipes = false;
    }

    public Recipe getRecipe(long maxVoltage) {
        MetaTileEntityRocketAssembler assembler = (MetaTileEntityRocketAssembler) this.metaTileEntity;
        if (!assembler.isAssemblyWorking) return null;

        AbstractComponent<?> targetComponent = assembler.getCurrentCraftTarget();
        if (targetComponent == null) return null;
        List<GTRecipeInput> flatExpandedInput = targetComponent.materials.stream()
                .flatMap(x -> x.expandRecipe().stream())
                .collect(Collectors.toList());
        Recipe recipe = assembler.recipeMap
                .recipeBuilder()
                .inputIngredients(collapse(flatExpandedInput))
                .EUt(VA[LuV]) // Almost 1 LuV amp
                .duration((int) Math.ceil(targetComponent.getAssemblyDuration() * 20))
                .build()
                .getResult();
        return recipe;
    }

    @Override
    protected @Nullable Recipe findRecipe(
                                          long maxVoltage, IItemHandlerModifiable inputs,
                                          IMultipleTankHandler fluidInputs) {
        MetaTileEntityRocketAssembler assembler = (MetaTileEntityRocketAssembler) this.metaTileEntity;
        EntityTransporterErector erector = assembler.findTransporterErector();
        if (erector == null) return null;
        return getRecipe(maxVoltage);
    }

    // mental illness n6: this runs when a recipe with nothing in it (findrecipe returns null) is
    // "complete" too!
    @Override
    protected void completeRecipe() {
        if (!(this.progressTime == 0 || this.maxProgressTime == 0)) {
            MetaTileEntityRocketAssembler assembler = (MetaTileEntityRocketAssembler) this.metaTileEntity;
            assembler.nextComponent();
        }
        super.completeRecipe();
    }

    // The lists are null
    @Override
    protected void outputRecipeOutputs() {}

    // Needs to be 2x the recipe EUt rather than 8x due to irregular energy hatch amperage draws
    @Override
    protected boolean hasEnoughPower(int @NotNull [] resultOverclock) {
        return getEnergyStored() >= ((long) recipeEUt << 1);
    }

    // doesnt work for this multi
    @Override
    protected boolean checkPreviousRecipe() {
        return false;
    }

    @Override
    protected void trySearchNewRecipe() {
        hasEnoughElectrodes = true;
        super.trySearchNewRecipe();
    }

    // mostly taken from the ball mill logic
    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        MetaTileEntityRocketAssembler assembler = (MetaTileEntityRocketAssembler) this.metaTileEntity;
        AbstractComponent<?> targetComponent = assembler.getCurrentCraftTarget();
        if (targetComponent == null) return false;
        int requiredDamage = getRequiredDamage(recipe, targetComponent);
        electrodeSlotCache.clear();
        int totalUses = 0;
        for (int i = 0; i < getInputInventory().getSlots(); i++) {
            ItemStack stack = getInputInventory().getStackInSlot(i);
            if (stack.isEmpty() || !SuSyMetaItems.TUNGSTEN_ELECTRODE.getStackForm().isItemEqual(stack)) {
                continue;
            }
            int remaining = ElectrodeDurabilityManager.getRemainingUses(stack);
            if (remaining > 0) {
                electrodeSlotCache.add(i);
                totalUses += remaining;
            }
        }
        if (totalUses < requiredDamage) {
            hasEnoughElectrodes = false;
            return false;
        }

        return super.checkRecipe(recipe);
    }

    // mostly taken from the ball mill logic
    @Override
    protected boolean setupAndConsumeRecipeInputs(
                                                  @NotNull Recipe recipe,
                                                  @NotNull IItemHandlerModifiable importInventory,
                                                  @NotNull IMultipleTankHandler importFluids) {
        if (!hasEnoughElectrodes || !super.setupAndConsumeRecipeInputs(recipe, importInventory, importFluids)) {
            return false;
        }
        MetaTileEntityRocketAssembler assembler = (MetaTileEntityRocketAssembler) this.metaTileEntity;
        AbstractComponent<?> targetComponent = assembler.getCurrentCraftTarget();
        if (targetComponent == null) return false;
        int requiredDamage = getRequiredDamage(recipe, targetComponent);
        for (int slot : electrodeSlotCache) {
            if (requiredDamage <= 0) break;
            ItemStack stack = importInventory.getStackInSlot(slot);
            if (stack.isEmpty() || !SuSyMetaItems.TUNGSTEN_ELECTRODE.getStackForm().isItemEqual(stack))
                continue;
            int canTake = Math.min(ElectrodeDurabilityManager.getRemainingUses(stack), requiredDamage);
            if (ElectrodeDurabilityManager.getRemainingUses(stack) == canTake) {
                importInventory.setStackInSlot(slot, ItemStack.EMPTY);
            } else {
                ElectrodeDurabilityManager.setElectrodeDamage(stack,
                        ElectrodeDurabilityManager.getElectrodeDamage(stack) + canTake);
            }
            requiredDamage -= canTake;
        }

        return true;
    }

    // maybe this is a little too much
    private static int getRequiredDamage(@NotNull Recipe recipe, @NotNull AbstractComponent<?> component) {
        return (int) ((double) recipe.getInputs().size() *
                (component.getAssemblyDuration() + component.getRadius()));
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
                            return new GTRecipeItemInput(x.getKey(), x.getValue());
                        })
                .collect(Collectors.toList());
    }
}
