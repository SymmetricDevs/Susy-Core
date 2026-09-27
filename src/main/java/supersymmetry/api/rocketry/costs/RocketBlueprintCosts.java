package supersymmetry.api.rocketry.costs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed material costs, keyed by blueprint name.
 * <p>
 * Deliberately not locked the way
 * {@link supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint}'s registry
 * is. Blueprints are registered during {@code FMLPostInitializationEvent}, but
 * the packs that fill this in run their scripts in the same phase — with no
 * guaranteed ordering — and again on every {@code /grs reload} long after the
 * world is up. So costs are looked up lazily, by name, at the moment assembly
 * starts, and nothing here is ever written into blueprint NBT. Edit a script,
 * reload, and every blueprint in every chest picks up the new bill of materials.
 * <p>
 * Plain static state on purpose: GroovyScript is the expected caller but not the
 * required one.
 */
public final class RocketBlueprintCosts {

    private static final Map<String, List<RocketCostGroup>> COSTS = new LinkedHashMap<>();

    private RocketBlueprintCosts() {}

    public static void add(String blueprintName, RocketCostGroup group) {
        COSTS.computeIfAbsent(blueprintName, k -> new ArrayList<>()).add(group);
    }

    public static boolean remove(String blueprintName, RocketCostGroup group) {
        List<RocketCostGroup> groups = COSTS.get(blueprintName);
        return groups != null && groups.remove(group);
    }

    public static void clear(String blueprintName) {
        COSTS.remove(blueprintName);
    }

    public static void clearAll() {
        COSTS.clear();
    }

    /** Never null; an unknown blueprint simply has no fixed costs. */
    public static List<RocketCostGroup> get(String blueprintName) {
        List<RocketCostGroup> groups = COSTS.get(blueprintName);
        return groups == null ? Collections.emptyList() : Collections.unmodifiableList(groups);
    }

    public static Map<String, List<RocketCostGroup>> getAll() {
        return Collections.unmodifiableMap(COSTS);
    }
}
