package supersymmetry.mixins.icbmclassic;

import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import icbm.classic.content.missile.entity.anti.SAMTargetData;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supersymmetry.api.SusyLog;
import supersymmetry.common.entities.EntityDropPod;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SAMTargetData.class, remap = false)
public abstract class SAMTargetDataMixin {

    static {
        SusyLog.logger.info("[SUSY MIXIN] SAMTargetDataMixin class loaded");
    }

    @Shadow @Final
    private EntitySurfaceToAirMissile host;

    @Shadow
    protected abstract AxisAlignedBB targetArea();

    @Inject(
            method = "getValidTargets",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void supersymmetry$getValidTargets(CallbackInfoReturnable<List<Entity>> cir) {

        SusyLog.logger.info("[SUSY MIXIN] getValidTargets injected");

        World world = host.world;

        List<Entity> targets = new ArrayList<>();

        List<EntityMissile> missiles = world.getEntitiesWithinAABB(
                EntityMissile.class,
                targetArea()
        );

        SusyLog.logger.info("[SUSY MIXIN] missiles found: {}", missiles.size());

        targets.addAll(missiles);

        List<EntityDropPod> pods = world.getEntitiesWithinAABB(
                EntityDropPod.class,
                targetArea()
        );

        SusyLog.logger.info("[SUSY MIXIN] drop pods found: {}", pods.size());

        targets.addAll(pods);

        cir.setReturnValue(targets);
    }

    @Inject(
            method = "isValid(Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void supersymmetry$isValid(Entity entity, CallbackInfoReturnable<Boolean> cir) {

        SusyLog.logger.info("[SUSY MIXIN] isValid injected: {}", entity);

        if (entity instanceof EntityDropPod) {
            SusyLog.logger.info("[SUSY MIXIN] accepting drop pod");
            cir.setReturnValue(entity.isEntityAlive());
        }
    }
}
