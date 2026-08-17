package supersymmetry.mixins.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;

import supersymmetry.api.space.Orbit;
import supersymmetry.api.space.Planetoid;

@Mixin(WorldProviderSurface.class)
public abstract class WorldProviderSurfaceMixin extends WorldProvider {

    @Override
    public boolean isSkyColored() {
        return true;
    }

    @Override
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        return Vec3d.ZERO;
    }

    @Override
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return Vec3d.ZERO;
    }

    @Override
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        return null;
    }

    @Override
    public boolean isDaytime() {
        return false;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0.5F;
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
        Planetoid ground = Planetoid.PLANETOIDS.inverse().get(getDimension());
        if (ground == null) return 0.0f;
        double worldTime = world.getWorldTime() + partialTicks;
        Vec3d localUp = Orbit.getLocalUp(ground, mc.player.posX, mc.player.posZ, worldTime);
        return Orbit.getSunBrightness(ground, localUp, worldTime);
    }

    @Override
    public float getStarBrightness(float partialTicks) {
        Planetoid ground = Planetoid.PLANETOIDS.inverse().get(getDimension());
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
