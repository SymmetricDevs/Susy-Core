package supersymmetry.mixins.rs_ctr;

import cd4017be.rs_ctr.render.WireRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;



@Mixin(value = WireRenderer.class)
public class WireRendererMixin {
    /**
     * @author XnossisX (aka Epix7)
     * @reason Optifine shaders were breaking because of this call, and it will be fixed later.
     */
    @Overwrite(remap = false)
    public static void drawLine(BufferBuilder b, float[] v, float x, float y, float z,
                                 int l0, int l1, int c) {
        return;
    }
}
