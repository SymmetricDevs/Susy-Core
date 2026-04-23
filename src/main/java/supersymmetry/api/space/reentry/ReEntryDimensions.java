package supersymmetry.api.space.reentry;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.world.DimensionType;
import supersymmetry.api.SusyLog;
import supersymmetry.common.world.SuSyDimensions;

import java.util.Map;

/**
 * Central registry for all re-entry corridor dimensions.
 *
 * Call {@link #init()} during mod setup (after {@link SuSyDimensions#init()}).
 */
public class ReEntryDimensions {

    public static DimensionType reEntryType;
    public static final Map<Integer, ReEntryDimension> REENTRY = new Int2ObjectArrayMap<>();

    public static final int EARTH_REENTRY_DIM_ID = 803;

    public static void init() {
        int id = -2;
        for (DimensionType type : DimensionType.values()) {
            if (type.getId() < id) id = type.getId();
        }
        id--;

        SusyLog.logger.info("[ReEntry] Registering re-entry dimension type at id " + id);
        reEntryType = DimensionType.register(
                "susy_reentry",
                "_susyreentry",
                id,
                WorldProviderReEntry.class,
                false);

        ReEntryDimension.createEarthReEntry(EARTH_REENTRY_DIM_ID, 0).load();

        SusyLog.logger.info("[ReEntry] Re-entry dimensions initialised.");
    }
}
