package supersymmetry.api.recipes.properties;

import javax.annotation.Nonnull;

import supersymmetry.api.recipes.catalysts.CatalystGroup;

public class CatalystPropertyValue {

    private final int tier;
    private final CatalystGroup catalystGroup;

    public CatalystPropertyValue(int tier, @Nonnull CatalystGroup catalystGroup) {
        this.tier = tier;
        this.catalystGroup = catalystGroup;
    }

    public int getTier() {
        return tier;
    }

    @Nonnull
    public CatalystGroup getCatalystGroup() {
        return catalystGroup;
    }
}
