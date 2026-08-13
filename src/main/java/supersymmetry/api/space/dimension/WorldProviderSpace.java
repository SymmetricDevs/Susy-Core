package supersymmetry.api.space.dimension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.DimensionManager;

import supersymmetry.api.SusyLog;
import supersymmetry.api.space.CelestialObject;
import supersymmetry.api.space.Orbit;
import supersymmetry.common.world.ChunkGeneratorVoid;
import supersymmetry.common.world.SuSyBiomes;
import supersymmetry.common.world.SuSyDimensions;

public class WorldProviderSpace extends WorldProvider {

    private static final Map<Integer, SpaceConfig> SPACE_CONFIGS = new HashMap<>();

    private SpaceConfig config;

    public static SpaceConfig create(int dimId, String name) {
        return new SpaceConfig(dimId, name);
    }

    public static SpaceConfig get(int dimId) {
        return SPACE_CONFIGS.get(dimId);
    }

    public static Set<Integer> getRegisteredIds() {
        return SPACE_CONFIGS.keySet();
    }

    @Override
    protected void init() {
        int dimId = this.getDimension();
        this.config = get(dimId);

        if (this.config == null) {
            throw new IllegalStateException(
                    String.format("No space dimension configuration registered for id %d. Registered: %s", dimId,
                            getRegisteredIds()));
        }

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
        return "space_" + config.name;
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
        Orbit orbit = config.orbit;
        if (orbit == null) return 0.0F;
        double m = orbit.meanAnomalyAtEpochRad + 2.0 * Math.PI * (worldTime - orbit.epochTicks) / orbit.periodTicks;
        m = (m % (2.0 * Math.PI) + 2.0 * Math.PI) % (2.0 * Math.PI);
        return (float) (m / (2.0 * Math.PI));
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
        if (!world.isRemote) return 0.0F;
        var mc = Minecraft.getMinecraft();
        if (mc.player == null) return 0.0F;
        Orbit orbit = config.orbit;
        CelestialObject center = config.centeredOn;
        if (orbit == null || center == null) return 0.0F;

        double worldTime = world.getWorldTime() + partialTicks;
        Vec3d centerPos = Orbit.computeAbsolutePosition(center, worldTime);
        Vec3d stationPos = centerPos.add(orbit.computeRelativePosition(worldTime));
        Vec3d up = stationPos.subtract(centerPos);
        if (up.lengthSquared() < 1e-15) return 0.0F;
        up = Orbit.normalizeSafe(up);

        CelestialObject sun = center.findPrimaryStar();
        if (sun == null) return 0.0F;

        Vec3d sunDir = Orbit.computeAbsolutePosition(sun, worldTime).subtract(stationPos).normalize();
        return MathHelper.clamp((float) sunDir.dotProduct(up), 0.0F, 1.0F);
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
    public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk) {
        return false;
    }

    /**
     * Static configuration for a space (non-planetoid) dimension. Instances are
     * created via {@link WorldProviderSpace#create} and stored in a static
     * registry so that provider instances can look up their own configuration
     * during init.
     */
    public static final class SpaceConfig {

        public final int id;
        public final String name;

        public float gravity = 0.0f;
        public IRenderHandler renderer;
        public CelestialObject centeredOn;
        public Orbit orbit;

        private SpaceConfig(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public SpaceConfig setRenderer(IRenderHandler renderer) {
            this.renderer = renderer;
            return this;
        }

        public SpaceConfig setGravity(float g) {
            this.gravity = g;
            return this;
        }

        public SpaceConfig setOrbit(CelestialObject centeredOn, Orbit orbit) {
            this.centeredOn = centeredOn;
            this.orbit = orbit;
            return this;
        }

        public void register() {
            SPACE_CONFIGS.put(id, this);

            // Register with Forge so the dimension actually exists
            if (!DimensionManager.isDimensionRegistered(id)) {
                DimensionManager.registerDimension(id, SuSyDimensions.spaceType);
                SusyLog.logger.info(String.format("Registered space dimension '%s' at id %d", name, id));
            }
        }
    }
}