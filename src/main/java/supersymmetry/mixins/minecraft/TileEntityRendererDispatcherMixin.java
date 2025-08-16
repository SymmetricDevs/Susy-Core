package supersymmetry.mixins.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supersymmetry.api.util.BlockRenderManager;

@Mixin(TileEntityRendererDispatcher.class)
public class TileEntityRendererDispatcherMixin {

    @Inject(method = "getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;",
            at = @At(value = "HEAD"),
            cancellable = true)
    private <T extends TileEntity> void ignoreBlocked(TileEntity tileEntityIn,
                                                      CallbackInfoReturnable<TileEntitySpecialRenderer<T>> cir) {
        if (tileEntityIn != null) {
            if (tileEntityIn.getWorld() == Minecraft.getMinecraft().world
                    && BlockRenderManager.modelDisabled.contains(tileEntityIn.getPos())) {
                cir.setReturnValue(null);
            }
        }
    }
}
