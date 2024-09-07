package supersymmetry.api.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IBreathingItem {
    boolean isValid(ItemStack stack, int dimension);

    double tryTick(ItemStack stack, EntityPlayer player, int dimension);
}
