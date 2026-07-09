package supersymmetry.api.space.dimension;

import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.DimensionManager;

import supersymmetry.api.SusyLog;
import supersymmetry.common.world.SuSyDimensions;

public class SpaceDimension {

    public final int id;
    public final String name;

    public float gravity = 0.0f;
    public long ticksPerDay = 24000L;
    public float dayLength = 24.0f;
    public float timeOffset = 0.0f;
    public IRenderHandler renderer;
    public float ambientLight = 0.02f;
    public boolean isVacuum = true;

    public SpaceDimension(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public SpaceDimension setRenderer(IRenderHandler renderer) {
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

        // Register with Forge so the dimension actually exists
        if (!DimensionManager.isDimensionRegistered(id)) {
            DimensionManager.registerDimension(id, SuSyDimensions.spaceType);
            SusyLog.logger.info(String.format("Registered space dimension '%s' at id %d", name, id));
        }
    }
}
