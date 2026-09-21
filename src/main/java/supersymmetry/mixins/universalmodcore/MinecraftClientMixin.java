package supersymmetry.mixins.universalmodcore;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cam72cam.mod.MinecraftClient;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "isReady", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onIsReady(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.world != null);
    }
}
