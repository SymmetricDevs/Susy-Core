package supersymmetry.api.items;

import gregtech.api.items.armor.IArmorLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IBreathingArmorLogic extends IArmorLogic {

    boolean mayBreatheWith(ItemStack stack, int dimension);


    double tryTick(ItemStack stack, EntityPlayer player, int dimension);
}
