package supersymmetry.api.items;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * A single item linked into an {@link ItemGroupFolder}.
 *
 * <p>
 * The entry stores the item's registry id (e.g. {@code gtadditions:machine_casing}) together with its
 * metadata/damage value, so it can be referenced and looked up independently of a concrete stack,
 * plus an {@link ItemStack} of that item (used for rendering/lookup) and an optional,
 * project-specific {@code info} payload for any extra metadata a consumer wants to attach.
 * </p>
 */
public final class GroupEntry {

    /** The item's registry id (usually {@code modid:name}). */
    public final String itemId;

    /** The item's metadata/damage value. */
    public final int meta;

    /** An {@link ItemStack} of the item (with {@link #meta}), or {@link ItemStack#EMPTY} if unresolved. */
    public final ItemStack stack;

    /** Optional project-specific metadata, or {@code null}. */
    @Nullable public final Object info;

    public GroupEntry(String itemId, int meta, ItemStack stack, @Nullable Object info) {
        this.itemId = itemId;
        this.meta = meta;
        this.stack = stack == null ? ItemStack.EMPTY : stack;
        this.info = info;
    }
}
