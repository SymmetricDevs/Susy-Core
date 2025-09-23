package supersymmetry.common.recipes;

import supersymmetry.api.recipes.catalysts.CatalystGroup;

public final class CatalystGroups {

    public static final CatalystGroup OXIDATION_CATALYST_BEDS = new CatalystGroup("oxidation_catalyst_beds");
    public static final CatalystGroup REDUCTION_CATALYST_BEDS = new CatalystGroup("reduction_catalyst_beds");
    public static final CatalystGroup CRACKING_CATALYST_BEDS = new CatalystGroup("cracking_catalyst_beds");
    // I don't think this has any use, we may as well yeet it
    // Keeping it in for now though
    public static final CatalystGroup STANDARD_CATALYSTS = new CatalystGroup("standard_catalysts");

    private CatalystGroups() {}
}
