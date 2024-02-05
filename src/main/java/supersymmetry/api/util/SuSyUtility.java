package supersymmetry.api.util;

import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;
import supersymmetry.Supersymmetry;

import java.util.function.Function;

public class SuSyUtility {

    /*
        if we assume a solar boiler to have an approximately 1m^2 "collecting" surface, and take the hp to be less inefficient,
        one can approximate the J to EU conversion via 18L/t -> 9EU/t -> 180EU/s -> 1000J/180EU ~= 6J in one EU.
        Assuming solar actually gets slightly less sunlight than 1m^2 conversion may be ~= 5, but potential inefficiencies
        make it unclear the specific amount. Just going to round to one sig fig and leave it at 10J -> 1EU
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

    public static ResourceLocation susyId(String path) {
        return new ResourceLocation(Supersymmetry.MODID, path);
    }
}
