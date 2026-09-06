package supersymmetry.mixins.immersiverailroading;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import cam72cam.immersiverailroading.entity.physics.SimulationState;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;

@Mixin(value = SimulationState.class, remap = false)
public class SimulationStateMixin {

    @Shadow
    public Vec3d position;

    @Shadow
    public List<Vec3i> trackToUpdate;
    @Shadow
    public SimulationState.Configuration config;

    @ModifyReturnValue(method = "next(DLjava/util/List;)Lcam72cam/immersiverailroading/entity/physics/SimulationState;",
                       at = @At("TAIL"))
    public SimulationState next(SimulationState next) {
        if (this.position.equals(next.position)) { // even if
            next.directResistance = ((ConfigurationAccessor) this.config).getDirectResistanceNewtons()
                    .apply(this.trackToUpdate);
        }
        return next;
    }
}
