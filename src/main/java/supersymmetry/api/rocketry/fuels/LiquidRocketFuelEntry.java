package supersymmetry.api.rocketry.fuels;

import java.util.*;

import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;

public class LiquidRocketFuelEntry implements RocketFuelEntry {

    public static class RocketFuelEntryBuilder {

        private String registryName;
        private ArrayList<Tuple<Fluid, Integer>> composition;
        private double density = 1;
        private double sIVacuum;
        private double sIPerPressure;

        public RocketFuelEntryBuilder(String name) {
            this.registryName = name;
            this.composition = new ArrayList<>();
        }

        public RocketFuelEntryBuilder addComponent(Fluid mat, int proportion) {
            composition.add(new Tuple<>(mat, proportion));
            if (composition.size() > 3) {
                throw new IllegalStateException("too many fuel components");
            }
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
            LiquidRocketFuelEntry
                    .registerFuel(new LiquidRocketFuelEntry(this.registryName, this.composition, this.density,
                            this.sIVacuum, this.sIPerPressure));
        }
    }

    private static Map<String, LiquidRocketFuelEntry> FUEL_REGISTRY = new HashMap<>();

    public static Map<String, LiquidRocketFuelEntry> getFuelRegistry() {
        return new HashMap<>(FUEL_REGISTRY);
    }

    public static LiquidRocketFuelEntry getCopyOf(String name) {
        if (name != null) {
            LiquidRocketFuelEntry entry = FUEL_REGISTRY.get(name);
            if (entry != null) {
                return entry.clone();
            }
        }
        return null;
    }

    public static void registerFuel(LiquidRocketFuelEntry rfe) {
        FUEL_REGISTRY.put(rfe.registryName, rfe);
    }

    private ArrayList<Tuple<Fluid, Integer>> composition; // any extra required materials, their proportions

    private String registryName;

    private double density; // kg/L

    private double sIVacuum; // kg * m / s

    private double sIPerPressure;

    public LiquidRocketFuelEntry(String registryName, ArrayList<Tuple<Fluid, Integer>> composition, double density,
                                 double sIVacuum, double sIPerPressure) {
        this.registryName = registryName;
        this.composition = composition;
        this.density = density;
        this.sIVacuum = sIVacuum;
        this.sIPerPressure = sIPerPressure;
    }

    @SuppressWarnings("unchecked")
    public LiquidRocketFuelEntry(LiquidRocketFuelEntry copy) {
        this.density = copy.density;
        this.composition = (ArrayList<Tuple<Fluid, Integer>>) copy.composition.clone();
        this.sIVacuum = copy.sIVacuum;
        this.registryName = copy.registryName;
    }

    public ArrayList<Tuple<Fluid, Integer>> getComposition() {
        return composition;
    }

    public LiquidRocketFuelEntry clone() {
        return new LiquidRocketFuelEntry(this);
    }

    public String getRegistryName() {
        return this.registryName;
    }

    @Override
    public String getFuelKey() {
        return LIQUID_PREFIX + this.registryName;
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

    public static @NotNull Optional<LiquidRocketFuelEntry> search(List<Fluid> userFluids) {
        for (LiquidRocketFuelEntry entry : LiquidRocketFuelEntry.getFuelRegistry().values()) {
            boolean matches = entry.getComposition().stream().map(Tuple::getFirst).allMatch(userFluids::contains);
            if (matches) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }
}
