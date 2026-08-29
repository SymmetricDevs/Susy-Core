package supersymmetry.api.space;

import static supersymmetry.api.rocketry.NozzleFlow.GAS_CONSTANT;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.tianmi.sussypatches.common.helper.DimDisplayRegistry;
import supersymmetry.SuSyValues;

public class Planetoid extends CelestialObject {

    private PlanetType planetType;
    private int dimension;
    /** Ambient pressure at the surface, in Pa. Zero, an airless body, unless said otherwise. */
    private double surfacePressure;
    private double atmosphereMolarMass;
    private double groundTemperature;
    private double atmosphereSpecificHeat;
    private double lowOrbitAltitude;
    /// the altitude where a body is considered to have reached orbit (if it has sufficient speed)
    private double rotationPeriod;
    public static BiMap<Planetoid, Integer> PLANETOIDS = HashBiMap.create();

    public Planetoid(String translationKey, double mass, double posT, double posX, double posY, double posZ,
                     @Nullable CelestialObject parentBody, PlanetType planetType) {
        super(translationKey, posT, posX, posY, posZ, mass, CelestialBodyType.PLANETOID, parentBody);
        this.planetType = planetType;
    }

    public PlanetType getPlanetType() {
        return planetType;
    }

    public void setPlanetType(PlanetType planetType) {
        this.planetType = planetType;
    }

    public Planetoid setDimension(int dimension) {
        this.dimension = dimension;
        // forcePut replaces any existing key→value or value→key mapping
        // instead of throwing when the value (dim id) is already present
        PLANETOIDS.forcePut(this, dimension);
        return this;
    }

    public int getDimension() {
        return dimension;
    }

    public Planetoid setAtmosphereStats(double surfacePressure, double atmosphereMolarMass,
                                        double groundTemperature, double atmosphereSpecificHeat) {
        this.surfacePressure = surfacePressure;
        this.atmosphereMolarMass = atmosphereMolarMass;
        this.groundTemperature = groundTemperature;
        this.atmosphereSpecificHeat = atmosphereSpecificHeat;
        return this;
    }

    // this can't easily be derived, since different bodies have different quirks (Earth has the atmosphere
    // which makes low orbits decay, Moon has mass concentrations that make low orbits unstable, etc.)
    public Planetoid setLowOrbitAltitude(double altitude) {
        this.lowOrbitAltitude = altitude;
        return this;
    }

    public Planetoid setRotationPeriod(double rotationPeriod) {
        this.rotationPeriod = rotationPeriod;
        return this;
    }

    public double getSurfacePressure() {
        return surfacePressure;
    }

    public double getAtmosphereMolarMass() {
        return atmosphereMolarMass;
    }

    public double getGroundTemperature() {
        return groundTemperature;
    }

    public double getAtmosphereSpecificHeat() {
        return atmosphereSpecificHeat;
    }

    public double getLowOrbitAltitude() {
        return lowOrbitAltitude;
    }

    public double getRotationPeriod() {
        return rotationPeriod;
    }

    public ItemStack getDisplayItem() {
        if (DimDisplayRegistry.getDisplayItem(this.dimension).isEmpty()) {
            return new ItemStack(Item.getItemById(this.dimension + 1));
        }
        return DimDisplayRegistry.getDisplayItem(this.dimension);
    }

    public double getPressureFromAltitude(double altitude) {
        if (this.surfacePressure == 0) {
            return 0;
        }
        // https://en.wikipedia.org/wiki/Atmospheric_pressure#Altitude_variation
        // FIXME: figure out whether this is correct for Mars/Venus/etc
        double pressure = this.getSurfacePressure() * Math.pow(1 + (SuSyValues.G0 * altitude) /
                (this.getAtmosphereSpecificHeat() * this.getGroundTemperature()),
                -this.getAtmosphereSpecificHeat() * this.getAtmosphereMolarMass() / GAS_CONSTANT);
        return (pressure > 1000 ? pressure : 0); // the formula doesn't work well for higher altitudes,
    }                                            // but pressure there is negligible anyway
}
