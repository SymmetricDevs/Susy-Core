package supersymmetry.api.rocketry.fuels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.util.Tuple;

import gregtech.api.unification.material.Material;

public class RocketFuelEntry {

    public static class RocketFuelEntryBuilder {

        private double proportionTot = 0;

        private Material material;

        private ArrayList<Tuple<Material, Double>> sides;
        private String registryName;

        private double density;
        private double sIVacuum;

        private double sIPerPressure;

        public RocketFuelEntryBuilder(String name, Material primary, double primaryProportion) {
            this.registryName = name;
            this.sides = new ArrayList<>();
            this.material = primary;
            this.proportionTot = primaryProportion;
        }

        public RocketFuelEntryBuilder addComponent(Material mat, double proportion) {
            proportionTot += proportion;
            if (proportionTot > 1 + 1e-15) {
                throw new RuntimeException(String.format("total proportion over 1 [%s]", proportionTot));
            }
            this.sides.add(new Tuple<Material, Double>(mat, proportion));
            return this;
        }

        public RocketFuelEntryBuilder setCharacteristics(
                                                         double density, double sIVacuum, double sIPerPressure) {
            this.density = density;
            this.sIVacuum = sIVacuum;
            this.sIPerPressure = sIPerPressure;
            return this;
        }

        public void register() {
            if (this.sides.isEmpty()) {
                throw new RuntimeException("empty list of fuel component entries");
            }
            if (Math.abs(proportionTot - 1.0) > 1e-15) {
                throw new RuntimeException(String.format("incorrect total proportion", proportionTot));
            }
            RocketFuelEntry.registerFuel(
                    new RocketFuelEntry(
                            this.registryName,
                            this.material,
                            this.sides,
                            this.density,
                            this.sIVacuum,
                            this.sIPerPressure));
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

    private Material material; // the main fuel

    private ArrayList<Tuple<Material, Double>> sides; // any extra required materials, their proportions

    private String registryName;

    private double density; // kg/L

    private double sIVacuum; // kg * m / s

    private double sIPerPressure;

    public RocketFuelEntry(
                           String registryName,
                           Material material,
                           ArrayList<Tuple<Material, Double>> sides,
                           double density,
                           double sIVacuum,
                           double sIPerPressure) {
        this.registryName = registryName;
        this.material = material;
        this.sides = sides;
        this.density = density;
        this.sIVacuum = sIVacuum;
        this.sIPerPressure = sIPerPressure;
    }

    @SuppressWarnings("unchecked")
    public RocketFuelEntry(RocketFuelEntry copy) {
        this.material = copy.material;
        this.density = copy.density;
        this.sides = (ArrayList<Tuple<Material, Double>>) copy.sides.clone();
        this.sIVacuum = copy.sIVacuum;
        this.registryName = copy.registryName;
    }

    public double getsIVacuum() {
        return sIVacuum;
    }

    public double getsIPerPressure() {
        return sIPerPressure;
    }

    public ArrayList<Tuple<Material, Double>> getSides() {
        return sides;
    }

    public RocketFuelEntry clone() {
        return new RocketFuelEntry(this);
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

    @Override
    public String toString() {
        return String.format(
                "RocketFuelEntry[%s]{sIVacuum:%s,sIPerPressure:%s,density:%s,material:%s,sides:[%s]}",
                this.registryName,
                this.sIVacuum,
                this.sIPerPressure,
                this.density,
                this.material,
                this.sides.stream()
                        .map(
                                x -> {
                                    return String.format("[%s:%.2f%]", x.getFirst(), x.getSecond() * 100);
                                })
                        .collect(Collectors.toList())
                        .toString());
    }
}
