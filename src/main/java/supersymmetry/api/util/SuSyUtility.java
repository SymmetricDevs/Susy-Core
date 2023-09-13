package supersymmetry.api.util;

import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;
import supersymmetry.Supersymmetry;

import java.util.function.Function;

public class SuSyUtility {
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
