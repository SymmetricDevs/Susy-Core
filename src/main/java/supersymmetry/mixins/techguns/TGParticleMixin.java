package supersymmetry.mixins.techguns;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import supersymmetry.SusyConfig;
import techguns.client.particle.TGParticle;

/**
 * Distance-culls Techguns' client-side particles.
 *
 * <p>
 * Techguns keeps every particle in one list and draws the whole list in {@code RenderWorldLast}, calling
 * {@code Tessellator.draw()} once per particle, without frustum or distance culling. Its trail effects are declared
 * with an infinite system lifetime and an "entity alive" condition, so a trail whose parent entity leaves the client
 * world never expires: it keeps spawning particles that nobody can see. In a pack profile those entries were 28% of
 * the client thread, almost all of them invisible.
 * </p>
 *
 * <p>
 * This mixin only cancels the draw of a particle beyond the configured radius, so nothing about spawning or
 * lifetime changes; visible particles and the rest of Techguns are untouched.
 * </p>
 */
@Mixin(value = TGParticle.class, remap = false)
public abstract class TGParticleMixin {

    @Shadow
    public abstract double posX();

    @Shadow
    public abstract double posY();

    @Shadow
    public abstract double posZ();

    @Inject(method = "doRender", at = @At("HEAD"), cancellable = true)
    private void susy$cullDistantParticle(BufferBuilder buffer, Entity viewEntity, float partialTicks, float rotX,
                                          float rotZ, float rotYZ, float rotXY, float rotXZ, CallbackInfo ci) {
        double maxDistance = SusyConfig.techgunsParticleCullDistance;
        if (maxDistance <= 0.0 || viewEntity == null) {
            return;
        }
        if (viewEntity.getDistanceSq(posX(), posY(), posZ()) > maxDistance * maxDistance) {
            ci.cancel();
        }
    }
}
