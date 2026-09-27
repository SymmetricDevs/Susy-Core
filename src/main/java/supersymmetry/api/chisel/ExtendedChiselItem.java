package supersymmetry.api.chisel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * A single block/item member of one or more {@link ExtendedChiselGroup}s.
 *
 * <p>
 * An item keeps an arbitrary number of <em>Extended Location Identifier</em> folder paths. These are
 * used by the machine GUI both to organize the folder tree (the item appears under <em>every</em> of
 * its locations) and to search: the GUI searches the registered id together with the whole of every
 * location path.
 * </p>
 *
 * <p>
 * The item is unique per registry id + meta ({@link #getId()}, e.g. {@code susy:structural_block:3}),
 * so the same block added to several groups is one item owning several groups.
 * </p>
 */
public final class ExtendedChiselItem {

    private final ItemStack stack;
    private final String itemId;
    private final int meta;
    private final String id;
    private final String displayName;
    private final Set<String> locations = new LinkedHashSet<>();
    private final Set<ExtendedChiselGroup> owners = new LinkedHashSet<>();
    private String searchableText;

    ExtendedChiselItem(ItemStack stack) {
        ItemStack s = stack.copy();
        s.setCount(1);
        this.stack = s;
        ResourceLocation rl = stack.getItem().getRegistryName();
        this.itemId = rl == null ? "unknown:unknown" : rl.toString();
        this.meta = stack.getMetadata();
        this.id = itemId + ":" + meta;
        this.displayName = stack.getDisplayName();
    }

    /** A copy of the item's stack (count 1). */
    public ItemStack getStack() {
        return stack.copy();
    }

    /** The registered item id, {@code modid:name}. */
    public String getItemId() {
        return itemId;
    }

    /** The item's metadata/damage value. */
    public int getMeta() {
        return meta;
    }

    /** Unique item key {@code modid:name:meta} (the identity used for dedupe, selection and crafting). */
    public String getId() {
        return id;
    }

    /** The stack's display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Adds a location folder path (slashes allowed), deduplicated. */
    public void addLocation(String location) {
        if (location != null && !location.isEmpty()) {
            locations.add(location);
        }
    }

    /** All location folder paths of this item. */
    public List<String> getLocations() {
        return Collections.unmodifiableList(new ArrayList<>(locations));
    }

    /**
     * The lowercase text the machine GUI searches: the registered id plus every location path. Cached
     * after the first call.
     */
    public String getSearchableText() {
        if (searchableText == null) {
            StringBuilder sb = new StringBuilder(itemId.toLowerCase(Locale.ROOT));
            for (String loc : locations) {
                sb.append(' ').append(loc.toLowerCase(Locale.ROOT));
            }
            searchableText = sb.toString();
        }
        return searchableText;
    }

    void addOwner(ExtendedChiselGroup group) {
        owners.add(group);
    }

    /** The groups this item belongs to. */
    public Set<ExtendedChiselGroup> getGroups() {
        return Collections.unmodifiableSet(owners);
    }

    @Override
    public String toString() {
        return id;
    }
}
