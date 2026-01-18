package supersymmetry.common.world;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldProviderPlanet extends WorldProvider {

    @Override
    public @NotNull DimensionType getDimensionType() {
        return SuSyDimensions.planetType;
    }

    @Override
    protected void init() {
        this.hasSkyLight = true;
        biomeProvider = new PlanetBiomeProvider(world);
        Planet planet = SuSyDimensions.PLANETS.get(this.getDimension());
        if (planet != null) {
            // Use getEffectiveSkyRenderer to get custom sky if available, otherwise default
            IRenderHandler renderer = planet.getEffectiveSkyRenderer();
            if (renderer != null) {
                this.setSkyRenderer(renderer);
            }
        }
    }

    @Override
    public @NotNull IChunkGenerator createChunkGenerator() {
        return new PlanetChunkGenerator(world, world.getSeed());
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    /**
     * Will check if the x, z position specified is alright to be set as the map spawn point
     */
    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        return false;
    }

    @Override
    public boolean isSkyColored() {
        return false;
    }

    @Override
    public @Nullable IRenderHandler getSkyRenderer() {
        Planet planet = SuSyDimensions.PLANETS.get(getDimension());
        if (planet != null) {
            // Return the effective sky renderer (custom if set, otherwise default)
            return planet.getEffectiveSkyRenderer();
        }
        return null;
    }

    @Override
    public @NotNull Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
        return new Vec3d(0.0D, 0.0D, 0.0D);
    }

    @Override
    public @Nullable float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        return new float[] { 0.0F, 0.0F, 0.0F, 0.0F };
    }

    public Planet getPlanet() {
        return SuSyDimensions.PLANETS.get(getDimension());
    }
}
