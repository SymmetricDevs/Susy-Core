package supersymmetry.mixins.universalmodcore;

import net.minecraft.entity.item.EntityItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cam72cam.mod.world.World;

/**
 * UMC 1.2.2 gives a dropped item its velocity with {@link EntityItem#setVelocity}, which is client-only and so
 * missing on a dedicated server. Every drop there throws NoSuchMethodError: breaking Immersive Railroading track,
 * rolling stock or machines drops nothing, and a break finished inside the player tick crashes the server.
 * <p>
 * Fixed in UMC 1.2.3, but that version also changes this method's return type, which IR 1.10.0 can't link against.
 * Remove this once both are updated.
 */
@Mixin(value = World.class, remap = false)
public class WorldMixin {

    // All overloads: only the one with a velocity (new in UMC 1.2.2, newer than the build dependency) makes this call
    @Redirect(method = "dropItem*",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/entity/item/EntityItem;setVelocity(DDD)V",
                       remap = true))
    private void setMotionOnBothSides(EntityItem entity, double x, double y, double z) {
        entity.motionX = x;
        entity.motionY = y;
        entity.motionZ = z;
    }
}
