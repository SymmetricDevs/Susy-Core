package supersymmetry.common.faction;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityAIThrowGrenade extends EntityAIBase {

    private static final Set<String> DANGEROUS_ENTITY_NAMES = new HashSet<>(Arrays.asList(
            "gaspunk:gas_grenade",
            "gaspunk:gas_cloud",
            "icbmclassic:item.grenade",
            "techguns:grenadeprojectile",
            "techguns:fraggrenadeprojectile"
            //not items, actual grenade entities
    ));

    private static Set<Class<? extends Entity>> resolvedDangerousClasses = null;

    private static Set<Class<? extends Entity>> getResolvedDangerousClasses() {
        if (resolvedDangerousClasses == null) {
            resolvedDangerousClasses = DANGEROUS_ENTITY_NAMES.stream()
                    .map(ResourceLocation::new)
                    .map(ForgeRegistries.ENTITIES::getValue)
                    .filter(entry -> entry != null)
                    .map(EntityEntry::getEntityClass)
                    .collect(Collectors.toSet());
        }
        return resolvedDangerousClasses;
    }

    private static final Predicate<Entity> IS_DANGEROUS_ENTITY = entity ->
            entity != null && getResolvedDangerousClasses().contains(entity.getClass());

    public static boolean isDangerousEntity(Entity entity) {
        return IS_DANGEROUS_ENTITY.apply(entity);
    }

    private enum Phase { CHARGING, RECOVERING, FLEEING }

    private static final int RECOVERY_TICKS = 30;

    public static final double FLEE_TRIGGER_RADIUS = 15.0D;
    private static final int FLEE_CLEAR_GRACE_TICKS = 20;
    private static final double FLEE_SPEED = 1.2D;
    private static final int FLEE_REPATH_INTERVAL = 15;

    private final EntityLiving mob;
    private final double throwRangeSq;
    private final boolean isSmart;

    private Phase phase = Phase.CHARGING;
    private int chargeTicks = -1;
    private int chargeTarget = -1;
    private int recoveryTicks = -1;
    private int fleeClearTicks = -1;
    private int cooldown = 0;
    private ItemStack heldTemplate = ItemStack.EMPTY;

    public EntityAIThrowGrenade(EntityLiving mob, double throwRange) {
        this.mob = mob;
        this.throwRangeSq = throwRange * throwRange;
        this.isSmart = FactionViolence.hasSmartAI(mob);
        this.setMutexBits(4);
    }

    private boolean isHoldingGrenade() {
        return GrenadeAIHandler.isThrowable(mob.getHeldItemMainhand());
    }

    private Entity findNearestDanger() {
        List<Entity> nearby = mob.world.getEntitiesWithinAABB(Entity.class,
                mob.getEntityBoundingBox().grow(FLEE_TRIGGER_RADIUS));

        Entity nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (Entity candidate : nearby) {
            if (!isDangerousEntity(candidate)) continue;
            double distSq = mob.getDistanceSq(candidate);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = candidate;
            }
        }
        return nearest;
    }

    @Override
    public boolean shouldExecute() {
        if (cooldown > 0) { cooldown--; return false; }
        EntityLivingBase target = mob.getAttackTarget();
        if (target == null || !target.isEntityAlive() || !isHoldingGrenade()) return false;
        return mob.getDistanceSq(target) <= throwRangeSq && mob.getEntitySenses().canSee(target);
    }

    @Override
    public boolean shouldContinueExecuting() {
        switch (phase) {
            case RECOVERING:
                return recoveryTicks < RECOVERY_TICKS;
            case FLEEING:
                return fleeClearTicks < FLEE_CLEAR_GRACE_TICKS;
            case CHARGING:
            default:
                EntityLivingBase target = mob.getAttackTarget();
                return target != null && target.isEntityAlive() && isHoldingGrenade() && chargeTicks < chargeTarget;
        }
    }

    @Override
    public void startExecuting() {
        phase = Phase.CHARGING;
        chargeTicks = 0;
        heldTemplate = mob.getHeldItemMainhand().copy();
        chargeTarget = GrenadeAIHandler.getChargeTicks(heldTemplate);
        mob.setActiveHand(EnumHand.MAIN_HAND);

        if (!isSmart) {
            mob.getNavigator().clearPath();
        }
    }

    @Override
    public void resetTask() {
        if (mob.isHandActive()) {
            mob.resetActiveHand();
        }
        phase = Phase.CHARGING;
        chargeTicks = -1;
        recoveryTicks = -1;
        fleeClearTicks = -1;
        mob.getNavigator().clearPath();
        cooldown = 20;
    }

    @Override
    public void updateTask() {
        switch (phase) {
            case CHARGING:    updateCharging();   break;
            case RECOVERING:  updateRecovering(); break;
            case FLEEING:     updateFleeing();    break;
        }
    }

    private void updateCharging() {
        EntityLivingBase target = mob.getAttackTarget();
        if (target == null) return;

        mob.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        mob.faceEntity(target, 30.0F, 30.0F);

        if (!isSmart) {
            mob.getNavigator().clearPath();
            mob.motionX = 0.0D;
            mob.motionZ = 0.0D;
        }

        chargeTicks++;

        if (chargeTicks >= chargeTarget) {
            mob.stopActiveHand();

            if (mob.getHeldItemMainhand().isEmpty() && !heldTemplate.isEmpty()) {
                mob.setHeldItem(EnumHand.MAIN_HAND, heldTemplate.copy());
            }

            if (!isSmart) {
                mob.getNavigator().clearPath();
                if (findNearestDanger() != null) {
                    phase = Phase.FLEEING;
                    fleeClearTicks = 0;
                    updateFleeing();
                } else {
                    phase = Phase.RECOVERING;
                    recoveryTicks = 0;
                }
            } else {
                phase = Phase.RECOVERING;
                recoveryTicks = 0;
            }
        }
    }

    private void updateRecovering() {
        recoveryTicks++;
        EntityLivingBase target = mob.getAttackTarget();
        if (target != null) {
            mob.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        }

        if (!isSmart) {
            mob.getNavigator().clearPath();
            mob.motionX = 0.0D;
            mob.motionZ = 0.0D;
            if (findNearestDanger() != null) {
                phase = Phase.FLEEING;
                fleeClearTicks = 0;
                updateFleeing();
            }
        }
    }

    private void updateFleeing() {
        Entity danger = findNearestDanger();

        if (danger != null) {
            fleeClearTicks = 0;

            boolean needsRepath = mob.getNavigator().noPath() || mob.ticksExisted % FLEE_REPATH_INTERVAL == 0;
            if (needsRepath) {
                Vec3d fleeVec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
                        (EntityCreature) mob, 16, 7, new Vec3d(danger.posX, danger.posY, danger.posZ));

                if (fleeVec != null) {
                    mob.getNavigator().tryMoveToXYZ(fleeVec.x, fleeVec.y, fleeVec.z, FLEE_SPEED);
                } else {
                    double dx = mob.posX - danger.posX;
                    double dz = mob.posZ - danger.posZ;
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist < 0.001D) { dx = 1.0D; dz = 0.0D; dist = 1.0D; }
                    mob.getNavigator().tryMoveToXYZ(
                            mob.posX + (dx / dist) * FLEE_TRIGGER_RADIUS, mob.posY,
                            mob.posZ + (dz / dist) * FLEE_TRIGGER_RADIUS, FLEE_SPEED);
                }
            }
        } else {
            fleeClearTicks++;
        }
    }
}
