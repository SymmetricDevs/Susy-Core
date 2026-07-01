package supersymmetry.mixins.immersiverailroading;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cam72cam.immersiverailroading.entity.physics.SimulationState;
import cam72cam.immersiverailroading.util.RealBB;
import cam72cam.mod.entity.boundingbox.IBoundingBox;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.world.World;

/**
 * Immersive Railroading works out which blocks a piece of rolling stock collides
 * with by taking the axis-aligned {@code min()}/{@code max()} of the stock's
 * {@link RealBB} and asking the world for every block inside it
 * ({@link World#blocksInBounds(IBoundingBox)}). {@code RealBB} carries a yaw, but
 * that derived AABB does not: when a train rounds a corner the AABB balloons to
 * enclose the whole rotated span, so blocks well outside the actual (rotated) hull
 * land in {@code collidingBlocks} — and, downstream, get flagged as interfering
 * and destroyed.
 *
 * <p>
 * We drop the blocks the rotated hull cannot actually touch. Only the X-Z plane
 * is affected — yaw does not tilt the box vertically — so we leave each block's Y
 * untouched (exactly as {@code blocksInBounds} returned it) and test only the X-Z
 * footprint.
 *
 * <p>
 * {@code RealBB#intersects} would answer this, but it rebuilds a rotated
 * {@link Shape} (plus transforms and rectangles) on <em>every</em> call. For large
 * multiblocks such as the transporter erector the candidate set runs to thousands
 * of blocks (~1000 axis-aligned, ~5000 near 45°), so instead we build that rotated
 * hull <em>once</em> — with the same rectangle and transform IR uses for its own
 * physics — and then only pay an allocation-free {@link Shape#intersects} per block.
 * The hull is grown by a small margin so grazing collisions are never lost.
 *
 * @author bruberu/Claude Opus 4.8
 */
@Mixin(value = SimulationState.class, remap = false)
public abstract class SimulationStateMixin {

    /**
     * Slack, in blocks, added around every candidate block voxel before the
     * yaw-aware intersection test. Being generous here keeps borderline collisions
     * rather than risking a train clipping through a block the coarse AABB caught.
     */
    private static final double supersymmetry$MARGIN = 0.125D;

    /**
     * Tolerance, in degrees, for treating a yaw as axis-aligned. Within this the
     * oriented box already fills its own AABB, so the per-block test is a no-op and
     * we skip it entirely.
     */
    private static final float supersymmetry$AXIS_ALIGNED_EPSILON = 1.0e-3F;

    @Redirect(
              method = "calculateBlockCollisions(Ljava/util/List;)V",
              at = @At(
                       value = "INVOKE",
                       target = "Lcam72cam/mod/world/World;blocksInBounds(Lcam72cam/mod/entity/boundingbox/IBoundingBox;)Ljava/util/List;"),
              remap = false)
    private List<Vec3i> supersymmetry$filterByYaw(World world, IBoundingBox bounds) {
        List<Vec3i> blocks = world.blocksInBounds(bounds);

        // Only RealBB carries the yaw that the AABB throws away; anything else is
        // already axis-aligned and needs no correction.
        if (!(bounds instanceof RealBB)) {
            return blocks;
        }
        RealBBAccessor box = (RealBBAccessor) bounds;

        // When the yaw is a multiple of 90 degrees the box is already axis-aligned:
        // it fills its own AABB, so every block would pass the test. Skip the work.
        float offAxis = Math.abs(box.supersymmetry$getYaw() % 90.0F);
        if (offAxis < supersymmetry$AXIS_ALIGNED_EPSILON || offAxis > 90.0F - supersymmetry$AXIS_ALIGNED_EPSILON) {
            return blocks;
        }

        // Build the rotated X-Z hull once, mirroring RealBB.intersectsAt so the
        // blocks we keep are exactly the ones IR's own physics would collide with.
        double width = box.supersymmetry$getWidth();
        double centerX = box.supersymmetry$getCenterX();
        double centerZ = box.supersymmetry$getCenterZ();
        Rectangle2D rect = new Rectangle2D.Double(
                box.supersymmetry$getRear() + centerX,
                -width / 2.0 + centerZ,
                box.supersymmetry$getFront() - box.supersymmetry$getRear(),
                width);
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(180.0 - box.supersymmetry$getYaw() + 90.0), centerX, centerZ);
        Shape hull = transform.createTransformedShape(rect);

        // Each block spans [pos, pos + 1] in X-Z; grow it by the margin on both sides.
        double size = 1.0 + 2.0 * supersymmetry$MARGIN;
        List<Vec3i> filtered = new ArrayList<>(blocks.size());
        for (Vec3i pos : blocks) {
            if (hull.intersects(pos.x - supersymmetry$MARGIN, pos.z - supersymmetry$MARGIN, size, size)) {
                filtered.add(pos);
            }
        }
        return filtered;
    }
}
