package supersymmetry.common.world;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import supersymmetry.api.space.Orbit;
import supersymmetry.api.space.Planetoid;

public class WorldProviderPlanet extends WorldProvider {

    @Override
    public @NotNull DimensionType getDimensionType() {
        return SuSyDimensions.planetType;
    }

    @Override
    protected void init() {
        this.hasSkyLight = true;
        biomeProvider = new PlanetBiomeProvider(world);

        if (FMLLaunchHandler.side() == Side.CLIENT) {
            PlanetoidHandler planet = PlanetoidHandler.get(this.getDimension());
            if (planet != null) {
                IRenderHandler renderer = planet.getSkyRenderer();
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
        PlanetoidHandler planet = PlanetoidHandler.get(getDimension());
        if (planet != null) {
            return planet.getSkyRenderer();
        }
        return null;
    }

    @Override
    public @NotNull Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        return new Vec3d(0.0D, 0.0D, 0.0D);
    }

    @Override
    public @NotNull Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return new Vec3d(0.0D, 0.0D, 0.0D);
    }

    @Override
    public @Nullable float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        return null;
    }

    @Override
    public boolean isDaytime() {
        return false;
    }

    public PlanetoidHandler getPlanet() {
        return PlanetoidHandler.get(getDimension());
    }

    private Planetoid getGroundPlanet() {
        return Planetoid.PLANETOIDS.inverse().get(getDimension());
    }

    @Override
    protected void generateLightBrightnessTable() {
        float ambientLight = 0.0f;
        for (int i = 0; i <= 15; i++) {
            float vanilla = 1.0F - i / 15.0F;
            this.lightBrightnessTable[i] = Math.max(1.0F - vanilla * vanilla * vanilla * vanilla, ambientLight);
        }
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        return getSunBrightness(partialTicks);
    }

    @Override
    public float getSunBrightness(float partialTicks) {
        if (!world.isRemote) return 0.0f;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return 0.0f;
        Planetoid ground = getGroundPlanet();
        if (ground == null) return 0.0f;
        double worldTime = world.getWorldTime() + partialTicks;
        Vec3d localUp = Orbit.getLocalUp(ground, mc.player.posX, mc.player.posZ, worldTime);
        return Orbit.getSunBrightness(ground, localUp, worldTime);
    }

    @Override
    public float getStarBrightness(float partialTicks) {
        Planetoid ground = getGroundPlanet();
        if (ground == null) return 0.0f;
        double worldTime = world.getWorldTime() + partialTicks;
        if (Orbit.isEclipse(ground, worldTime)) return 1.0f;
        return 1.0F - getSunBrightness(partialTicks);
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        return 0.25f;
    }

    @Override
    public float getCloudHeight() {
        return -100.0f;
    }

    @Override
    public boolean canDoRainSnowIce(Chunk chunk) {
        return false;
    }

    @Override
    public boolean canDoLightning(Chunk chunk) {
        return false;
    }
}
