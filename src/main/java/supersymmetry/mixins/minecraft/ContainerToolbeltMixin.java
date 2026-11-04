package supersymmetry.mixins.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import supersymmetry.common.item.ItemToolbelt;

/**
 * Records which player and slot are currently crafting when the cursor holds a toolbelt,
 * so the ingredient hook can sync the auto-selected tool back to that player.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0).
 */
@Mixin(Container.class)
public abstract class ContainerToolbeltMixin {

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/item/ItemStack;splitStack(I)Lnet/minecraft/item/ItemStack;",
                     ordinal = 1))
    private void susy$setCraftingPlayer(int slotId, int dragType, ClickType clickType, EntityPlayer player,
                                        CallbackInfoReturnable<ItemStack> cir) {
        ItemStack cursor = player.inventory.getItemStack();
        if (player instanceof EntityPlayerMP serverPlayer && cursor.getItem() instanceof ItemToolbelt) {
            ItemToolbelt.setCraftingSlot(slotId, serverPlayer);
        }
    }
}
