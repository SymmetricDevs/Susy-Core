package supersymmetry.api.util;

import net.minecraft.util.ResourceLocation;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;

import supersymmetry.Supersymmetry;
import supersymmetry.common.materials.SusyMaterials;

import java.util.HashMap;
import java.util.function.Function;
import java.util.Map;

public class SuSyUtility {

    /*
     * if we assume a solar boiler to have an approximately 1m^2 "collecting" surface, and take the hp to be less
     * inefficient,
     * one can approximate the J to EU conversion via 18L/t -> 9EU/t -> 180EU/s -> 1000J/180EU ~= 6J in one EU.
     * Assuming solar actually gets slightly less sunlight than 1m^2 conversion may be ~= 5, but potential
     * inefficiencies
     * make it unclear the specific amount. Just going to round to one sig fig and leave it at 10J -> 1EU
     */
    public static final int JOULES_PER_EU = 10;

    public static final Function<Integer, Integer> reactorTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 12000;
        if (tier == GTValues.MV)
            return 16000;
        if (tier == GTValues.HV)
            return 20000;
        if (tier == GTValues.EV)
            return 36000;
        // IV+
        return 64000;
    };

    public static final Function<Integer, Integer> collectorTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 16000;
        if (tier == GTValues.MV)
            return 24000;
        if (tier == GTValues.HV)
            return 32000;
        // EV+
        return 64000;
    };

    public static final Function<Integer, Integer> bulkTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 12000;
        if (tier == GTValues.MV)
            return 18000;
        if (tier == GTValues.HV)
            return 24000;
        if (tier == GTValues.EV)
            return 48000;
        // IV+
        return 64000;
    };

    public static class Lubricant {
        public String name;
        public int amount_required;
        public double boost;

        public Lubricant(String name, int amount_required, double boost) {
            this.name = name;
            this.amount_required = amount_required;
            this.boost = boost;
        }
    }

    public static final Map<String, Lubricant> lubricants;
    static {
        lubricants = new HashMap<>();
        lubricants.put("lubricating_oil", new Lubricant("LubricatingOil", 16, 1.0));
        lubricants.put("lubricant", new Lubricant("Lubricant", 8, 1.0));
        lubricants.put("midgrade_lubricant", new Lubricant("MidgradeLubricant", 4, 1.5));
        lubricants.put("premium_lubricant", new Lubricant("PremiumLubricant", 2, 1.5));
        lubricants.put("supreme_lubricant", new Lubricant("SupremeLubricant", 1, 2.0));
    }

    public static class Coolant {
        public String name;
        public int amount_required;

        public Coolant(String name, int amount_required) {
            this.name = name;
            this.amount_required = amount_required;
        }
    }

    public static final Map<String, Coolant> coolants;
    static {
        coolants = new HashMap<>();
        coolants.put("water", new Coolant("Water", 16));
        coolants.put("distilled_water", new Coolant("DistilledWater", 8));
        coolants.put("coolant", new Coolant("Coolant", 4));
        coolants.put("advanced_coolant", new Coolant("AdvancedCoolant", 1));
    }

    public static ResourceLocation susyId(String path) {
        return new ResourceLocation(Supersymmetry.MODID, path);
    }

    public static String getRLPrefix(Material material) {
        return material.getModid().equals(GTValues.MODID) ? "" : material.getModid() + ":";
    }
}
