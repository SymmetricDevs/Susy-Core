package supersymmetry.mixins.littletiles;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.creativemd.littletiles.client.render.world.LittleRenderChunkSuppilier;

@Mixin(value = LittleRenderChunkSuppilier.class, remap = false)
public abstract class LittleRenderChunkSuppilier_FinalizeMixin {

    @Shadow
    public abstract void unloadRenderCache();

    /**
     * @reason Prevent the garbage collector's finalizer thread from calling OpenGL cleanups
     *         (chunk.unload() -> VertexBuffer.deleteBuffers()) without an active OpenGL context,
     *         which causes a fatal JVM crash on modern Java versions (Java 17/21/25).
     */
    @Inject(method = "finalize", at = @At("HEAD"), cancellable = true, remap = false)
    private void susy$preventAsyncFinalizerCrash(CallbackInfo ci) {
        // Completely cancel the finalizer's attempt to run OpenGL code off-thread.
        // Memory cleanup should be handled by standard world/client unloads on the main thread instead.
        ci.cancel();
    }
}
