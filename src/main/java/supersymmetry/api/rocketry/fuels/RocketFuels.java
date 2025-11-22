package supersymmetry.api.rocketry.fuels;

import static supersymmetry.api.util.SuSyUtility.susyId;

import gregtech.api.unification.material.Material;

public class RocketFuels {

    public static Material LOX;

    public static void init() {
        initMaterials();
    }

    public static void initMaterials() {
        RocketFuels.LOX = new Material.Builder(24000, susyId("susy:liquid_oxygen")).color(0xDEF0FF).build();
    }
}
