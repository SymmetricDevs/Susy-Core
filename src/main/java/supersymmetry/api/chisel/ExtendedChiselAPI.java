package supersymmetry.api.chisel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import supersymmetry.api.SusyLog;
import team.chisel.api.carving.CarvingUtils;
import team.chisel.api.carving.ICarvingGroup;
import team.chisel.api.carving.ICarvingRegistry;
import team.chisel.api.carving.ICarvingVariation;

/**
 * Groovy-facing static API for building {@link ExtendedChiselGroup}s and {@link ExtendedChiselItem}s.
 * Import the class statically in a script, e.g.:
 *
 * <pre>
 * import static supersymmetry.api.chisel.ExtendedChiselAPI.*
 *
 * ExtendedChiselGroup group = createExtendedChiselGroup("susy.test");
 * addItemToGroup(group.getName(), item('chisel:glass', 0))   // recipe membership
 * addItemToGroup(group.getName(), item('minecraft:glass'))
 * addLocationsToItem(item('chisel:glass', 0), "chisel/glass/Glass")   // placement only
 * </pre>
 *
 * <p>
 * Locations and groups are independent: a group <em>is</em> the recipe map (any member converts into
 * any other member), while location identifiers only decide where an item appears in the machine's
 * folder browser (see {@link #addLocationsToItem(ItemStack, String...)}).
 * </p>
 *
 * <p>
 * Existing Chisel mod groups can be converted wholesale (best-effort, non-destructive to the Chisel
 * registry) with {@link #convertChiselGroups()}, optionally excluding some by name via
 * {@link #excludeChiselGroups(String...)}. Converted groups are marked as auto-converted and place
 * their items directly under the {@code chisel/{group}} folder; the GUI tooltip resolves each item's
 * Chisel variation name from its blockstate and the Chisel lang files.
 * </p>
 */
public final class ExtendedChiselAPI {

    private static final Set<String> EXCLUDED_CHISEL_GROUPS = new LinkedHashSet<>();

    private ExtendedChiselAPI() {}

    /**
     * Returns the name of every group currently registered in the Chisel mod, or an empty list if it is not installed.
     */
    public static List<String> getAllChiselGroups() {
        List<String> names = new ArrayList<>();
        ICarvingRegistry registry = chiselRegistry();
        if (registry != null) {
            names.addAll(registry.getSortedGroupNames());
        }
        return names;
    }

    /** Prevents {@link #convertChiselGroups()} from converting the given Chisel group names. */
    public static void excludeChiselGroups(String... groupNames) {
        if (groupNames != null) {
            for (String name : groupNames) {
                EXCLUDED_CHISEL_GROUPS.add(name);
            }
        }
    }

    /** The Chisel group names currently excluded from auto-conversion. */
    public static List<String> getExcludedChiselGroups() {
        return new ArrayList<>(EXCLUDED_CHISEL_GROUPS);
    }

    /**
     * Converts every Chisel mod group into an equivalent {@link ExtendedChiselGroup} (skipping
     * excluded names and ones that are already registered). Each converted item is placed directly in
     * the {@code chisel/{group}} folder; tooltips resolve the Chisel variation name from the item's
     * blockstate and the Chisel lang files.
     */
    public static void convertChiselGroups() {
        ICarvingRegistry registry = chiselRegistry();
        if (registry == null) {
            SusyLog.logger.warn("[ExtendedChisel] Chisel mod is not loaded; convertChiselGroups() did nothing");
            return;
        }
        int groups = 0;
        int items = 0;
        for (String name : registry.getSortedGroupNames()) {
            if (EXCLUDED_CHISEL_GROUPS.contains(name) || ExtendedChiselRegistry.getGroup(name) != null) {
                continue;
            }
            ExtendedChiselGroup group = ExtendedChiselRegistry.createGroup(name, true);
            if (group == null) {
                continue;
            }
            ICarvingGroup chiselGroup = registry.getGroup(name);
            if (chiselGroup == null) {
                continue;
            }
            for (ICarvingVariation variation : chiselGroup.getVariations()) {
                ItemStack stack = variation.getStack();
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                ExtendedChiselRegistry.addItem(name, stack);
                ExtendedChiselRegistry.addLocations(stack, "chisel/" + name);
                items++;
            }
            groups++;
        }
        SusyLog.logger.info("[ExtendedChisel] auto-converted {} chisel groups ({} items)", groups, items);
    }

    /** Creates a new, uniquely named extended group. Returns the group. */
    public static ExtendedChiselGroup createExtendedChiselGroup(String name) {
        return ExtendedChiselRegistry.createGroup(name, false);
    }

    /** Adds an item to an existing extended group (recipe membership only, no locations). */
    public static void addItemToGroup(String groupName, ItemStack stack) {
        ExtendedChiselRegistry.addItem(groupName, stack);
    }

    /** Adds many items to an existing extended group (recipe membership only, no locations). */
    public static void addItemsToGroup(String groupName, ItemStack[] stacks) {
        if (stacks == null) {
            return;
        }
        for (ItemStack stack : stacks) {
            addItemToGroup(groupName, stack);
        }
    }

    /**
     * Adds Extended Location Identifiers to an item, independent of any group. Locations only decide
     * where the block appears in the machine's folder browser (and what its search text is); recipes
     * come exclusively from group membership. The item is registered on first use even if no group
     * owns it yet, and locations keep merging across calls.
     */
    public static void addLocationsToItem(ItemStack stack, String... locations) {
        ExtendedChiselRegistry.addLocations(stack, locations);
    }

    /** Single-location convenience overload (Groovy binds exact-arity calls more reliably than varargs). */
    public static void addLocationsToItem(ItemStack stack, String location) {
        ExtendedChiselRegistry.addLocations(stack, location);
    }

    /** Two-location convenience overload. */
    public static void addLocationsToItem(ItemStack stack, String location1, String location2) {
        ExtendedChiselRegistry.addLocations(stack, location1, location2);
    }

    /** List-location convenience overload. */
    public static void addLocationsToItem(ItemStack stack, List<String> locations) {
        ExtendedChiselRegistry.addLocations(stack,
                locations == null ? new String[0] : locations.toArray(new String[0]));
    }

    /** Prevents {@link #convertChiselGroups()} from converting a single Chisel group (exact-arity overload). */
    public static void excludeChiselGroups(String groupName) {
        excludeChiselGroups(new String[] { groupName });
    }

    /** The names of every registered extended group. */
    public static List<String> getAllExtendedGroups() {
        List<String> names = new ArrayList<>();
        for (ExtendedChiselGroup group : ExtendedChiselRegistry.getAllGroups()) {
            names.add(group.getName());
        }
        return names;
    }

    private static ICarvingRegistry chiselRegistry() {
        if (!Loader.isModLoaded("chisel")) {
            return null;
        }
        return CarvingUtils.getChiselRegistry();
    }
}
