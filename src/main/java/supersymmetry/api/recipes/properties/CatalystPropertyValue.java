package supersymmetry.api.recipes.properties;

import supersymmetry.api.recipes.catalysts.CatalystGroup;

import javax.annotation.Nonnull;

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
