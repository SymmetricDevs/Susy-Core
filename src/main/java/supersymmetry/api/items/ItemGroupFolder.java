package supersymmetry.api.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * A single node in a reusable, hierarchical item-group registry.
 *
 * <p>
 * Groups are addressed by slash-separated ids (e.g. {@code set1}, {@code set1/set1.1},
 * {@code set1/set1.1/set1.1.1}); each id component forms a nested {@link ItemGroupFolder}, so any
 * number of arbitrarily-related items can be linked into the <em>same</em> folder while still
 * belonging to a shared hierarchy. This is the generic, project-independent core of the tree logic
 * used by the machine markings; future projects register their own groups and link items to them
 * without reimplementing nesting or traversal.
 * </p>
 */
public final class ItemGroupFolder {

    /** The last path component, e.g. {@code set1.1}. */
    public final String name;

    /** The full slash-separated id, e.g. {@code set1/set1.1}. */
    public final String fullId;

    /** @Nullable parent folder, or {@code null} for the root. */
    @Nullable public final ItemGroupFolder parent;

    private final Map<String, ItemGroupFolder> children = new LinkedHashMap<>();
    private final List<GroupEntry> entries = new ArrayList<>();

    ItemGroupFolder(String name, String fullId, @Nullable ItemGroupFolder parent) {
        this.name = name;
        this.fullId = fullId;
        this.parent = parent;
    }

    /** Child folders, sorted by name. */
    public List<ItemGroupFolder> getFolderChildren() {
        List<ItemGroupFolder> out = new ArrayList<>(children.values());
        Collections.sort(out, (a, b) -> a.name.compareToIgnoreCase(b.name));
        return out;
    }

    /** Items linked directly to this folder. */
    public List<GroupEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    boolean addChild(ItemGroupFolder child) {
        return children.putIfAbsent(child.name, child) == null;
    }

    void addEntry(GroupEntry entry) {
        entries.add(entry);
    }

    void clear() {
        children.clear();
        entries.clear();
    }
}
