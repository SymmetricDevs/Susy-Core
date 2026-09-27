package supersymmetry.api.unification.material.properties;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;

public class SolidRocketFuelProperty implements IMaterialProperty, RocketFuelEntry {

    /**
     * @return the solid fuel a dust stack is made of, or null if the stack isn't a dust of a solid rocket fuel
     */
    public static @Nullable SolidRocketFuelProperty search(ItemStack stack) {
        UnificationEntry entry = OreDictUnifier.getUnificationEntry(stack);
        if (entry == null || entry.material == null || entry.orePrefix != OrePrefix.dust) {
            return null;
        }
        return entry.material.getProperty(SuSyPropertyKey.SOLID_ROCKET_FUEL);
    }

    private double mass; // Per dust, kg
    private double volume; // Per dust, L
    private double sIVacuum; // kg * m/s
    private double sIPerPressure;
    private Material material;

    public SolidRocketFuelProperty(double mass, double volume, double sIVacuum) {
        this(mass, volume, sIVacuum, 0);
    }

    public SolidRocketFuelProperty(double mass, double volume, double sIVacuum, double sIPerPressure) {
        this.mass = mass;
        this.volume = volume;
        this.sIVacuum = sIVacuum;
        this.sIPerPressure = sIPerPressure;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        // the only handle we get on the material we belong to, and the fuel key needs
        // its name
        this.material = properties.getMaterial();

        if (!properties.hasProperty(PropertyKey.DUST)) {
            throw new IllegalStateException("Material " + properties.getMaterial() +
                    " needs a dust property to be a solid rocket fuel!");
        }

        if (sIVacuum <= 0 || mass <= 0 || volume <= 0) {
            throw new IllegalStateException("Material " + properties.getMaterial() +
                    " should not have negative specific impulse, mass, or volume!");
        }
    }

    @Override
    public double getSpecificImpulse() {
        return sIVacuum;
    }

    public double getDensity() {
        return mass / volume;
    }

    /** Liters of tank one dust fills. */
    public double getVolume() {
        return volume;
    }

    @Override
    public String getFuelKey() {
        return SOLID_PREFIX + this.material.getRegistryName();
    }

    @Override
    public double getSIVariation() {
        return sIPerPressure;
    }
}
