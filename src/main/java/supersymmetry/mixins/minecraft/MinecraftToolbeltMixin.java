package supersymmetry.mixins.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import supersymmetry.common.item.ItemToolbelt;

/**
 * Sneak + hotbar key selects a toolbelt slot instead of switching hotbar slots.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0).
 */
@Mixin(Minecraft.class)
public class MinecraftToolbeltMixin {

    @Redirect(method = "processKeyBinds",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I",
                       opcode = Opcodes.PUTFIELD))
    public void susy$toolbeltHotkey(InventoryPlayer inventory, int index) {
        EntityPlayer player = inventory.player;
        if (player != null && player.isSneaking()) {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemToolbelt belt) {
                belt.changeSelectedToolHotkey(index, stack);
                return;
            }
        }
        inventory.currentItem = index;
    }
}
