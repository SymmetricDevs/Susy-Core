package supersymmetry.common.item.behavior;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import baubles.api.BaubleType;
import gregtech.integration.baubles.BaubleBehavior;

public class ArmorBaubleBehavior extends BaubleBehavior {

    public ArmorBaubleBehavior(BaubleType baubleType) {
        super(baubleType);
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase player) {
        if (stack != null && stack != ItemStack.EMPTY && player instanceof EntityPlayer entityPlayer) {
            stack.getItem().onArmorTick(player.getEntityWorld(), entityPlayer, stack); // Redirects onWornTick() to
                                                                                       // onArmorTick()
        }
    }
}
