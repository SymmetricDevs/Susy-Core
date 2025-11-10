package supersymmetry.api.rocketry.fuels;

import java.util.HashMap;
import java.util.Map;

import gregtech.api.unification.material.Material;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;

public class RocketFuelEntry {

    Map<Material, Double> material; // generally a mixture
    String registryName;
    double density; // kg/L
    double heatOfUse; // J/kg (technically incorrect)

    public RocketFuelEntry(String registryName, Map<Material, Double> material, double density, double heatOfUse) {
        this.registryName = registryName;
        this.material = material;
        this.density = density;
        this.heatOfUse = heatOfUse;
    }

    public RocketFuelEntry(RocketFuelEntry copy) {
        this.material = copy.material;
        this.density = copy.density;
        this.heatOfUse = copy.heatOfUse;
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

    public Map<Material, Double> getMixture() {
        return this.material;
    }

    public double getDensity() {
        return this.density;
    }

    public double getHeatOfUse() {
        return this.heatOfUse;
    }
}
