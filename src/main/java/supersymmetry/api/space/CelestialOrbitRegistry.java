package supersymmetry.api.space;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CelestialOrbitRegistry {

    private static final Map<CelestialObject, Orbit> ORBITS = new HashMap<>();

    public static void register(CelestialObject body, Orbit orbit) {
        ORBITS.put(body, orbit);
    }

    public static Orbit get(CelestialObject body) {
        return ORBITS.get(body);
    }

    public static Map<CelestialObject, Orbit> getAll() {
        return Collections.unmodifiableMap(ORBITS);
    }
}
