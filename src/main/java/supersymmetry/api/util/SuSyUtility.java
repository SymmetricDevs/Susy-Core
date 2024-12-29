package supersymmetry.api.util;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.util.RelativeDirection;
import gregtech.core.unification.material.internal.MaterialRegistryManager;
import net.minecraft.util.ResourceLocation;
import supersymmetry.Supersymmetry;

import java.util.function.Function;

import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.DULL;
import static gregtech.api.util.GTUtility.gregtechId;

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

    /**
     * Returns a new String with the character at the given index replaced with the given character
     *
     * @param str     The original string whose character is to be replaced
     * @param index   The index of the character to be replaced
     * @param replace The character to replace the character at the given index
     * @return The new string with the character at the given index replaced with the given character
     */
    public static String replace(String str, int index, char replace) {
        if (str == null) {
            return str;
        } else if (index < 0 || index >= str.length()) {
            return str;
        }
        char[] chars = str.toCharArray();
        chars[index] = replace;
        return String.valueOf(chars);
    }

    // Work-around for not having Corium in susycore
    public static Material Corium() {
        Material corium = MaterialRegistryManager.getInstance().getMaterial("corium");
        if (corium == null) {
            corium = new Material.Builder(1560, gregtechId("corium"))
                    .liquid(new FluidBuilder().temperature(2500).block().density(8.0D).viscosity(10000))
                    .color(0x7A6B50)
                    .iconSet(DULL)
                    .flags(NO_UNIFICATION, STICKY, GLOWING)
                    .build();
        }
        return corium;
    }
}
