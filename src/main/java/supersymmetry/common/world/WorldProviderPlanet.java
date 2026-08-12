package supersymmetry.common.world;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
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

import supersymmetry.api.space.CelestialObject;
import supersymmetry.api.space.Orbit;
import supersymmetry.api.space.Planetoid;
import supersymmetry.api.space.Star;

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
            PlanetoidHandler planet = SuSyDimensions.PLANETS.get(this.getDimension());
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
        PlanetoidHandler planet = SuSyDimensions.PLANETS.get(getDimension());
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
        return SuSyDimensions.PLANETS.get(getDimension());
    }

    private Planetoid getGroundPlanet() {
        return Planetoid.PLANETOIDS.inverse().get(getDimension());
    }

    public Vec3d getLocalUp(Planetoid ground, double x, double z, double worldTime) {
        return tidalUp(ground, x, z, worldTime);
    }

    public Vec3d getLocalUpForPlayer(Planetoid ground, EntityPlayer player, double worldTime) {
        return tidalUp(ground, player.posX, player.posZ, worldTime);
    }

    private Vec3d tidalUp(Planetoid ground, double x, double z, double worldTime) {
        Vec3d up = Orbit.surfacePointToLocalUp(x, z, ground.getRadius());
        Vec3d axis = ground.getRotationAxis();
        if (axis == null) return up;
        return Orbit.rotateAboutAxis(up, axis, -ground.getRotationAngle(worldTime));
    }

    public boolean isEclipse(float partialTicks) {
        double worldTime = world.getWorldTime() + partialTicks;
        Planetoid ground = getGroundPlanet();
        if (ground == null) return false;

        CelestialObject sun = ground.findPrimaryStar();
        if (sun == null) return false;

        Vec3d sunPos = Orbit.computeAbsolutePosition(sun, worldTime);
        Vec3d groundPos = Orbit.computeAbsolutePosition(ground, worldTime);
        Vec3d toSun = sunPos.subtract(groundPos);
        double distToSun = toSun.length();
        if (distToSun < 1e-15) return false;
        Vec3d toSunDir = toSun.normalize();
        double angularRadiusSun = sun.getRadiusAU() / distToSun;

        for (CelestialObject child : ground.getChildBodies()) {
            if (isOccluding(child, groundPos, toSunDir, distToSun, angularRadiusSun, worldTime))
                return true;
        }

        CelestialObject parent = ground.getParentBody();
        if (parent != null && !(parent instanceof Star)) {
            if (isOccluding(parent, groundPos, toSunDir, distToSun, angularRadiusSun, worldTime))
                return true;
        }

        return false;
    }

    private boolean isOccluding(
                                CelestialObject occluder,
                                Vec3d observerPos,
                                Vec3d toSunDir,
                                double distToSun,
                                double angularRadiusSun,
                                double worldTime) {
        Vec3d oPos = Orbit.computeAbsolutePosition(occluder, worldTime);
        Vec3d toOccluder = oPos.subtract(observerPos);
        double distToOccluder = toOccluder.length();
        if (distToOccluder < 1e-15) return false;

        double projDist = toOccluder.dotProduct(toSunDir);
        if (projDist <= 0 || projDist >= distToSun) return false;

        Vec3d perp = toOccluder.subtract(toSunDir.scale(projDist));
        double distPerp = perp.length();

        double angularRadiusOccluder = occluder.getRadiusAU() / distToOccluder;
        double angularSeparation = distPerp / distToOccluder;

        return angularSeparation < angularRadiusOccluder + angularRadiusSun;
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
        if (world.isRemote) {
            var mc = Minecraft.getMinecraft();
            if (mc.player != null) {
                Planetoid ground = getGroundPlanet();
                if (ground != null) {
                    double worldTime = world.getWorldTime() + partialTicks;
                    Vec3d localUp = getLocalUpForPlayer(ground, mc.player, worldTime);
                    double solarAltitude = Orbit.computeSolarAltitude(
                            ground, localUp, worldTime);
                    if (!Double.isNaN(solarAltitude))
                        return (float) MathHelper.clamp(solarAltitude * 4.0, 0.0, 1.0);
                }
            }
        }
        return 0.0f;
    }

    @Override
    public float getStarBrightness(float partialTicks) {
        if (isEclipse(partialTicks)) return 1.0f;
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
