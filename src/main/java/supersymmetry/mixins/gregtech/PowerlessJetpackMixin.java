package supersymmetry.mixins.gregtech;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.common.items.armor.PowerlessJetpack;
import supersymmetry.common.world.atmosphere.AtmosphereUtils;

@Mixin(value = PowerlessJetpack.class, remap = false)
public abstract class PowerlessJetpackMixin {

    @Inject(method = "onArmorTick", at = @At("HEAD"), cancellable = true)
    private void susy$disableWithoutOxygen(World world, EntityPlayer player, ItemStack stack, CallbackInfo ci) {
        if (!AtmosphereUtils.isPosOxygenated(player.getPosition(), world)) {
            ci.cancel();
        }
    }
}
