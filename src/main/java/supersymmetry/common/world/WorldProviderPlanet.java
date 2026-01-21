package supersymmetry.common.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import supersymmetry.api.SusyLog;
import supersymmetry.common.world.sky.SkyColorData;
import supersymmetry.common.world.sky.SkyRenderData;

public class WorldProviderPlanet extends WorldProvider {

    private long TICKS_PER_DAY = 24000L; // the maximum for a long (signed 64 bit) is 2^63

    public void setTicksPerDay(long ticksPerDay) {
        this.TICKS_PER_DAY = ticksPerDay;
    }

    public long getTicksPerDay() {
        return this.TICKS_PER_DAY;
    }

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

    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        return false;
    }

    @Override
    public boolean isSkyColored() {
        return true;
    }

    @Override
    public @Nullable IRenderHandler getSkyRenderer() {
        Planet planet = SuSyDimensions.PLANETS.get(getDimension());
        if (planet != null) {
            return planet.getEffectiveSkyRenderer();
        }
        return null;
    }

    @Override
    public @NotNull Vec3d getSkyColor(net.minecraft.entity.Entity cameraEntity, float partialTicks) {
        Planet planet = getPlanet();
        if (planet != null && planet.hasCustomSky()) {
            SuSySkyRenderer skyRenderer = planet.getSuSySkyRenderer();
            if (skyRenderer != null && skyRenderer.getSkyColorData() != null) {
                float celestialAngle = world.getCelestialAngle(partialTicks);
                return skyRenderer.getSkyColorData().getSkyColor(celestialAngle);
            }
        }
        return new Vec3d(0.0D, 0.0D, 0.0D);
    }

    @Override
    public @NotNull Vec3d getFogColor(float celestialAngle, float partialTicks) {
        Planet planet = getPlanet();
        if (planet != null && planet.hasCustomSky()) {
            SuSySkyRenderer skyRenderer = planet.getSuSySkyRenderer();
            if (skyRenderer != null && skyRenderer.getSkyColorData() != null) {
                SkyColorData colorData = skyRenderer.getSkyColorData();
                if (colorData.useFogColor()) {
                    return colorData.getFogColor();
                }
            }
        }
        return new Vec3d(0.0D, 0.0D, 0.0D);
    }

    @Override
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        return new Vec3d(0.0D, 0.0D, 0.0D);
    }

    @Override
    public @Nullable float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        Planet planet = getPlanet();
        if (planet != null && planet.hasCustomSky()) {
            SuSySkyRenderer skyRenderer = planet.getSuSySkyRenderer();
            if (skyRenderer != null && skyRenderer.getSkyColorData() != null) {
                return skyRenderer.getSkyColorData().getSunriseSunsetColors(celestialAngle);
            }
        }
        return null;
    }

    @Override
    public boolean isDaytime() {
        // During eclipse, it's night (mobs spawn, beds work, etc)
        if (isEclipse(0.0f)) {
            return false;
        }
        return super.isDaytime();
    }

    public Planet getPlanet() {
        return SuSyDimensions.PLANETS.get(getDimension());
    }

    private float realCelestialAngle(long worldTime, float partialTicks) {
        Planet planet = getPlanet();
        if (planet != null && planet.getDayLength() > 0) {
            float dayLengthMultiplier = planet.getDayLength();
            long adjustedTime = (long) (worldTime / dayLengthMultiplier);
            int dayPart = (int) (adjustedTime % TICKS_PER_DAY);
            float angle = ((float) dayPart + partialTicks) / (float) TICKS_PER_DAY;
            angle = angle - (float) Math.floor(angle);
            angle = (angle + planet.getTimeOffset()) % 1.0F;

            return angle;
        }
        return world.getCelestialAngle(partialTicks);
    }

    public boolean isEclipse(float partialTicks) {
        Planet planet = getPlanet();
        if (planet == null || !planet.hasCustomSky()) return false;

        SuSySkyRenderer renderer = planet.getSuSySkyRenderer();
        SkyRenderData sun = renderer.getSun();
        SkyRenderData earth = renderer.getObjectAtZenith();

        if (sun == null || earth == null) return false;

        // Check if sun has orbital inclination configured
        if (sun.getNodalPeriodLength() <= 0) return false;

        long worldTime = world.getWorldTime();

        // Get current orbital inclination (how far north/south the sun's path is)
        float currentInclination = sun.getCurrentInclination(worldTime);

        // Eclipse can only occur when the orbital plane crosses the zenith
        // This means the inclination must be near zero (within eclipse threshold)
        float eclipseThreshold = 2.0f; // degrees - tune this for eclipse frequency

        if (Math.abs(currentInclination) > eclipseThreshold) {
            return false; // Not in eclipse season
        }

        // Now check if sun is at zenith (same position as Earth)
        // With timeOffset=0.25, when worldTime=0, celestialAngle should be 0.25
        float celestialAngle = realCelestialAngle(worldTime, partialTicks);

        // Zenith is at 0.25
        float angleFromZenith = Math.abs(celestialAngle - 0.25f);

        // Handle wrap-around
        if (angleFromZenith > 0.5f) {
            angleFromZenith = 1.0f - angleFromZenith;
        }

        // Convert to degrees
        float angleInDegrees = angleFromZenith * 360.0f;

        // Eclipse occurs when sun is within Earth's angular radius
        float earthAngularRadius = earth.getSize() / 3.0f;

        return angleInDegrees < earthAngularRadius;
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        if (isEclipse(partialTicks)) {
            return 0.0f;
        }
        return super.getSunBrightnessFactor(partialTicks);
    }

    @Override
    public float getSunBrightness(float partialTicks) {
        if (isEclipse(partialTicks)) {
            return 0.0f;
        }
        return super.getSunBrightness(partialTicks);
    }

    @Override
    public float getStarBrightness(float partialTicks) {
        // Show stars during eclipse
        if (isEclipse(partialTicks)) {
            return 1.0f;
        }
        return super.getStarBrightness(partialTicks);
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        // During eclipse, return 1.0 to maximize darkness
        if (isEclipse(0.0f)) {
            return 1.0f;
        }
        return 0.25f; // Normal Moon lighting (no actual moon)
    }

    @Override
    public float getCloudHeight() {
        return -100.0f; // No clouds on the Moon
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        Planet planet = getPlanet();
        if (planet != null) {
            // During eclipse, trick Minecraft into thinking it's night
            if (isEclipse(partialTicks)) {
                // Return an angle that corresponds to midnight (0.75)
                return 0.75f;
            }

            if (planet.getDayLength() > 0) {
                float dayLengthMultiplier = planet.getDayLength();
                long adjustedTime = (long) (worldTime / dayLengthMultiplier);
                int dayPart = (int) (adjustedTime % TICKS_PER_DAY);
                float angle = ((float) dayPart + partialTicks) / (float) TICKS_PER_DAY;
                angle = angle - (float) Math.floor(angle);
                angle = (angle + planet.getTimeOffset()) % 1.0F;
                return angle;
            }
        }
        return super.calculateCelestialAngle(worldTime, partialTicks);
    }
}
