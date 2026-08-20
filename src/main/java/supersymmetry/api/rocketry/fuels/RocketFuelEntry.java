package supersymmetry.api.rocketry.fuels;

import org.jetbrains.annotations.Nullable;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import supersymmetry.api.unification.material.properties.SuSyPropertyKey;

public interface RocketFuelEntry {

    String LIQUID_PREFIX = "liquid:";
    String SOLID_PREFIX = "solid:";

    double getSpecificImpulse();

    double getDensity();

    double getSIVariation();

    /**
     * A name for this fuel that survives a save. The two kinds of fuel live in
     * different registries, so the key is tagged with which one to look in; resolve
     * it again with {@link #fromFuelKey}.
     */
    String getFuelKey();

    static @Nullable RocketFuelEntry fromFuelKey(String key) {
        if (key.startsWith(LIQUID_PREFIX)) {
            return LiquidRocketFuelEntry.getCopyOf(key.substring(LIQUID_PREFIX.length()));
        }
        if (key.startsWith(SOLID_PREFIX)) {
            // material registry names are themselves modid:path, so the rest of the key
            // gets handed over whole
            Material material = GregTechAPI.materialManager.getMaterial(key.substring(SOLID_PREFIX.length()));
            return material == null ? null : material.getProperty(SuSyPropertyKey.SOLID_ROCKET_FUEL);
        }
        return null;
    }
}
