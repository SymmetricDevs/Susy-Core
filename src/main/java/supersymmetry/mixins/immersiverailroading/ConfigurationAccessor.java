package supersymmetry.mixins.immersiverailroading;

import cam72cam.mod.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;

import cam72cam.immersiverailroading.entity.physics.SimulationState;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Function;

@Mixin(value = SimulationState.Configuration.class, remap = false)
public interface ConfigurationAccessor {
    @Accessor("directResistanceNewtons")
    Function<List<Vec3i>, Double> getDirectResistanceNewtons();
}
