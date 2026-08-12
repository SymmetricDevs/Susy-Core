package supersymmetry.api.space.dimension;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;

import supersymmetry.api.SusyLog;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.CelestialOrbitRegistry;
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
            throw new IllegalStateException(
                    "No SpaceDimension registered for id " + dimId + ". SPACE map has: " +
                            SuSyDimensions.SPACE.keySet());
        }

        SusyLog.logger.info(
                "[Space] WorldProviderSpace.init() dimId=" + dimId + " name=" + config.name + " renderer=" +
                        config.renderer);

        this.biomeProvider = new BiomeProviderSingle(SuSyBiomes.VOID);
        this.hasSkyLight = true;

        generateLightBrightnessTable();
    }

    @Override
    protected void generateLightBrightnessTable() {
        for (int i = 0; i <= 15; i++) {
            float vanilla = 1.0F - i / 15.0F;
            this.lightBrightnessTable[i] = 1.0F - vanilla * vanilla * vanilla * vanilla;
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

    @Override
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        return null;
    }

    @Override
    public float getSunBrightness(float partialTicks) {
        long worldTime = world.getWorldTime();
        Vec3d earthPos = CelestialOrbitRegistry.get(CelestialObjects.EARTH)
                .computeAbsolutePosition(CelestialObjects.EARTH, worldTime + partialTicks);
        Vec3d sunDir = earthPos.normalize().scale(-1);
        float orbitalPhase = world.getCelestialAngle(partialTicks);
        float playerAngle = orbitalPhase * ((float) Math.PI * 2F);

        Vec3d playerDir = new Vec3d(MathHelper.cos(playerAngle), 0, MathHelper.sin(playerAngle));

        float dot = (float) playerDir.dotProduct(sunDir);
        return MathHelper.clamp(dot, 0.0F, 1.0F);
    }

    @Override
    public float getStarBrightness(float partialTicks) {
        return 1.0F - getSunBrightness(partialTicks);
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
