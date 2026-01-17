package supersymmetry.api.recycling;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import gregtech.api.unification.stack.ItemAndMetadata;

// TODO: wildcard support?
public record ItemLikeRecyclable(ItemAndMetadata itemAndMetadata) implements Recyclable {

    public ItemLikeRecyclable(ItemStack itemStack) {
        this(new ItemAndMetadata(itemStack.copy()));
    }

    public ItemLikeRecyclable(Item item, int damage) {
        this(new ItemAndMetadata(item, damage));
    }

    public ItemLikeRecyclable(Item item) {
        this(item, 0);
    }

    public ItemLikeRecyclable(Block block) {
        this(new ItemAndMetadata(new ItemStack(block)));
    }

    @Override
    public int hashCode() {
        return itemAndMetadata.hashCode();
    }

    @Override
    public ItemStack asStack(int size) {
        return itemAndMetadata.toItemStack(size);
    }

    @Override
    public String toString() {
        return String.format("I[%s]", itemAndMetadata);
    }
}
