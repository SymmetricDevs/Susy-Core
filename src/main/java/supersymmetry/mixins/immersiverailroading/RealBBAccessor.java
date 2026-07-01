package supersymmetry.mixins.immersiverailroading;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import cam72cam.immersiverailroading.util.RealBB;

/**
 * Exposes the private fields of {@link RealBB} that {@link SimulationStateMixin}
 * needs to rebuild the box's rotated X-Z hull once (instead of paying for a fresh
 * transform + shape allocation on every {@code intersects} call), and to skip
 * filtering entirely when the box is already axis-aligned.
 */
@Mixin(value = RealBB.class, remap = false)
public interface RealBBAccessor {

    @Accessor("yaw")
    float supersymmetry$getYaw();

    @Accessor("front")
    double supersymmetry$getFront();

    @Accessor("rear")
    double supersymmetry$getRear();

    @Accessor("width")
    double supersymmetry$getWidth();

    @Accessor("centerX")
    double supersymmetry$getCenterX();

    @Accessor("centerZ")
    double supersymmetry$getCenterZ();
}
