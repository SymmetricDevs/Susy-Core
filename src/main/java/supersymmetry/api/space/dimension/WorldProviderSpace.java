package supersymmetry.api.space.dimension;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;

import supersymmetry.common.world.ChunkGeneratorVoid;
import supersymmetry.common.world.SuSyBiomes;
import supersymmetry.common.world.SuSyDimensions;

public class WorldProviderSpace extends WorldProvider {

    private SpaceDimension config;

    @Override
    protected void init() {
        this.config = SuSyDimensions.SPACE.get(this.getDimension());
        if (this.config == null) {
            throw new IllegalStateException(
                    "No SpaceDimension registered for dimension id " + this.getDimension());
        }
        this.biomeProvider = new BiomeProviderSingle(SuSyBiomes.VOID);
        this.hasSkyLight = true;
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
    public float[] getLightBrightnessTable() {
        float[] table = new float[16];
        for (int i = 0; i < 16; i++) {
            float vanilla = i / 15.0F;
            table[i] = Math.max(vanilla, config.ambientLight);
        }
        return table;
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
}
