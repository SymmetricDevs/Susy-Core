package supersymmetry.api.rocketry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public interface WeightedBlock<T extends Enum<T> & IStringSerializable> {

    double getMass(T type);

    default double getMass(IBlockState state) {
        return getMass(getState(state));
    }

    default double getMass(ItemStack stack) {
        return getMass(getState(stack));
    }

    T getState(IBlockState blockState);

    T getState(ItemStack stack);
}
