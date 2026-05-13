package supersymmetry.mixins.icbmclassic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import icbm.classic.content.missile.entity.anti.SAMTargetData;
import supersymmetry.common.entities.EntityDropPod;

@Mixin(value = SAMTargetData.class, remap = false)
public abstract class SAMTargetDataMixin {

    @Shadow
    @Final
    private EntitySurfaceToAirMissile host;

    @Shadow
    protected abstract AxisAlignedBB targetArea();

    @Inject(
            method = "getValidTargets",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void supersymmetry$getValidTargets(CallbackInfoReturnable<List<Entity>> cir) {
        World world = host.world;

        List<Entity> targets = new ArrayList<>();

        List<EntityMissile> missiles = world.getEntitiesWithinAABB(
                EntityMissile.class,
                targetArea());

        targets.addAll(missiles);

        List<EntityDropPod> pods = world.getEntitiesWithinAABB(
                EntityDropPod.class,
                targetArea());

        targets.addAll(pods);

        cir.setReturnValue(targets);
    }

    @Inject(
            method = "isValid(Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void supersymmetry$isValid(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof EntityDropPod) {
            cir.setReturnValue(entity.isEntityAlive());
        }
    }
}
