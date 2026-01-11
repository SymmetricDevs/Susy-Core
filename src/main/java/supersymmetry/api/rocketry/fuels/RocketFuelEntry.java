package supersymmetry.api.rocketry.fuels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.Tuple;

import gregtech.api.unification.material.Material;

public class RocketFuelEntry {

    public class RocketFuelEntryBuilder {

        private RocketFuelEntry rfe;
        private double proportionTot = 0;

        public RocketFuelEntryBuilder(String name) {
            this.rfe.registryName = name;
            this.rfe.composition = new ArrayList<>();
        }

        public void addComponent(Material mat, int proportion) throws Exception {
            proportionTot += proportion;
            if (proportionTot >= 1) {
                throw new Exception("total proportion over 1");
            }
            rfe.composition.add(new Tuple<Material, Integer>(mat, proportion));
        }

        public void density(double density) {
            rfe.density = density;
        }

        public void sIVacuum(double sIVacuum) {
            rfe.sIVacuum = sIVacuum;
        }

        public void sIPerPressure(double sIPerPressure) {
            rfe.sIPerPressure = sIPerPressure;
        }

        public void register() throws Exception {
            if (composition.isEmpty()) {
                throw new Exception("empty list of fuel component entries");
            }
            RocketFuelEntry.registerFuel(rfe);
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
