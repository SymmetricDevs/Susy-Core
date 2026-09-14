package supersymmetry.api.items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

/**
 * Reusable registry of hierarchical item groups.
 *
 * <p>
 * This is the generic, project-independent "library" half of the folder browser: it parses
 * slash-separated group ids (e.g. {@code set1/set1.1/set1.1.1}) into a nested {@link ItemGroupFolder}
 * tree and lets you link as many <em>arbitrarily-related</em> items as you want into any single
 * folder — cohesion is not required. The same registry backs future projects, which only have to
 * {@link #register(String)} their group ids, {@link #link(String, ItemStack, Object)} items to them,
 * and then walk the tree.
 * </p>
 *
 * <p>
 * Items are identified by registry id <em>plus</em> metadata: 1.12.2 distinguishes many item variants
 * (dyes, colored blocks, meta-based markings) purely through {@link ItemStack#getMetadata()}, so a
 * linked entry always remembers its meta and resolves its stack with it.
 * </p>
 */
public final class ItemGroupRegistry {

    private static final ItemGroupFolder ROOT = new ItemGroupFolder("", "", null);

    private ItemGroupRegistry() {}

    /**
     * Registers a group id, creating any missing intermediate folders. The id may be a plain single
     * segment ({@code set1}) or a nested path ({@code set1/set1.1/set1.1.1}). Returns the folder for
     * the given id, creating it if needed.
     */
    public static ItemGroupFolder register(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            return ROOT;
        }
        ItemGroupFolder node = ROOT;
        StringBuilder full = new StringBuilder();
        for (String part : groupId.split("/")) {
            if (part.isEmpty()) {
                continue;
            }
            if (full.length() > 0) {
                full.append('/');
            }
            full.append(part);
            ItemGroupFolder parent = node;
            node = childOrCreate(parent, part, full.toString());
        }
        return node;
    }

    private static ItemGroupFolder childOrCreate(ItemGroupFolder parent, String name, String fullId) {
        for (ItemGroupFolder child : parent.getFolderChildren()) {
            if (child.name.equalsIgnoreCase(name)) {
                return child;
            }
        }
        ItemGroupFolder child = new ItemGroupFolder(name, fullId, parent);
        parent.addChild(child);
        return child;
    }

    /**
     * Links an item (by an {@link ItemStack}) into the given group id, preserving its meta. The group
     * id folders are created on demand.
     */
    public static GroupEntry link(String groupId, ItemStack stack, @Nullable Object info) {
        String itemId = stack == null ? "" : stack.getItem().getRegistryName().toString();
        int meta = stack == null ? 0 : stack.getMetadata();
        GroupEntry entry = new GroupEntry(itemId, meta, stack == null ? ItemStack.EMPTY : stack, info);
        register(groupId).addEntry(entry);
        return entry;
    }

    /**
     * Links an item (by its {@link Item}) into the given group id with the given meta. A stack is
     * derived with a count of 1 and that meta.
     */
    public static GroupEntry link(String groupId, Item item, int meta, @Nullable Object info) {
        ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item, 1, meta);
        String itemId = item == null ? "" : item.getRegistryName().toString();
        GroupEntry entry = new GroupEntry(itemId, meta, stack, info);
        register(groupId).addEntry(entry);
        return entry;
    }

    /**
     * Links an item by its registry id ({@code modid:name}) and meta. The stack is resolved from the
     * item registry, or {@link ItemStack#EMPTY} if the id cannot be resolved.
     */
    public static GroupEntry link(String groupId, String itemId, int meta, @Nullable Object info) {
        Item item = resolveItem(itemId);
        ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item, 1, meta);
        GroupEntry entry = new GroupEntry(itemId, meta, stack, info);
        register(groupId).addEntry(entry);
        return entry;
    }

    @Nullable private static Item resolveItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return null;
        }
        ResourceLocation rl;
        try {
            rl = new ResourceLocation(itemId);
        } catch (Exception e) {
            return null;
        }
        return Item.REGISTRY.getObject(rl);
    }

    /** The root folder of the whole hierarchy. */
    public static ItemGroupFolder getRoot() {
        return ROOT;
    }

    /** Clears the entire registry, dropping all folders and linked items. */
    public static void clear() {
        ROOT.clear();
    }

    /** Child folders of a folder, sorted by name. */
    public static List<ItemGroupFolder> getFolderChildren(ItemGroupFolder folder) {
        return folder == null ? new ArrayList<>() : folder.getFolderChildren();
    }

    /** Items linked directly to a folder. */
    public static List<GroupEntry> getItems(ItemGroupFolder folder) {
        return folder == null ? new ArrayList<>() : folder.getEntries();
    }

    /** All items linked, recursively, under a folder (in link order). */
    public static List<GroupEntry> getAllItems(ItemGroupFolder folder) {
        List<GroupEntry> out = new ArrayList<>();
        if (folder == null) {
            return out;
        }
        collect(folder, out);
        return out;
    }

    private static void collect(ItemGroupFolder folder, List<GroupEntry> out) {
        out.addAll(folder.getEntries());
        for (ItemGroupFolder child : folder.getFolderChildren()) {
            collect(child, out);
        }
    }
}
