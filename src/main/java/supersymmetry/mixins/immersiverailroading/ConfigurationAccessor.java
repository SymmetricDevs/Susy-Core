package supersymmetry.mixins.immersiverailroading;

import java.util.List;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import cam72cam.immersiverailroading.entity.physics.SimulationState;
import cam72cam.mod.math.Vec3i;

@Mixin(value = SimulationState.Configuration.class, remap = false)
public interface ConfigurationAccessor {

    @Accessor("directResistanceNewtons")
    Function<List<Vec3i>, Double> getDirectResistanceNewtons();
}
