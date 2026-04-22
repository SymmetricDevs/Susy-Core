package supersymmetry.api.space.reentry;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;

import supersymmetry.api.SusyLog;
import supersymmetry.common.world.ChunkGeneratorVoid;
import supersymmetry.common.world.SuSyBiomes;

/**
 * WorldProvider for the atmospheric re-entry corridor dimension.
 *
 * The dimension is a void-chunk world (no terrain) with a custom sky renderer
 * that animates the pod descending toward Earth.
 */
public class WorldProviderReEntry extends WorldProvider {

    private ReEntryDimension config;

    @Override
    protected void init() {
        int dimId = this.getDimension();
        this.config = ReEntryDimensions.REENTRY.get(dimId);

        if (config == null) {
            throw new IllegalStateException(
                    "[ReEntry] No ReEntryDimension registered for id " + dimId + ". REENTRY map has: " +
                            ReEntryDimensions.REENTRY.keySet());
        }

        SusyLog.logger.info("[ReEntry] WorldProviderReEntry.init() dimId=" + dimId + " name=" + config.name);

        this.biomeProvider = new BiomeProviderSingle(SuSyBiomes.VOID);
        this.hasSkyLight = true;

        generateLightBrightnessTable();
    }

    @Override
    protected void generateLightBrightnessTable() {
        float ambientLight = (config != null) ? config.ambientLight : 0.02f;
        for (int i = 0; i <= 15; i++) {
            float vanilla = 1.0F - i / 15.0F;
            this.lightBrightnessTable[i] = Math.max((1.0F - vanilla * vanilla * vanilla * vanilla), ambientLight);
        }
    }

    @Override
    public DimensionType getDimensionType() {
        return ReEntryDimensions.reEntryType;
    }

    @Override
    public String getSaveFolder() {
        return "SuSy_ReEntry_" + config.name;
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorVoid(this.world);
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    @Override
    public boolean hasSkyLight() {
        return true;
    }

    @Override
    public IRenderHandler getSkyRenderer() {
        return config.renderer;
    }

    @Override
    public IRenderHandler getCloudRenderer() {
        return null;
    }

    @Override
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return new Vec3d(0.0, 0.0, 0.0);
    }

    @Override
    public Vec3d getSkyColor(net.minecraft.entity.Entity cameraEntity, float partialTicks) {
        // During re-entry the atmosphere renderer provides colour; keep provider black.
        return new Vec3d(0.0, 0.0, 0.0);
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        if (config == null) return 0f;
        double fraction = (worldTime % config.orbitalPeriodTicks) / (double) config.orbitalPeriodTicks;
        return (float) fraction;
    }

    @Override
    public boolean isSkyColored() {
        return false;
    }

    @Override
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        return null;
    }

    public float getGravity() {
        return config != null ? config.gravity : 0.0f;
    }

    public ReEntryDimension getConfig() {
        return config;
    }

    @Override
    public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk) {
        return false;
    }

    @Override
    public int getAverageGroundLevel() {
        return 0;
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }
}
