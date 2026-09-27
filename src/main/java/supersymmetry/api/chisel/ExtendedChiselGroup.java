package supersymmetry.api.chisel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A named, uniquely identified group of interchangeable blocks.
 *
 * <p>
 * The group <em>is</em> the machine's recipe map: any item of the group can craft any (selected) item
 * of the same group, and nothing outside the group. A group created from an existing Chisel mod group
 * is flagged {@link #isAutoConverted()}.
 * </p>
 */
public final class ExtendedChiselGroup {

    private final String name;
    private final boolean autoConverted;
    private final Map<String, ExtendedChiselItem> items = new LinkedHashMap<>();

    ExtendedChiselGroup(String name, boolean autoConverted) {
        this.name = name;
        this.autoConverted = autoConverted;
    }

    /** The unique group name/identifier. */
    public String getName() {
        return name;
    }

    /** Whether this group was auto-created from an existing Chisel mod group. */
    public boolean isAutoConverted() {
        return autoConverted;
    }

    void addItem(ExtendedChiselItem item) {
        items.putIfAbsent(item.getId(), item);
    }

    /** Whether the given item is part of this group. */
    public boolean contains(ExtendedChiselItem item) {
        return item != null && items.containsKey(item.getId());
    }

    /** The group's items, in insertion order. */
    public List<ExtendedChiselItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    public int getItemCount() {
        return items.size();
    }

    @Override
    public String toString() {
        return "ExtendedChiselGroup{" + name + '}';
    }
}
