package supersymmetry.api.space;

import supersymmetry.api.SusyLog;
import supersymmetry.api.image.Cubemap;

public class RenderableCelestialObject {

    private final CelestialObject object;
    private final Cubemap cubemap;

    private float angularSizeDeg = 20.0f;
    private long orbitalPeriodTicks = 0L;
    private float orbitalInclinationDeg = 0.0f;
    private long phaseOffsetTicks = 0L;

    private boolean hasFixedDirection = false;
    private float fixedDx, fixedDy, fixedDz;

    private boolean loadAttempted = false;

    public RenderableCelestialObject(CelestialObject object, Cubemap cubemap) {
        this.object = object;
        this.cubemap = cubemap;
    }

    public RenderableCelestialObject setAngularSize(float degrees) {
        this.angularSizeDeg = degrees;
        return this;
    }

    public RenderableCelestialObject setOrbitalPeriod(long ticks) {
        this.orbitalPeriodTicks = ticks;
        return this;
    }

    public RenderableCelestialObject setOrbitalInclination(float degrees) {
        this.orbitalInclinationDeg = degrees;
        return this;
    }

    public RenderableCelestialObject setPhaseOffset(long ticks) {
        this.phaseOffsetTicks = ticks;
        return this;
    }

    public RenderableCelestialObject setFixedDirection(float dx, float dy, float dz) {
        this.hasFixedDirection = true;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        this.fixedDx = dx / len;
        this.fixedDy = dy / len;
        this.fixedDz = dz / len;
        return this;
    }

    public CelestialObject getCelestialObject() {
        return object;
    }

    public Cubemap getCubemap() {
        return cubemap;
    }

    public float getAngularSizeDeg() {
        return angularSizeDeg;
    }

    public boolean ensureLoaded() {
        return ensureLoadedInternal();
    }

    private boolean ensureLoadedInternal() {
        if (loadAttempted) return cubemap.isLoaded();
        loadAttempted = true;
        try {
            cubemap.loadAll();
            SusyLog.logger.info("[Space] Cubemap loaded for " + object.getTranslationKey());
            return true;
        } catch (Exception e) {
            SusyLog.logger.error("[Space] Cubemap FAILED for " + object.getTranslationKey(), e);
            return false;
        }
    }

    public float[] getWorldDirection(long worldTime) {
        if (hasFixedDirection) {
            return new float[] { fixedDx, fixedDy, fixedDz };
        }
        double angle = orbitalPeriodTicks > 0 ?
                ((worldTime + phaseOffsetTicks) % orbitalPeriodTicks) / (double) orbitalPeriodTicks * 2.0 * Math.PI :
                0.0;
        float incRad = (float) Math.toRadians(orbitalInclinationDeg);
        return new float[] {
                (float) Math.cos(angle),
                (float) (Math.sin(angle) * Math.sin(incRad)),
                (float) (Math.sin(angle) * Math.cos(incRad))
        };
    }
}
