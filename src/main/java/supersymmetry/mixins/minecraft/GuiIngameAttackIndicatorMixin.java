package supersymmetry.mixins.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.settings.GameSettings;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import supersymmetry.client.renderer.gun.GunClientHandler;

/**
 * Vanilla draws the crosshair and the attack indicator in the same method, and Forge exposes only one
 * {@code CROSSHAIRS} overlay event, so cancelling that event hides both. The indicator below the crosshair is
 * gated by this field read, so hiding only that read keeps the crosshair while dropping a swing meter that says
 * nothing about a gun's readiness.
 */
@Mixin(GuiIngame.class)
public class GuiIngameAttackIndicatorMixin {

    @Redirect(method = "renderAttackIndicator",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/settings/GameSettings;attackIndicator:I",
                       opcode = Opcodes.GETFIELD))
    private int susy$hideGunAttackIndicator(GameSettings settings) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && GunClientHandler.isHoldingGun(mc.player)) {
            return 0;
        }
        return settings.attackIndicator;
    }
}
