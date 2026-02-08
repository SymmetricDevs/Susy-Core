package supersymmetry.mixins.minecraft;

import cam72cam.mod.entity.ModdedEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.client.renderer.handler.IAlwaysRender;

@Mixin(net.minecraft.client.renderer.RenderGlobal.class)
public class RenderGlobalMixin {
    @WrapOperation(method = "renderEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z")
        )
    public boolean checkForAlwaysRender(Entity entityIn, ICamera camera, double camX, double camY, double camZ, Operation<Boolean> shouldRender) {
        if (entityIn instanceof IAlwaysRender) {
            return true;
        }
        if (entityIn instanceof ModdedEntity entity) {
            if (entity.getSelf() instanceof IAlwaysRender) {
                return true;
            }
        }
        return shouldRender.call(entityIn, camera, camX, camY, camZ);
    }
}
