package supersymmetry.api.space.dimension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.DimensionManager;

import supersymmetry.api.SusyLog;
import supersymmetry.api.space.CelestialObject;
import supersymmetry.api.space.Orbit;
import supersymmetry.common.world.SuSyDimensions;

public class SpaceDimension {

    private static final Map<Integer, SpaceDimension> SPACE_DIMENSIONS = new HashMap<>();

    public final int id;
    public final String name;

    public float gravity = 0.0f;
    public IRenderHandler renderer;
    public boolean isVacuum = true;

    public CelestialObject centeredOn;
    public Orbit orbit;

    public SpaceDimension(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static SpaceDimension get(int dimId) {
        return SPACE_DIMENSIONS.get(dimId);
    }

    public static Set<Integer> getRegisteredIds() {
        return SPACE_DIMENSIONS.keySet();
    }

    public SpaceDimension setRenderer(IRenderHandler renderer) {
        this.renderer = renderer;
        return this;
    }

    public SpaceDimension setGravity(float g) {
        this.gravity = g;
        return this;
    }

    public SpaceDimension setVacuum(boolean vacuum) {
        this.isVacuum = vacuum;
        return this;
    }

    public SpaceDimension setOrbit(CelestialObject centeredOn, Orbit orbit) {
        this.centeredOn = centeredOn;
        this.orbit = orbit;
        return this;
    }

    public void load() {
        SPACE_DIMENSIONS.put(id, this);

        // Register with Forge so the dimension actually exists
        if (!DimensionManager.isDimensionRegistered(id)) {
            DimensionManager.registerDimension(id, SuSyDimensions.spaceType);
            SusyLog.logger.info(String.format("Registered space dimension '%s' at id %d", name, id));
        }
    }
}
