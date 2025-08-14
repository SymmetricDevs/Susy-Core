package supersymmetry.mixins.minecraft;

import cam72cam.mod.entity.ModdedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import supersymmetry.client.renderer.handler.IAlwaysRender;

import java.util.List;

@Mixin(net.minecraft.client.renderer.RenderGlobal.class)
public class RenderGlobalMixin {
    @Shadow
    private WorldClient world;
    @Shadow
    private RenderManager renderManager;
    @Shadow
    private Minecraft mc;

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos$PooledMutableBlockPos;release()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void forceRenderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci, int pass, double d0, double d1, double d2, Entity ignored, double d3, double d4, double d5, List list, List list1, List list2, BlockPos.PooledMutableBlockPos pos) {
        for (Object entityObject : list) {
            if (entityObject instanceof IAlwaysRender && entityObject instanceof Entity entity) {
                this.forceRenderEntity(entity, camera, partialTicks, d0, d1, d2, pos, pass + IAlwaysRender.RENDER_PASS_ALWAYS);
            }
            if (entityObject instanceof ModdedEntity entity) {
                if (entity.getSelf() instanceof IAlwaysRender) {
                    this.forceRenderEntity(entity, camera, partialTicks, d0, d1, d2, pos, pass);
                }
            }
        }
    }

    public void forceRenderEntity(Entity entity, ICamera camera, float partialTicks, double d0, double d1, double d2, BlockPos.PooledMutableBlockPos pos, int pass) {
        if (!entity.shouldRenderInPass(pass)) return;
        entity.ignoreFrustumCheck = true;
        boolean flag = this.renderManager.shouldRender(entity, camera, d0, d1, d2) || entity.isRidingOrBeingRiddenBy(this.mc.player);
        if (flag) {
            boolean flag1 = this.mc.getRenderViewEntity() instanceof EntityLivingBase ? ((EntityLivingBase) this.mc.getRenderViewEntity()).isPlayerSleeping() : false;

            if ((entity != this.mc.getRenderViewEntity() || this.mc.gameSettings.thirdPersonView != 0 || flag1) && (entity.posY < 0.0D || entity.posY >= 256.0D || this.world.isBlockLoaded(pos.setPos(entity)))) {
                this.renderManager.renderEntityStatic(entity, partialTicks, false);
            }
        }
    }
}
