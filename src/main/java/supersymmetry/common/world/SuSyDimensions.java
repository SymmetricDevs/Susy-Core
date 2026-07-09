package supersymmetry.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import supersymmetry.api.SusyLog;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.dimension.SpaceDimension;
import supersymmetry.api.space.dimension.WorldProviderSpace;
import supersymmetry.common.blocks.BlockRegolith;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.common.world.biome.SuSyBiomeEntry;

public class SuSyDimensions {

    public static DimensionType planetType;
    public static DimensionType spaceType;

    public static List<Biome> BIOMES = new ArrayList<>();
    public static Map<Integer, PlanetoidHandler> PLANETS = new Int2ObjectArrayMap<>();

    public static Map<Integer, SpaceDimension> SPACE = new Int2ObjectArrayMap<>();

    static long leoOrbitTicks = 110_400L;

    public static void init() {
        int id = -2;
        for (DimensionType type : DimensionType.values()) {
            if (type.getId() < id) id = type.getId();
        }
        id--;

        SusyLog.logger.info("Registering planet dimension type at id " + id);
        planetType = DimensionType.register("Supersymmetry Planet", "_susy", id, WorldProviderPlanet.class, false);

        SusyLog.logger.info("Registering space dimension type at id " + (id - 1));
        spaceType = DimensionType.register("susy_space", "_susyspace", id - 1, WorldProviderSpace.class, false);

        long lunarDayTicks = 708_734L;

        new PlanetoidHandler(CelestialObjects.MOON).setBiomeList(
                new SuSyBiomeEntry(SuSyBiomes.LUNAR_HIGHLANDS, 80)
                        .setCraterBlock(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.HIGHLAND)),
                new SuSyBiomeEntry(SuSyBiomes.LUNAR_MARIA, 80)
                        .setCraterBlock(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.LOWLAND)),
                new SuSyBiomeEntry(SuSyBiomes.LUNAR_KREEP_TERRANE, 40)
                        .setCraterBlock(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.KREEP)))
                .setStone(SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH)
                        .getState(SusyStoneVariantBlock.StoneType.ANORTHOSITE))
                .setCustomSkyRenderer(CelestialObjects.RENDERER)
                .setGravity(0.166f)
                .setBiomeSize(7)
                .setTicksPerDay(lunarDayTicks)
                .setDayLength(29.53f)
                .setTimeOffset(0.0f)
                .load();

        new SpaceDimension(802, "low_earth_orbit")
                .setRenderer(CelestialObjects.RENDERER)
                .setGravity(0.0f)
                .setAmbientLight(0.02f)
                .setVacuum(true)
                .setDayCycle(leoOrbitTicks, 1.53f, 0.0f)
                .load();

        if (!DimensionManager.isDimensionRegistered(802)) {
            DimensionManager.registerDimension(802, spaceType);
            SusyLog.logger.info("Registered Low Earth Orbit space dimension at id 802");
        }
    }
}
