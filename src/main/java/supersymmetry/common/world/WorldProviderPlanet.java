package supersymmetry.common.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderPlanet extends WorldProvider {
    @Override
    public DimensionType getDimensionType() {
        return SuSyDimensions.planetType;
    }

    @Override
    protected void init() {
        biomeProvider = new PlanetBiomeProvider(world);

    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new PlanetChunkGenerator(world, world.getSeed());
    }

    @Override
    public boolean isSurfaceWorld()
    {
        return false;
    }

    /**
     * Will check if the x, z position specified is alright to be set as the map spawn point
     */
    @Override
    public boolean canCoordinateBeSpawn(int x, int z)
    {
        return false;
    }
}
