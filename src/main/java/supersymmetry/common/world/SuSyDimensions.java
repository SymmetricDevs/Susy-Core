package supersymmetry.common.world;

import net.minecraft.world.DimensionType;

import supersymmetry.api.SusyLog;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.Orbit;
import supersymmetry.api.space.dimension.WorldProviderSpace;
import supersymmetry.common.blocks.BlockRegolith;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.common.world.biome.SuSyBiomeEntry;

public class SuSyDimensions {

    public static DimensionType planetType;
    public static DimensionType spaceType;

    public static void init() {
        int id = -2;
        for (DimensionType type : DimensionType.values()) {
            if (type.getId() < id)
                id = type.getId();
        }
        id--;

        SusyLog.logger.info("Registering planet dimension type at id " + id);
        planetType = DimensionType.register("Supersymmetry Planet", "_susy", id, WorldProviderPlanet.class, false);

        SusyLog.logger.info("Registering space dimension type at id " + (id - 1));
        spaceType = DimensionType.register("susy_space", "_susyspace", id - 1, WorldProviderSpace.class, false);

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
                .load();

        WorldProviderSpace.create(802, "low_earth_orbit")
                .setOrbit(CelestialObjects.EARTH, new Orbit(
                        0.00007, 0.0, Math.toRadians(51.6), 0.0, 0.0, 0.0, 0L, 110_400L))
                .setRenderer(CelestialObjects.RENDERER)
                .setGravity(0.0f)
                .register();
    }
}
