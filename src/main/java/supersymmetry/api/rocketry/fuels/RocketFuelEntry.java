package supersymmetry.api.rocketry.fuels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;

import gregtech.api.unification.material.Material;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;

public class RocketFuelEntry {

    private Material material; // the main fuel
    private ArrayList<Triple<Material, Double, Double>> sides; // any extra required materials, their proportions, and their
                                                       // densities
    private String registryName;
    private double density; // kg/L
    private double sIVacuum; // kg * m / s
    private double sIPerPressure;

    public RocketFuelEntry(String registryName, Material material, ArrayList<Triple<Material, Double, Double>> sides,
                           double density, double sIVacuum, double sIPerPressure) {
        this.registryName = registryName;
        this.material = material;
        this.sides = sides;
        this.density = density;
        this.sIVacuum = sIVacuum;
        this.sIPerPressure = sIPerPressure;
    }

    public RocketFuelEntry(RocketFuelEntry copy) {
        this.material = copy.material;
        this.density = copy.density;
        this.sides = (ArrayList<Triple<Material, Double, Double>>) copy.sides.clone();
        this.sIVacuum = copy.sIVacuum;
        this.registryName = copy.registryName;
    }

    private static Map<String, RocketFuelEntry> FUEL_REGISTRY = new HashMap<>();

    public static Map<String, RocketFuelEntry> getFuelRegistry() {
        return new HashMap<>(FUEL_REGISTRY);
    }

    public static RocketFuelEntry getCopyOf(String name) {
        try {
            return RocketFuelEntry.getFuelRegistry().get(name).clone();
        } catch (Exception e) {
            return null;
        }
    }

    public RocketFuelEntry clone() {
        return new RocketFuelEntry(this);
    }

    public static void registerFuel(RocketFuelEntry rfe) {
        if (!getRegistryLock()) {
            FUEL_REGISTRY.put(rfe.registryName, rfe);
        }
    }

    public static boolean registryLock = false;

    public static boolean getRegistryLock() {
        return registryLock;
    }

    public static void setRegistryLock(boolean registryLock) {
        AbstractRocketBlueprint.registryLock = registryLock;
    }

    public String getRegistryName() {
        return this.registryName;
    }

    public Material getMaterial() {
        return this.material;
    }

    public double getDensity() {
        return this.density;
    }

    public double getSpecificImpulse() {
        return this.sIVacuum;
    }

    public double getSIVariation() {
        return this.sIPerPressure;
    }
}
