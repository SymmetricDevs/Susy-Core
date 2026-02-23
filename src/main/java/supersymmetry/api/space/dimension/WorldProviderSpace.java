package supersymmetry.api.space.dimension;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;

import supersymmetry.api.SusyLog;
import supersymmetry.common.world.ChunkGeneratorVoid;
import supersymmetry.common.world.SuSyBiomes;
import supersymmetry.common.world.SuSyDimensions;

public class WorldProviderSpace extends WorldProvider {

    private SpaceDimension config;

    @Override
    protected void init() {
        int dimId = this.getDimension();
        this.config = SuSyDimensions.SPACE.get(dimId);

        if (this.config == null) {
            throw new IllegalStateException("No SpaceDimension registered for id " + dimId + ". SPACE map has: " +
                    SuSyDimensions.SPACE.keySet());
        }

        SusyLog.logger.info("[Space] WorldProviderSpace.init() dimId=" + dimId + " name=" + config.name + " renderer=" +
                config.renderer);

        this.biomeProvider = new BiomeProviderSingle(SuSyBiomes.VOID);
        this.hasSkyLight = true;

        // Inject ambient light floor into the brightness table Minecraft uses
        generateLightBrightnessTable();
    }

    /** Override the light table to give a minimum ambient level in space. */
    @Override
    protected void generateLightBrightnessTable() {
        float ambientLight = (config != null) ? config.ambientLight : 0.05f;
        for (int i = 0; i <= 15; i++) {
            float vanilla = 1.0F - i / 15.0F;
            this.lightBrightnessTable[i] = Math.max((1.0F - vanilla * vanilla * vanilla * vanilla), ambientLight);
        }
    }

    @Override
    public DimensionType getDimensionType() {
        return SuSyDimensions.spaceType;
    }

    @Override
    public String getSaveFolder() {
        return "SuSy_Space_" + config.name;
    }

    public String getDimensionName() {
        return config.name;
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
        SusyLog.logger.info("[Space] getSkyRenderer() returning " + config.renderer);
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
        return new Vec3d(0.0, 0.0, 0.0);
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        double fraction = ((worldTime % config.ticksPerDay) + config.timeOffset) / (double) config.ticksPerDay;
        return (float) fraction;
    }

    @Override
    public boolean isSkyColored() {
        return false;
    }

    public float getGravity() {
        return config.gravity;
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
