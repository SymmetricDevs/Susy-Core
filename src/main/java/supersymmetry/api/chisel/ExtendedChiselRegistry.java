package supersymmetry.api.chisel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import supersymmetry.api.SusyLog;

/**
 * Registry of every {@link ExtendedChiselGroup} and every {@link ExtendedChiselItem}.
 *
 * <p>
 * Items are unique per registered id + meta. Adding the same stack to several groups shares one item
 * that owns all those groups, which is what lets the machine accept a craft whenever the input and the
 * selected output share <em>any</em> common group.
 * </p>
 */
public final class ExtendedChiselRegistry {

    private static final Map<String, ExtendedChiselGroup> GROUPS = new LinkedHashMap<>();
    private static final Map<String, ExtendedChiselItem> ITEMS = new LinkedHashMap<>();

    private ExtendedChiselRegistry() {}

    /**
     * Creates a group with a unique name. Logs an error and returns {@code null} if the name is empty
     * or already taken.
     */
    @Nullable public static ExtendedChiselGroup createGroup(String name) {
        return createGroup(name, false);
    }

    @Nullable static ExtendedChiselGroup createGroup(String name, boolean autoConverted) {
        if (name == null || name.isEmpty()) {
            SusyLog.logger.error("[ExtendedChisel] group name must not be empty");
            return null;
        }
        ExtendedChiselGroup existing = GROUPS.get(name);
        if (existing != null) {
            SusyLog.logger.error("[ExtendedChisel] group '{}' already exists; not re-creating it", name);
            return null;
        }
        ExtendedChiselGroup group = new ExtendedChiselGroup(name, autoConverted);
        GROUPS.put(name, group);
        return group;
    }

    /** All groups, in creation order. */
    public static List<ExtendedChiselGroup> getAllGroups() {
        return Collections.unmodifiableList(new ArrayList<>(GROUPS.values()));
    }

    /** A group by name, or {@code null}. */
    @Nullable public static ExtendedChiselGroup getGroup(String name) {
        return GROUPS.get(name);
    }

    /**
     * Adds a stack to an existing group (recipe membership only — this does <em>not</em> assign any
     * location). Placement in the folder browser is handled separately via
     * {@link #addLocations(ItemStack, String...)}. The group must be created first via
     * {@link #createGroup(String)}. Shared stacks deduplicate into one {@link ExtendedChiselItem}.
     */
    public static void addItem(String groupName, ItemStack stack) {
        ExtendedChiselGroup group = GROUPS.get(groupName);
        if (group == null) {
            SusyLog.logger.error("[ExtendedChisel] no group '{}' found; create it with createGroup(...) first",
                    groupName);
            return;
        }
        if (stack == null || stack.isEmpty()) {
            SusyLog.logger.error("[ExtendedChisel] refused to add an empty stack to group '{}'", groupName);
            return;
        }
        ExtendedChiselItem item = ITEMS.get(stackId(stack));
        if (item == null) {
            item = new ExtendedChiselItem(stack);
            ITEMS.put(item.getId(), item);
        }
        item.addOwner(group);
        group.addItem(item);
    }

    /** Adds many stacks to a group (recipe membership only). */
    public static void addItems(String groupName, Iterable<ItemStack> stacks) {
        if (stacks == null) {
            return;
        }
        for (ItemStack stack : stacks) {
            addItem(groupName, stack);
        }
    }

    /**
     * Registers an item (if it is not yet known) and merges the given location identifiers into it,
     * independent of any group. Locations only decide where the item appears in the folder browser;
     * recipes come from {@link #addItem(String, ItemStack)} group membership.
     */
    public static void addLocations(ItemStack stack, String... locations) {
        if (stack == null || stack.isEmpty()) {
            SusyLog.logger.error("[ExtendedChisel] refused to add locations to an empty stack");
            return;
        }
        ExtendedChiselItem item = ITEMS.get(stackId(stack));
        if (item == null) {
            item = new ExtendedChiselItem(stack);
            ITEMS.put(item.getId(), item);
        }
        if (locations != null) {
            for (String loc : locations) {
                item.addLocation(loc);
            }
        }
    }

    /** Resolves an item by its unique id ({@code modid:name:meta}), or {@code null}. */
    @Nullable public static ExtendedChiselItem resolve(String id) {
        return ITEMS.get(id);
    }

    /** Every registered item (group members and location-only items alike), in registration order. */
    public static List<ExtendedChiselItem> getAllItems() {
        return Collections.unmodifiableList(new ArrayList<>(ITEMS.values()));
    }

    /** The groups the given stack belongs to (empty if it is not part of any group). */
    public static Set<ExtendedChiselGroup> getGroupsFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Collections.emptySet();
        }
        ExtendedChiselItem item = ITEMS.get(stackId(stack));
        return item == null ? Collections.emptySet() : item.getGroups();
    }

    /**
     * The single recipe rule of the machine: crafting is allowed iff the input and the selected output
     * share at least one common {@link ExtendedChiselGroup}.
     */
    public static boolean areCompatible(ItemStack input, ItemStack selected) {
        Set<ExtendedChiselGroup> a = getGroupsFor(input);
        Set<ExtendedChiselGroup> b = getGroupsFor(selected);
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        for (ExtendedChiselGroup g : a) {
            if (b.contains(g)) {
                return true;
            }
        }
        return false;
    }

    private static String stackId(ItemStack stack) {
        ResourceLocation rl = stack.getItem().getRegistryName();
        return (rl == null ? "unknown:unknown" : rl.toString()) + ":" + stack.getMetadata();
    }
}
