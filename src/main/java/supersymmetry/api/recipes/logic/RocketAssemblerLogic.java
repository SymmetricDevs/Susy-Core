package supersymmetry.api.recipes.logic;

import static gregtech.api.GTValues.LuV;
import static gregtech.api.GTValues.VA;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import supersymmetry.api.metatileentity.multiblock.IRocketAssemblyController;
import supersymmetry.api.rocketry.AssemblyStep;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.item.behavior.ElectrodeDurabilityManager;

public class RocketAssemblerLogic extends MultiblockRecipeLogic {

    private static int getRequiredDamage(@NotNull Recipe recipe, @NotNull AssemblyStep step) {
        return (int) ((double) recipe.getInputs().size() * step.getElectrodeDamageFactor());
    }

    private List<Integer> electrodeSlotCache = new ArrayList<>();

    public boolean hasEnoughElectrodes = true;

    public final IRocketAssemblyController assembler;

    private final int assemblyEUt;
    private final boolean usesElectrodes;

    public <T extends RecipeMapMultiblockController & IRocketAssemblyController> RocketAssemblerLogic(T assembler) {
        this(assembler, VA[LuV], true);
    }

    public <T extends RecipeMapMultiblockController & IRocketAssemblyController> RocketAssemblerLogic(T assembler,
                                                                                                      int assemblyEUt) {
        this(assembler, assemblyEUt, true);
    }

    public <T extends RecipeMapMultiblockController & IRocketAssemblyController> RocketAssemblerLogic(T assembler,
                                                                                                      int assemblyEUt,
                                                                                                      boolean usesElectrodes) {
        super(assembler);
        this.assembler = assembler;
        this.assemblyEUt = assemblyEUt;
        this.usesElectrodes = usesElectrodes;
    }

    public void setInputsValid() {
        this.invalidInputsForRecipes = false;
    }

    public Recipe getCurrentRecipe() {
        return getRecipe();
    }

    public Recipe getRecipe() {
        if (!assembler.isAssemblyWorking())
            return null;

        if (assembler.getComponentIndex() >= assembler.getComponentCount())
            return null;

        AssemblyStep step = assembler.getCurrentStep();
        if (step == null)
            return null;
        Recipe recipe = getRecipeMap().recipeBuilder().inputIngredients(collapse(step.getRecipeInputs()))
                .EUt(assemblyEUt).duration((int) Math.ceil(step.getAssemblyDuration())).build().getResult();
        return recipe;
    }

    @Override
    public void updateWorkable() {
        super.updateWorkable();
        World world = getMetaTileEntity().getWorld();
        if (world != null && !world.isRemote) {
            if (workingEnabled && progressTime == 0) {
                // check the assembler to see if it can finish
                if (assembler.isAssemblyWorking() && assembler.getComponentIndex() == assembler.getComponentCount()) {
                    if (assembler.isAssemblySiteReady()) {
                        assembler.finishAssembly();
                    }
                }
            }
        }
    }

    // mostly taken from the ball mill logic
    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        if (assembler.getComponentIndex() >= assembler.getComponentCount())
            return false;

        AssemblyStep targetComponent = assembler.getCurrentStep();
        if (targetComponent == null)
            return false;
        if (!usesElectrodes) {
            hasEnoughElectrodes = true;
            return assembler.isAssemblySiteReady() && super.checkRecipe(recipe);
        }
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

        return assembler.isAssemblySiteReady() && super.checkRecipe(recipe);
    }

    @Override
    protected @Nullable Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs,
                                          IMultipleTankHandler fluidInputs) {
        if (!assembler.isAssemblySiteAvailable())
            return null;
        return getRecipe();
    }

    // mental illness n6: this runs when a recipe with nothing in it (findrecipe
    // returns null) is "complete" too!
    @Override
    protected void completeRecipe() {
        if (!(this.progressTime == 0 || this.maxProgressTime == 0)) {
            assembler.nextComponent();
        }
        super.completeRecipe();
    }

    // The lists are null
    @Override
    protected void outputRecipeOutputs() {}

    // Needs to be 2x the recipe EUt rather than 8x due to irregular energy hatch
    // amperage draws
    @Override
    protected boolean hasEnoughPower(int @NotNull [] resultOverclock) {
        return getEnergyStored() >= ((long) recipeEUt * 2);
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
    protected boolean setupAndConsumeRecipeInputs(@NotNull Recipe recipe,
                                                  @NotNull IItemHandlerModifiable importInventory,
                                                  @NotNull IMultipleTankHandler importFluids) {
        if (usesElectrodes && !hasEnoughElectrodes) {
            return false;
        }
        if (!super.setupAndConsumeRecipeInputs(recipe, importInventory, importFluids)) {
            return false;
        }
        if (!usesElectrodes) {
            return true;
        }
        AssemblyStep targetComponent = assembler.getCurrentStep();
        if (targetComponent == null)
            return false;
        int requiredDamage = getRequiredDamage(recipe, targetComponent);
        for (int slot : electrodeSlotCache) {
            if (requiredDamage <= 0)
                break;
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

    @Override
    protected void setupRecipe(Recipe recipe) {
        super.setupRecipe(recipe);

        assembler.onComponentSetup();
    }

    protected List<GTRecipeInput> collapse(List<GTRecipeInput> in) {
        List<GTRecipeInput> out = new ArrayList<>();
        for (GTRecipeInput input : in) {
            int match = indexOfMatch(out, input);
            if (match < 0) {
                out.add(input);
                continue;
            }
            GTRecipeInput existing = out.get(match);
            out.set(match, existing.copyWithAmount(existing.getAmount() + input.getAmount()));
        }
        return out;
    }

    private static int indexOfMatch(List<GTRecipeInput> out, GTRecipeInput input) {
        for (int i = 0; i < out.size(); i++) {
            if (out.get(i).equalIgnoreAmount(input)) {
                return i;
            }
        }
        return -1;
    }
}
