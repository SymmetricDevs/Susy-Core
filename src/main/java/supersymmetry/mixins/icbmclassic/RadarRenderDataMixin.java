package supersymmetry.mixins.icbmclassic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.content.blocks.radarstation.data.RadarDotType;
import icbm.classic.content.blocks.radarstation.data.RadarRenderData;
import supersymmetry.api.mixin.IDropPodRadar;
import supersymmetry.common.entities.EntityDropPod;

@Mixin(value = RadarRenderData.class, remap = false)
public abstract class RadarRenderDataMixin {

    @Shadow
    private TileRadarStation host;

    @Shadow
    public abstract void addDot(double ex, double ez, RadarDotType type);

    @Inject(method = "update()V", at = @At("TAIL"), remap = false)
    private void supersymmetry$addDropPodDots(CallbackInfo ci) {
        if (!(host instanceof IDropPodRadar)) return;

        for (EntityDropPod pod : ((IDropPodRadar) host).susy$getIncomingDropPods()) {
            if (pod.isEntityAlive()) {
                // INCOMING = red dot, same as missiles heading toward the radar
                addDot(pod.posX, pod.posZ, RadarDotType.INCOMING);
            }
        }
    }
}
