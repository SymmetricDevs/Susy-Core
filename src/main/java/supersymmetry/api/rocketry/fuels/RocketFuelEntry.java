package supersymmetry.api.rocketry.fuels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.Tuple;

import gregtech.api.unification.material.Material;

public class RocketFuelEntry {

    public static class RocketFuelEntryBuilder {

        private String registryName;
        private ArrayList<Tuple<Material, Integer>> composition;
        private double density;
        private double sIVacuum;
        private double sIPerPressure;

        public RocketFuelEntryBuilder(String name) {
            this.registryName = name;
            this.composition = new ArrayList<>();
        }

        public RocketFuelEntryBuilder addComponent(Material mat, int proportion) {
            composition.add(new Tuple<>(mat, proportion));
            return this;
        }

        public RocketFuelEntryBuilder density(double density) {
            this.density = density;
            return this;
        }

        public RocketFuelEntryBuilder sIVacuum(double sIVacuum) {
            this.sIVacuum = sIVacuum;
            return this;
        }

        public RocketFuelEntryBuilder sIPerPressure(double sIPerPressure) {
            this.sIPerPressure = sIPerPressure;
            return this;
        }

        public void register() {
            if (this.composition.isEmpty()) {
                throw new IllegalStateException("empty list of fuel component entries");
            }
            RocketFuelEntry.registerFuel(new RocketFuelEntry(this.registryName, this.composition, this.density,
                    this.sIVacuum, this.sIPerPressure));
        }
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

    public static void registerFuel(RocketFuelEntry rfe) {
        FUEL_REGISTRY.put(rfe.registryName, rfe);
    }

    private ArrayList<Tuple<Material, Integer>> composition; // any extra required materials, their proportions

    private String registryName;

    private double density; // kg/L

    private double sIVacuum; // kg * m / s

    private double sIPerPressure;

    public RocketFuelEntry(
                           String registryName,
                           ArrayList<Tuple<Material, Integer>> composition,
                           double density,
                           double sIVacuum,
                           double sIPerPressure) {
        this.registryName = registryName;
        this.composition = composition;
        this.density = density;
        this.sIVacuum = sIVacuum;
        this.sIPerPressure = sIPerPressure;
    }

    @SuppressWarnings("unchecked")
    public RocketFuelEntry(RocketFuelEntry copy) {
        this.density = copy.density;
        this.composition = (ArrayList<Tuple<Material, Integer>>) copy.composition.clone();
        this.sIVacuum = copy.sIVacuum;
        this.registryName = copy.registryName;
    }

    public double getsIVacuum() {
        return sIVacuum;
    }

    public double getsIPerPressure() {
        return sIPerPressure;
    }

    public ArrayList<Tuple<Material, Integer>> getComposition() {
        return composition;
    }

    public RocketFuelEntry clone() {
        return new RocketFuelEntry(this);
    }

    public String getRegistryName() {
        return this.registryName;
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
