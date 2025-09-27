package supersymmetry.api.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public class VariantItemBlockFalling<R extends Enum<R> & IStringSerializable, T extends VariantBlockFalling<R>>
                                    extends ItemBlock {

    private final T genericBlock;

    public VariantItemBlockFalling(T block) {
        super(block);
        this.genericBlock = block;
        this.setHasSubtypes(true);
    }

    public int getMetadata(int damage) {
        return damage;
    }

    public IBlockState getBlockState(ItemStack stack) {
        return this.block.getStateFromMeta(this.getMetadata(stack.getItemDamage()));
    }

    public @NotNull String getTranslationKey(@NotNull ItemStack stack) {
        return super.getTranslationKey(stack) + '.' +
                ((IStringSerializable) this.genericBlock.getState(this.getBlockState(stack))).getName();
    }
}
