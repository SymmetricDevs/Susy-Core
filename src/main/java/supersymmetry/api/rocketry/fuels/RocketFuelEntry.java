package supersymmetry.api.rocketry.fuels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.Tuple;
import org.apache.commons.lang3.tuple.Triple;

import gregtech.api.unification.material.Material;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.integration.immersiverailroading.model.part.Rocket;

public class RocketFuelEntry {

    private Material material; // the main fuel
    private ArrayList<Tuple<Material, Double>> sides; // any extra required materials, their proportions
    private String registryName;
    private double density; // kg/L
    private double sIVacuum; // kg * m / s
    private double sIPerPressure;

    public RocketFuelEntry(String registryName, Material material, ArrayList<Tuple<Material, Double>> sides,
                           double density, double sIVacuum, double sIPerPressure) {
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
        FUEL_REGISTRY.put(rfe.registryName, rfe);
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

    public class RocketFuelEntryBuilder {
        private RocketFuelEntry rfe;
        private double proportionTot = 0;
        public RocketFuelEntryBuilder(String name) {
            this.rfe.registryName=name;
            this.rfe.sides = new ArrayList<>();
        }

        public void addComponent(Material mat, double proportion) throws Exception {
            proportionTot += proportion;
            if (proportionTot >= 1) {
                throw new Exception("total proportion over 1");
            }
            rfe.sides.add(new Tuple<Material, Double>(mat, proportion));
        }

        public void setCharacteristics(double density, double sIVacuum, double sIPerPressure) {
            rfe.density = density;
            rfe.sIVacuum = sIVacuum;
            rfe.sIPerPressure = sIPerPressure;
        }

        public void register() throws Exception {
            if (sides.isEmpty()) {
                throw new Exception("empty list of fuel component entries");
            }
            if (proportionTot != 1) {
                throw new Exception("incorrect total proportion");
            }
            RocketFuelEntry.registerFuel(rfe);
        }
    }
}
