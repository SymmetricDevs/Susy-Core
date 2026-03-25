package supersymmetry.mixins.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFirework.class)
public abstract class EntityElytraMixin {

    @Inject(
            method = "onItemRightClick",
            at = @At(
                     value = "INVOKE",
                     target = "Lnet/minecraft/entity/item/EntityFireworkRocket;<init>(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;)V"),
            cancellable = true)
    public void onUse(World world, EntityPlayer player, EnumHand hand, CallbackInfoReturnable<ItemStack> cir) {
        if (player.isElytraFlying()) {
            ItemStack stack = player.getHeldItem(hand);

            // Consume item if not in creative
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }

            // Cancel the original firework launch
            cir.setReturnValue(stack);

        }
    }
}
