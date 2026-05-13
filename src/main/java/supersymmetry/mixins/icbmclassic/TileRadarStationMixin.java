package supersymmetry.mixins.icbmclassic;

import icbm.classic.content.blocks.radarstation.EnumRadarState;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supersymmetry.api.mixin.IDropPodRadar;
import supersymmetry.common.entities.EntityDropPod;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = TileRadarStation.class, remap = false)
public abstract class TileRadarStationMixin implements IDropPodRadar {

    @Shadow
    private int detectionRange;

    @Unique
    private final List<EntityDropPod> susy$incomingDropPods = new ArrayList<>();

    @Override
    public List<EntityDropPod> susy$getIncomingDropPods() {
        return susy$incomingDropPods;
    }

    @Inject(method = "doScan()V", at = @At("TAIL"), remap = false)
    private void supersymmetry$scanDropPods(CallbackInfo ci) {
        susy$incomingDropPods.clear();

        TileEntity self = (TileEntity)(Object) this;
        BlockPos pos = self.getPos();
        net.minecraft.world.World world = self.getWorld();
        if (world == null) return;

        Vec3d radarPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        AxisAlignedBB area = new AxisAlignedBB(
                pos.getX() - detectionRange, pos.getY() - detectionRange, pos.getZ() - detectionRange,
                pos.getX() + detectionRange, pos.getY() + detectionRange, pos.getZ() + detectionRange
        );

        List<EntityDropPod> pods = world.getEntitiesWithinAABB(EntityDropPod.class, area);

        for (EntityDropPod pod : pods) {
            if (!pod.isEntityAlive()) continue;
            Vec3d podPos = pod.getPositionVector();
            double currentDistance = podPos.distanceTo(radarPos);
            double nextDistance = podPos.add(pod.motionX, pod.motionY, pod.motionZ).distanceTo(radarPos);
            if (nextDistance < currentDistance) {
                susy$incomingDropPods.add(pod);
            }
        }
    }

    @Inject(method = "hasIncomingMissiles()Z", at = @At("RETURN"), remap = false, cancellable = true)
    private void supersymmetry$hasIncomingMissiles(CallbackInfoReturnable<Boolean> cir) {
        if (!susy$incomingDropPods.isEmpty()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getRadarState()Licbm/classic/content/blocks/radarstation/EnumRadarState;", at = @At("RETURN"), remap = false, cancellable = true)
    private void supersymmetry$getRadarState(CallbackInfoReturnable<EnumRadarState> cir) {
        // Only upgrade the state, never downgrade
        if (!susy$incomingDropPods.isEmpty() && cir.getReturnValue() != EnumRadarState.DANGER) {
            cir.setReturnValue(EnumRadarState.DANGER);
        }
    }

    @Inject(method = "getStrongRedstonePower(Lnet/minecraft/util/EnumFacing;)I", at = @At("RETURN"), remap = false, cancellable = true)
    private void supersymmetry$redstonePower(EnumFacing side, CallbackInfoReturnable<Integer> cir) {
        if (!susy$incomingDropPods.isEmpty()) {
            cir.setReturnValue(Math.min(15, cir.getReturnValue() + susy$incomingDropPods.size()));
        }
    }
}
