package supersymmetry.mixins.littletiles;

import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;

/**
 * Stop {@link World#getEntitiesWithinAABB(Class, AxisAlignedBB, Predicate)} calls in {@link EntityAnimation}.
 * A brute-force solution for it being too laggy sometimes when the entity is not properly unloaded.
 */
@Mixin(value = EntityAnimation.class, remap = false)
public class EntityAnimationMixin {

    @Redirect(method = "moveAndRotateAnimation(Lcom/creativemd/creativecore/common/utils/math/collision/CollisionCoordinator;)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/World;getEntitiesWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;",
                       remap = true))
    public List<Entity> iDoNotCare(World i, Class<?> do_, AxisAlignedBB not, Predicate<?> care) {
        return Collections.emptyList();
    }

    @Redirect(method = "moveAndRotateAnimation(DDDDDD)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/World;getEntitiesWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;",
                       remap = true))
    public List<Entity> iDoNotCareEither(World i, Class<?> do_, AxisAlignedBB not, Predicate<?> care) {
        return Collections.emptyList();
    }

    @Redirect(method = "onTick",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/World;getEntitiesWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;",
                       remap = true))
    public List<Entity> iDoNotCareAsWell(World i, Class<?> do_, AxisAlignedBB not, Predicate<?> care) {
        return Collections.emptyList();
    }
}
