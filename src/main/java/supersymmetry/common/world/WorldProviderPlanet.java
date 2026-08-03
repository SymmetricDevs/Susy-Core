package supersymmetry.common.world;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import supersymmetry.api.space.RenderableCelestialObject;
import supersymmetry.common.world.sky.SkyColorData;

public class WorldProviderPlanet extends WorldProvider {

    private long TICKS_PER_DAY = 24000L;

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

        if (FMLLaunchHandler.side() == Side.CLIENT) {
            PlanetoidHandler planet = SuSyDimensions.PLANETS.get(this.getDimension());
            if (planet != null) {
                IRenderHandler renderer = planet.getEffectiveSkyRenderer();
                if (renderer != null) {
                    this.setSkyRenderer(renderer);
                }
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
        PlanetoidHandler planet = SuSyDimensions.PLANETS.get(getDimension());
        if (planet != null) {
            return planet.getEffectiveSkyRenderer();
        }
        return null;
    }

    @Override
    public @NotNull Vec3d getSkyColor(net.minecraft.entity.Entity cameraEntity, float partialTicks) {
        PlanetoidHandler planet = getPlanet();
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
        PlanetoidHandler planet = getPlanet();
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
    public @Nullable float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        PlanetoidHandler planet = getPlanet();
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
        if (isEclipse(0.0f)) {
            return false;
        }
        return super.isDaytime();
    }

    public PlanetoidHandler getPlanet() {
        return SuSyDimensions.PLANETS.get(getDimension());
    }

    private float realCelestialAngle(long worldTime, float partialTicks) {
        PlanetoidHandler planet = getPlanet();
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
        PlanetoidHandler planet = getPlanet();
        if (planet == null || !planet.hasCustomSky()) return false;

        SuSySkyRenderer renderer = planet.getSuSySkyRenderer();
        if (renderer == null) return false;

        RenderableCelestialObject sun = renderer.getSunObject();
        RenderableCelestialObject primaryBody = renderer.getPrimaryBody(); // Earth on the Moon

        if (sun == null || primaryBody == null) return false;

        long worldTime = world.getWorldTime();

        float[] sunDir = sun.getWorldDirection(worldTime);
        float eclipseThresholdY = 0.035f;

        if (Math.abs(sunDir[1]) > eclipseThresholdY) {
            return false; // Not in eclipse season
        }

        float celestialAngle = realCelestialAngle(worldTime, partialTicks);
        float angleFromZenith = Math.abs(celestialAngle - 0.25f);
        if (angleFromZenith > 0.5f) angleFromZenith = 1.0f - angleFromZenith;

        float angleInDegrees = angleFromZenith * 360.0f;

        // Half the angular diameter == angular radius
        float bodyAngularRadius = primaryBody.getAngularSizeDeg() / 2.0f;

        return angleInDegrees < bodyAngularRadius;
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        if (isEclipse(partialTicks)) return 0.0f;
        return super.getSunBrightnessFactor(partialTicks);
    }

    @Override
    public float getSunBrightness(float partialTicks) {
        if (isEclipse(partialTicks)) return 0.0f;
        return super.getSunBrightness(partialTicks);
    }

    @Override
    public float getStarBrightness(float partialTicks) {
        if (isEclipse(partialTicks)) return 1.0f;
        return super.getStarBrightness(partialTicks);
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        if (isEclipse(0.0f)) return 1.0f;
        return 0.25f; // Normal Moon lighting (no actual moon)
    }

    @Override
    public float getCloudHeight() {
        return -100.0f; // No clouds on the Moon
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        PlanetoidHandler planet = getPlanet();
        if (planet != null) {
            if (isEclipse(partialTicks)) {
                return 0.75f; // Trick Minecraft into treating it as midnight
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

    @Override
    public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk) {
        return false;
    }

    @Override
    public void onWorldUpdateEntities() {
        super.onWorldUpdateEntities();
        this.world.getWorldInfo().setRainTime(0);
        this.world.getWorldInfo().setRaining(false);
    }

    @Override
    public void updateWeather() {
        this.world.getWorldInfo().setRainTime(0);
        this.world.getWorldInfo().setRaining(false);
        this.world.getWorldInfo().setThunderTime(0);
        this.world.getWorldInfo().setThundering(false);
    }

    @Override
    public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk) {
        return false;
    }
}
