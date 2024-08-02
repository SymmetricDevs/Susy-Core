package supersymmetry.api.items;

import gregtech.api.items.armor.IArmorLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IBreathingArmorLogic extends IArmorLogic {

    boolean isValid(ItemStack stack, int dimension);


    boolean tryTick(ItemStack stack, EntityPlayer player, int dimension);
}
