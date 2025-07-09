package mrtjp.projectred.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IScrewdriver {

    boolean canUse(EntityPlayer player, ItemStack stack);

    void damageScrewdriver(EntityPlayer player, ItemStack stack); // Damage the item on usage
}
