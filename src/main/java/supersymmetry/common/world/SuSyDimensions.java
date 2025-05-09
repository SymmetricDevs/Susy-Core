package supersymmetry.common.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import supersymmetry.api.SusyLog;

import java.util.ArrayList;
import java.util.List;

public class SuSyDimensions {

    public static DimensionType planetType;
    public static DimensionType spaceType;

    public static List<Biome> BIOMES = new ArrayList<>();
    public static List<WorldType> WORLD_TYPES = new ArrayList<>();

    public static void init() {
        int id = 800;

        for (DimensionType type : DimensionType.values()) {
            if (type.getId() > id) {
                id = type.getId();
            }
        }
        id++;

        SusyLog.logger.info("Registering planet dimension type at id " + id);
        planetType = DimensionType.register("susy_planet", "_susy", id, WorldProviderPlanet.class, false);

    }

}
