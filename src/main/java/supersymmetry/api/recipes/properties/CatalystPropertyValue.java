package supersymmetry.api.recipes.properties;

import supersymmetry.api.recipes.catalysts.CatalystGroup;

public class CatalystPropertyValue {

    private int tier;
    private CatalystGroup catalystGroup;

    public CatalystPropertyValue(int tier, CatalystGroup catalystGroup) {
        this.tier = tier;
        this.catalystGroup = catalystGroup;
    }

    public int getTier() {
        return tier;
    }

    public CatalystGroup getCatalystGroup() {
        return catalystGroup;
    }
}
