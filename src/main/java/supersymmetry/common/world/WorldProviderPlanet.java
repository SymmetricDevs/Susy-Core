package supersymmetry.common.world;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldProviderPlanet extends WorldProvider {

    private static final long TICKS_PER_DAY = 24000L;

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

    // All this stuff below is for custom day/night cycle time overrides
    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        Planet planet = getPlanet();
        if (planet != null && planet.getDayLength() > 0) {
            // Get the planet's custom day length
            float dayLengthMultiplier = planet.getDayLength();

            // Example: for the moon, dayLength = 29.53, so one full cycle takes 29.53 Minecraft days
            long adjustedTime = (long) (worldTime / dayLengthMultiplier);

            // Calculate the angle within a single day cycle
            int dayPart = (int) (adjustedTime % TICKS_PER_DAY);
            float angle = ((float) dayPart + partialTicks) / (float) TICKS_PER_DAY;

            // Normalize to 0-1 range
            angle = angle - (float) Math.floor(angle);

            // Apply the planet's time offset
            // 0.0 = start at sunrise, 0.5 = start at night, etc.
            angle = (angle + planet.getTimeOffset()) % 1.0F;

            return angle;
        }

        // Default behavior for planets without custom day length
        return super.calculateCelestialAngle(worldTime, partialTicks);
    }

    @Override
    public boolean isDaytime() {
        Planet planet = getPlanet();
        if (planet != null && planet.getDayLength() > 0) {
            float angle = calculateCelestialAngle(this.world.getWorldTime(), 0);
            // Day is when angle is between 0.0 and 0.5 (same as vanilla)
            return angle >= 0.0F && angle < 0.5F;
        }
        return super.isDaytime();
    }

    public Planet getPlanet() {
        return SuSyDimensions.PLANETS.get(getDimension());
    }
}
