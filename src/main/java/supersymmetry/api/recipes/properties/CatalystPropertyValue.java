package supersymmetry.api.recipes.properties;

import org.jspecify.annotations.NonNull;

import supersymmetry.api.recipes.catalysts.CatalystGroup;

public class CatalystPropertyValue {

    private final int tier;
    private final CatalystGroup catalystGroup;

    public CatalystPropertyValue(int tier, @NonNull CatalystGroup catalystGroup) {
        this.tier = tier;
        this.catalystGroup = catalystGroup;
    }

    public int getTier() {
        return tier;
    }

    @NonNull
    public CatalystGroup getCatalystGroup() {
        return catalystGroup;
    }
}
