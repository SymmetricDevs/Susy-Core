package supersymmetry.mixins.gregtech;

import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import codechicken.lib.lighting.LightMatrix;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.client.renderer.pipe.PipeRenderer.PipeRenderContext;
import supersymmetry.api.pipelike.CustomContext;

@Mixin(value = PipeRenderer.class, remap = false)
public abstract class PipeRendererMixin implements CustomContext {

    @Redirect(method = "renderItem",
              at = @At(value = "NEW", target = "gregtech/client/renderer/pipe/PipeRenderer$PipeRenderContext"))
    private PipeRenderContext createCustomItemRenderContext(int connections, int blockedConnections, float thickness) {
        return createItemRenderContext(connections, blockedConnections, thickness);
    }

    @Redirect(method = "renderBlock",
              at = @At(value = "NEW", target = "gregtech/client/renderer/pipe/PipeRenderer$PipeRenderContext"))
    private PipeRenderContext createCustomRenderContext(BlockPos pos, LightMatrix lightMatrix, int connections,
                                                        int blockedConnections, float thickness) {
        return createRenderContext(pos, lightMatrix, connections, blockedConnections, thickness);
    }
}
