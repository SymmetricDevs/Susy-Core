package supersymmetry.common.metatileentities.single.electric;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import supersymmetry.api.chisel.ExtendedChiselItem;
import supersymmetry.api.chisel.ExtendedChiselRegistry;
import supersymmetry.api.items.GroupEntry;
import supersymmetry.api.items.ItemGroupFolder;
import supersymmetry.api.items.ItemGroupRegistry;

/**
 * Category hierarchy over the {@link ExtendedChiselRegistry} items, built on top of the reusable
 * {@link ItemGroupRegistry} library.
 *
 * <p>
 * Every {@link ExtendedChiselItem} is linked under <em>each</em> of its Extended Location Identifiers
 * (folder paths) — so the same block appears under every location it belongs to. The GUI grid dedupes
 * by item id, and folder clicks drive the active folder the same way the Marking Maker's browser does.
 * </p>
 *
 * <p>
 * As with the marking tree: the registry is a global singleton and is cleared on rebuild, so the tree
 * is rebuilt each time it is constructed. The hierarchy is side-independent and only reads the runtime
 * registry, which is identical on both server and client.
 * </p>
 */
public final class ExtendedChiselFolderTree {

    /** A GUI-facing folder; wraps a registry {@link ItemGroupFolder}. */
    public static final class FolderNode {

        public final String name;
        @Nullable public final FolderNode parent;
        private final ItemGroupFolder backing;

        private FolderNode(String name, @Nullable FolderNode parent, ItemGroupFolder backing) {
            this.name = name;
            this.parent = parent;
            this.backing = backing;
        }

        public List<FolderNode> getFolderChildren() {
            List<FolderNode> out = new ArrayList<>();
            for (ItemGroupFolder child : backing.getFolderChildren()) {
                out.add(new FolderNode(child.name, this, child));
            }
            return out;
        }

        ItemGroupFolder getBacking() {
            return backing;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FolderNode)) {
                return false;
            }
            return backing == ((FolderNode) o).backing;
        }

        @Override
        public int hashCode() {
            return backing.hashCode();
        }
    }

    /** A single selectable extended chisel item; wraps an {@link ExtendedChiselItem}. */
    public static final class ItemLeaf {

        /** Unique item id, {@code modid:name:meta}. */
        public final String itemId;
        /** Human readable display name of the item. */
        public final String displayName;

        private final ExtendedChiselItem item;

        private ItemLeaf(ExtendedChiselItem item) {
            this.item = item;
            this.itemId = item.getId();
            this.displayName = item.getDisplayName();
        }

        public ExtendedChiselItem getItem() {
            return item;
        }
    }

    private FolderNode root;

    public ExtendedChiselFolderTree() {
        rebuild();
    }

    private void rebuild() {
        // The registry is a global singleton; clear it so repeated construction is deterministic.
        ItemGroupRegistry.clear();
        for (ExtendedChiselItem item : ExtendedChiselRegistry.getAllItems()) {
            for (String location : item.getLocations()) {
                if (location == null || location.isEmpty()) {
                    continue;
                }
                ItemGroupRegistry.link(location, item.getStack(), item);
            }
        }
        this.root = new FolderNode("", null, ItemGroupRegistry.getRoot());
    }

    /** Returns the root node. */
    public FolderNode getRoot() {
        return root;
    }

    /**
     * All distinct items (recursively) under the given folder, deduplicated by item id.
     * Retains link (registration) order. An item linked under several locations within the same
     * subtree is listed once.
     */
    public List<ItemLeaf> getAllItems(FolderNode folder) {
        Map<String, ItemLeaf> deduped = new LinkedHashMap<>();
        List<GroupEntry> entries = ItemGroupRegistry.getAllItems(folder.getBacking());
        for (GroupEntry entry : entries) {
            if (!(entry.info instanceof ExtendedChiselItem)) {
                continue;
            }
            ExtendedChiselItem item = (ExtendedChiselItem) entry.info;
            deduped.putIfAbsent(item.getId(), new ItemLeaf(item));
        }
        return new ArrayList<>(deduped.values());
    }

    /** Resolves the {@code ItemStack} icon for an item leaf. */
    @Nullable public ItemStack getItemStack(ItemLeaf leaf) {
        ItemStack stack = leaf.getItem().getStack();
        return stack.isEmpty() ? null : stack;
    }
}
