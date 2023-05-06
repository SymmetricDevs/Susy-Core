package supersymmetry.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import supersymmetry.api.recipes.catalysts.CatalystGroup;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class SuSyRecipeCatalystInput extends GTRecipeOreInput {

    CatalystGroup catalystGroup;
    ItemStack[] inputStacks;
    int tier;

    public SuSyRecipeCatalystInput(CatalystGroup catalystGroup, int amount, int tier) {
        super("", amount);
        this.catalystGroup = catalystGroup;
        this.amount = amount;
        this.tier = tier;
    }

    public static GTRecipeInput getOrCreate(CatalystGroup catalystGroup, int tier, int amount){
        return getFromCache(new SuSyRecipeCatalystInput(catalystGroup, amount, tier));
    }

    public static GTRecipeInput getOrCreate(CatalystGroup catalystGroup, int tier) {
        return getOrCreate(new SuSyRecipeCatalystInput(catalystGroup, 1, tier));
    }

    public static GTRecipeInput getOrCreate(CatalystGroup catalystGroup) {
        return getOrCreate(new SuSyRecipeCatalystInput(catalystGroup, 1, 1));
    }

    static GTRecipeInput getFromCache(GTRecipeInput realIngredient) {
        GTRecipeInput cachedIngredient = INSTANCES.get(realIngredient);
        if (cachedIngredient == null) {
            cachedIngredient = realIngredient;
            INSTANCES.add(realIngredient);
        }

        return cachedIngredient;
    }

    @Override
    protected SuSyRecipeCatalystInput copy() {
        SuSyRecipeCatalystInput copy = new SuSyRecipeCatalystInput(this.catalystGroup, this.amount, this.tier);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    public GTRecipeInput copyWithAmount(int amount) {
        SuSyRecipeCatalystInput copy = new SuSyRecipeCatalystInput(this.catalystGroup, amount, this.tier);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    public ItemStack[] getInputStacks() {
        if (this.inputStacks == null) {
            this.inputStacks = this.catalystGroup.getCatalystInfos().getMap().entrySet().stream()
                    .filter(entry -> this.tier <= entry.getValue().getTier())
                    .map(Map.Entry::getKey)
                    .map(is -> {
                        is = is.copy();
                        is.setCount(this.amount);
                        return is;
                    }).toArray(ItemStack[]::new);
        }

        return this.inputStacks;
    }

    @Override
    public boolean isOreDict() {
        return false;
    }

    @Override
    public boolean acceptsStack(@Nullable ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        for (ItemStack target : getInputStacks()) {
            if (OreDictionary.itemMatches(target, input, false)) {
                return nbtMatcher == null || nbtMatcher.evaluate(input, nbtCondition);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, catalystGroup, tier, isConsumable, nbtMatcher, nbtCondition);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SuSyRecipeCatalystInput)) {
            return false;
        }
        SuSyRecipeCatalystInput other = (SuSyRecipeCatalystInput) obj;
        if (this.amount != other.amount) return false;
        if (this.isConsumable != other.isConsumable) return false;
        if (this.tier != other.tier) return false;
        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;
        return this.catalystGroup == other.catalystGroup;
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput obj) {
        if (this == obj) return true;
        if (!(obj instanceof SuSyRecipeCatalystInput)) {
            return false;
        }
        SuSyRecipeCatalystInput other = (SuSyRecipeCatalystInput) obj;
        if (this.tier != other.tier) return false;
        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;
        return this.catalystGroup == other.catalystGroup;
    }

    @Override
    public String toString() {
        //noinspection StringConcatenationMissingWhitespace
        return amount + "x" + catalystGroup.getName();
    }

}
