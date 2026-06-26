package supersymmetry.api.rocketry;


import gregtech.api.block.VariantBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @SideOnly(Side.CLIENT)
    default void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        tooltip.add(I18n.format("susy.tooltip.mass", getMass(stack)));
    }

}
