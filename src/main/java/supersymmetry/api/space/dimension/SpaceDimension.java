package supersymmetry.api.space.dimension;

import supersymmetry.api.space.RenderableCelestialObject;
import supersymmetry.common.world.SuSyDimensions;

public class SpaceDimension {

    public final int id;
    public final String name;

    public RenderableCelestialObject objectOrbiting;
    public RenderableCelestialObject[] visibleObjects;
    public float gravity = 0.0f;
    public long ticksPerDay = 24000L;
    public float dayLength = 24.0f;
    public float timeOffset = 0.0f;
    public SuSySpaceRenderer renderer;

    /** Ambient light level in space (0.0–1.0). Default near-zero (no atmosphere). */
    public float ambientLight = 0.02f;

    /** Whether the player should have vacuum damage applied */
    public boolean isVacuum = true;

    public SpaceDimension(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public SpaceDimension setCelestialObjects(RenderableCelestialObject... objs) {
        this.visibleObjects = objs;
        return this;
    }

    public SpaceDimension setOrbitTarget(RenderableCelestialObject obj) {
        this.objectOrbiting = obj;
        return this;
    }

    public SpaceDimension setRenderer(SuSySpaceRenderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public SpaceDimension setGravity(float g) {
        this.gravity = g;
        return this;
    }

    public SpaceDimension setAmbientLight(float light) {
        this.ambientLight = light;
        return this;
    }

    public SpaceDimension setVacuum(boolean vacuum) {
        this.isVacuum = vacuum;
        return this;
    }

    public SpaceDimension setDayCycle(long ticks, float length, float offset) {
        this.ticksPerDay = ticks;
        this.dayLength = length;
        this.timeOffset = offset;
        return this;
    }

    public void load() {
        SuSyDimensions.SPACE.put(id, this);
    }
}
