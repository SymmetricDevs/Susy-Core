package supersymmetry.common.faction;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;
import supersymmetry.common.potion.PotionDropPodSickness;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class FactionViolence {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";
    private static final String TAG_SMART_AI = "smartAI";
    private static final double radius = 32.0;

    private static final double STRAFE_SPEED = 0.5D;
    private static final double CHASE_SPEED  = 1.1D;
    private static final double BACK_OFF_DIST_SQ = 16.0D * 16.0D;

    private static final String TAG_STRAFING_CLOCKWISE = "strafingClockwise";
    private static final String TAG_STRAFING_BACKWARDS = "strafingBackwards";
    private static final String TAG_STRAFING_TIME = "strafingTime";

    private static final String TAG_COVER_X = "coverX";
    private static final String TAG_COVER_Y = "coverY";
    private static final String TAG_COVER_Z = "coverZ";
    private static final String TAG_LAST_SEARCH_TIME = "lastSearchTime";

    // ========================================================================
    // Smart AI: anti friendly fire, because fuck the playerbase
    // ========================================================================

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().world.isRemote) return;
        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityLivingBase)) return;
        if (!hasSmartAI((EntityLivingBase) source)) return;
        String victimFaction = getFaction(event.getEntityLiving());
        if (victimFaction.isEmpty()) return;
        String sourceFaction = getFaction((EntityLivingBase) source);
        if (sourceFaction.isEmpty()) return;
        if (victimFaction.equals(sourceFaction)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!FactionViolenceManager.isEnabled(event.getEntity().world)) return;
        if (!(event.getEntity() instanceof EntityLiving)) return;

        EntityLiving mob = (EntityLiving) event.getEntity();
        NBTTagCompound tag = mob.getEntityData();
        if (!tag.hasKey(TAG_ROOT)) return;
        NBTTagCompound susyTag = tag.getCompoundTag(TAG_ROOT);
        if (!susyTag.hasKey(TAG_FACTION)) return;
        String mobFaction = susyTag.getString(TAG_FACTION);
        if (mobFaction.isEmpty()) return;
        if (!(mob instanceof IMob)) return;
        if (mob.isPotionActive(PotionDropPodSickness.INSTANCE)) return;

        boolean isSmart = susyTag.getBoolean(TAG_SMART_AI);

        // hot-swapping out for our custom nav, because the vanilla nav is not good enough for what I'm trying to cook

        if (isSmart && !(mob.getNavigator() instanceof FactionPathNavigator)) {
            if (mob.getNavigator() instanceof PathNavigateGround) {
                try {
                    FactionPathNavigator customNav = new FactionPathNavigator(mob, mob.world);
                    ObfuscationReflectionHelper.setPrivateValue(
                            EntityLiving.class,
                            mob,
                            customNav,
                            "navigator", "field_70699_by"
                    );
                    boolean hasDoorAI = mob.tasks.taskEntries.stream()
                            .anyMatch(e -> e.action instanceof net.minecraft.entity.ai.EntityAIOpenDoor);
                    if (!hasDoorAI) {
                        mob.tasks.addTask(0, new net.minecraft.entity.ai.EntityAIOpenDoor(mob, true));
                    }
                } catch (Exception e) {}
            }
        }

        // unfucking targets, because techguns
        EntityLivingBase currentTarget = mob.getAttackTarget();
        if (currentTarget != null) {
            String targetFaction = getFaction(currentTarget);
            if (!targetFaction.isEmpty() && mobFaction.equals(targetFaction)) {
                mob.setAttackTarget(null);
                if (mob.getRevengeTarget() == currentTarget) mob.setRevengeTarget(null);
            } else if (currentTarget.isDead || !currentTarget.isEntityAlive()) {
                mob.setAttackTarget(null);
            } else if (currentTarget instanceof EntityPlayer) {
                EntityPlayer targetPlayer = (EntityPlayer) currentTarget;
                if (targetPlayer.isCreative() || targetPlayer.isSpectator()) {
                    mob.setAttackTarget(null);
                    if (mob.getRevengeTarget() == currentTarget) mob.setRevengeTarget(null);
                }
            }
        }

        EntityLivingBase revengeTarget = mob.getRevengeTarget();
        if (revengeTarget != null) {
            String revengeFaction = getFaction(revengeTarget);
            if (!revengeFaction.isEmpty() && mobFaction.equals(revengeFaction)) {
                mob.setRevengeTarget(null);
            }
        }

        if (mob.getAttackTarget() != null &&
                (!(mob.getAttackTarget() instanceof IMob) &&
                        !(mob.getAttackTarget() instanceof EntityPlayer))) {
            mob.setAttackTarget(null);
        }

        // ====================================================================
        //  Smart AI: wallhacking against players, because fuck the playerbase
        // ====================================================================

        if (isSmart) {
            EntityLivingBase bestSmartTarget = null;
            double nearestDistSq = Double.MAX_VALUE;

            for (EntityPlayer player : mob.world.getEntitiesWithinAABB(EntityPlayer.class,
                    mob.getEntityBoundingBox().grow(radius))) {
                if (player.isCreative() || player.isSpectator()) continue;
                String playerFaction = getFaction(player);
                if (!playerFaction.isEmpty() && mobFaction.equals(playerFaction)) continue;
                double distSq = mob.getDistanceSq(player);
                if (distSq < nearestDistSq) {
                    nearestDistSq = distSq;
                    bestSmartTarget = player;
                }
            }

            for (EntityLivingBase target : mob.world.getEntitiesWithinAABB(EntityLivingBase.class,
                    mob.getEntityBoundingBox().grow(radius))) {
                if (target == mob) continue;
                if (target instanceof EntityPlayer) continue;
                if (!mob.canEntityBeSeen(target)) continue;
                String targetFaction = getFaction(target);
                boolean isUnaligned = targetFaction.isEmpty();
                boolean isOpposingFaction = !isUnaligned && !mobFaction.equals(targetFaction);
                boolean shouldAttack = (isUnaligned && target instanceof IMob) || isOpposingFaction;
                if (!shouldAttack) continue;
                double distSq = mob.getDistanceSq(target);
                if (distSq < nearestDistSq) {
                    nearestDistSq = distSq;
                    bestSmartTarget = target;
                }
            }

            if (bestSmartTarget != null) {
                mob.setAttackTarget(bestSmartTarget);
            }
        }

        // default
        if (!isSmart && mob.getAttackTarget() == null) {
            EntityLivingBase bestTarget = null;
            double bestDistanceSq = Double.MAX_VALUE;
            for (EntityLivingBase target : mob.world.getEntitiesWithinAABB(EntityLivingBase.class,
                    mob.getEntityBoundingBox().grow(radius))) {
                if (target == mob) continue;
                if (!mob.canEntityBeSeen(target)) continue;
                String targetFaction = getFaction(target);
                boolean isUnaligned = targetFaction.isEmpty();
                boolean isOpposingFaction = !isUnaligned && !mobFaction.equals(targetFaction);
                boolean shouldAttack = false;
                if (isUnaligned) {
                    if (target instanceof IMob ||
                            (target instanceof EntityPlayer &&
                                    !((EntityPlayer) target).isCreative() &&
                                    !((EntityPlayer) target).isSpectator())) {
                        shouldAttack = true;
                    }
                } else if (isOpposingFaction) {
                    shouldAttack = true;
                }
                if (!shouldAttack) continue;
                double distSq = mob.getDistanceSq(target);
                if (distSq < bestDistanceSq) {
                    bestDistanceSq = distSq;
                    bestTarget = target;
                }
            }
            if (bestTarget != null) mob.setAttackTarget(bestTarget);
        }

        // ====================================================================
        //  Smart AI: Cover seeking when low on health, because fuck the playerbase
        // ====================================================================

        boolean shouldStrafe = true;
        boolean isInCover = false;
        boolean isLowHealth = mob.getHealth() <= (mob.getMaxHealth() * 0.5F);

        if (isSmart) {
            EntityLivingBase target = mob.getAttackTarget();
            BlockPos coverPos = null;
            boolean coverValid = false;

            if (target != null && isLowHealth) {
                long gameTime = mob.world.getTotalWorldTime();
                long lastSearchTime = susyTag.getLong(TAG_LAST_SEARCH_TIME);
                double weaponRange = getWeaponRange(mob);
                boolean hasCover = susyTag.hasKey(TAG_COVER_X) && susyTag.hasKey(TAG_COVER_Y) && susyTag.hasKey(TAG_COVER_Z);

                if (hasCover) {
                    int cx = susyTag.getInteger(TAG_COVER_X);
                    int cy = susyTag.getInteger(TAG_COVER_Y);
                    int cz = susyTag.getInteger(TAG_COVER_Z);
                    coverPos = new BlockPos(cx, cy, cz);
                    coverValid = isCoverValid(mob, target, coverPos, weaponRange);
                    if (!coverValid) {
                        clearStoredCover(susyTag, tag);
                        coverPos = null;
                    }
                }

                boolean cooldownExpired = (gameTime - lastSearchTime) > 40;

                if (!coverValid && cooldownExpired) {
                    BlockPos newCover = findCoverPosition(mob, target, weaponRange);
                    susyTag.setLong(TAG_LAST_SEARCH_TIME, gameTime);
                    if (newCover != null) {
                        coverPos = newCover;
                        coverValid = true;
                        susyTag.setInteger(TAG_COVER_X, newCover.getX());
                        susyTag.setInteger(TAG_COVER_Y, newCover.getY());
                        susyTag.setInteger(TAG_COVER_Z, newCover.getZ());
                    }
                    tag.setTag(TAG_ROOT, susyTag);
                }

                if (coverValid && coverPos != null) {
                    shouldStrafe = false;

                    double targetX = coverPos.getX() + 0.5D;
                    double targetY = coverPos.getY();
                    double targetZ = coverPos.getZ() + 0.5D;
                    double dx = mob.posX - targetX;
                    double dz = mob.posZ - targetZ;
                    double distToCoverSq = dx * dx + dz * dz;

                    boolean wasInCover = susyTag.getBoolean("wasInCover");
                    boolean keepCoverState = wasInCover ? (distToCoverSq <= 6.25D) : (distToCoverSq <= 2.25D);

                    if (!keepCoverState) {
                        susyTag.setBoolean("wasInCover", false);
                        tag.setTag(TAG_ROOT, susyTag);

                        mob.tasks.taskEntries.forEach(e -> {
                            if (e.action instanceof net.minecraft.entity.ai.EntityAIWander ||
                                    e.action instanceof net.minecraft.entity.ai.EntityAIWanderAvoidWater ||
                                    e.action instanceof net.minecraft.entity.ai.EntityAIAttackRanged) {
                                e.action.resetTask();
                            }
                            // techguns..........
                            String name = e.action.getClass().getSimpleName();
                            if (name.equals("EntityAIRangedAttack")) {
                                e.action.resetTask();
                            }
                        });

                        boolean needsRepath = mob.getNavigator().noPath() || mob.ticksExisted % 15 == 0;
                        if (needsRepath) {
                            FactionAStar astar = new FactionAStar(mob.world, mob);
                            BlockPos mobBlockPos = mob.getPosition();
                            net.minecraft.pathfinding.Path path = astar.findPath(mobBlockPos, coverPos);
                            if (path != null && path.getCurrentPathLength() > 0) {
                                mob.getNavigator().setPath(path, 1.5D);
                            }
                        }

                        mob.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);

                        if (mob.collidedHorizontally) {
                            mob.motionY = 0.3D;
                            mob.fallDistance = 0.0F;
                            if (mob.onGround) mob.setPosition(mob.posX, mob.posY + 0.01D, mob.posZ);
                        }
                    } else {
                        isInCover = true;
                        susyTag.setBoolean("wasInCover", true);
                        tag.setTag(TAG_ROOT, susyTag);

                        clearNavigatorPath(mob);

                        mob.motionX = 0.0D;
                        mob.motionZ = 0.0D;
                        mob.setPosition(targetX, mob.posY, targetZ);

                        double dX = target.posX - mob.posX;
                        double dZ = target.posZ - mob.posZ;
                        double dY = (target.posY + (double) target.getEyeHeight()) - (mob.posY + (double) mob.getEyeHeight());
                        double horizontalDist = Math.sqrt(dX * dX + dZ * dZ);
                        float targetYaw = (float) (Math.atan2(dZ, dX) * (180.0D / Math.PI)) - 90.0F;
                        float targetPitch = (float) (-(Math.atan2(dY, horizontalDist) * (180.0D / Math.PI)));
                        float smoothedYaw = MathHelper.wrapDegrees(mob.rotationYaw + MathHelper.wrapDegrees(targetYaw - mob.rotationYaw));
                        mob.rotationYaw = smoothedYaw;
                        mob.rotationYawHead = smoothedYaw;
                        mob.renderYawOffset = smoothedYaw;
                        mob.rotationPitch = targetPitch;
                        mob.prevRotationYaw = smoothedYaw;
                        mob.prevRotationYawHead = smoothedYaw;
                        mob.prevRenderYawOffset = smoothedYaw;
                        mob.prevRotationPitch = targetPitch;
                        mob.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                    }
                } else {
                    shouldStrafe = true;
                }
            } else {
                clearStoredCover(susyTag, tag);
                shouldStrafe = true;
            }
        }

        // ====================================================================
        //  Smart AI: Strafing, because fuck the playerbase
        // ====================================================================

        if (shouldStrafe && isSmart) {
            EntityLivingBase smartTarget = mob.getAttackTarget();
            if (smartTarget != null) {
                boolean canSeeTarget = mob.getEntitySenses().canSee(smartTarget);
                double distSq = mob.getDistanceSq(smartTarget);

                if (!canSeeTarget) {
                    if (mob.getNavigator().noPath() || mob.ticksExisted % 10 == 0) {
                        mob.getNavigator().tryMoveToEntityLiving(smartTarget, CHASE_SPEED);
                    }
                    susyTag.setInteger(TAG_STRAFING_TIME, -1);
                    tag.setTag(TAG_ROOT, susyTag);
                } else {
                    mob.getLookHelper().setLookPositionWithEntity(smartTarget, 30.0F, 30.0F);

                    int strafingTime = susyTag.getInteger(TAG_STRAFING_TIME);
                    boolean strafingClockwise = susyTag.getBoolean(TAG_STRAFING_CLOCKWISE);
                    boolean strafingBackwards = susyTag.getBoolean(TAG_STRAFING_BACKWARDS);

                    boolean inStrafe = distSq <= BACK_OFF_DIST_SQ && canSeeTarget;
                    if (inStrafe) {
                        clearNavigatorPath(mob);
                        strafingTime++;
                    } else {
                        if (mob.getNavigator().noPath()) {
                            mob.getNavigator().tryMoveToEntityLiving(smartTarget, STRAFE_SPEED);
                        }
                        strafingTime = -1;
                    }

                    if (strafingTime >= 20) {
                        if (mob.getRNG().nextFloat() < 0.3D) strafingClockwise = !strafingClockwise;
                        if (mob.getRNG().nextFloat() < 0.3D) strafingBackwards = !strafingBackwards;
                        strafingTime = 0;
                    }

                    if (strafingTime > -1) {
                        if (distSq > BACK_OFF_DIST_SQ * 0.75D) strafingBackwards = false;
                        else if (distSq < BACK_OFF_DIST_SQ * 0.25D) strafingBackwards = true;
                        mob.getMoveHelper().strafe(strafingBackwards ? -0.5F : 0.5F,
                                strafingClockwise ? 0.5F : -0.5F);
                        mob.faceEntity(smartTarget, 30.0F, 30.0F);
                    } else {
                        mob.getLookHelper().setLookPositionWithEntity(smartTarget, 30.0F, 30.0F);
                    }

                    susyTag.setInteger(TAG_STRAFING_TIME, strafingTime);
                    susyTag.setBoolean(TAG_STRAFING_CLOCKWISE, strafingClockwise);
                    susyTag.setBoolean(TAG_STRAFING_BACKWARDS, strafingBackwards);
                    tag.setTag(TAG_ROOT, susyTag);
                }
            }
        }

        // ====================================================================
        //  Smart AI: Wall climbing, because fuck the playerbase
        // ====================================================================

        if (isSmart && !isInCover) {
            EntityLivingBase target = mob.getAttackTarget();
            if (target != null) {
                boolean isNavigating = !mob.getNavigator().noPath();

                boolean nextNodeAbove = false;
                net.minecraft.pathfinding.PathPoint nextNode = null;

                if (isNavigating && mob.getNavigator().getPath() != null) {
                    net.minecraft.pathfinding.Path path = mob.getNavigator().getPath();
                    int nextIndex = path.getCurrentPathIndex();
                    if (nextIndex < path.getCurrentPathLength()) {
                        nextNode = path.getPathPointFromIndex(nextIndex);
                        nextNodeAbove = nextNode.y > MathHelper.floor(mob.posY);
                    }
                }

                if (isNavigating && nextNodeAbove && nextNode != null) {
                    net.minecraft.pathfinding.Path path = mob.getNavigator().getPath();
                    int nextIndex = path.getCurrentPathIndex();

                    boolean atClimbPeak;
                    int afterPeak = nextIndex + 1;
                    if (afterPeak < path.getCurrentPathLength()) {
                        net.minecraft.pathfinding.PathPoint nodeAfter = path.getPathPointFromIndex(afterPeak);
                        atClimbPeak = nodeAfter.y <= nextNode.y;
                    } else {
                        atClimbPeak = true;
                    }

                    BlockPos mobPos = mob.getPosition();

                    for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                        BlockPos adjacent = mobPos.offset(facing);
                        BlockPos adjacentBelow = adjacent.down();

                        boolean isWallPresent = mob.world.getBlockState(adjacent).getMaterial().isSolid()
                                || mob.world.getBlockState(adjacentBelow).getMaterial().isSolid();

                        if (isWallPresent) {
                            double faceCenterX = adjacent.getX() + 0.5D - facing.getXOffset() * 0.5D;
                            double faceCenterZ = adjacent.getZ() + 0.5D - facing.getZOffset() * 0.5D;
                            double dx = mob.posX - faceCenterX;
                            double dz = mob.posZ - faceCenterZ;

                            if (Math.sqrt(dx * dx + dz * dz) <= 0.65D) {
                                double currentY = mob.posY;
                                double topOfCurrentBlock = Math.ceil(currentY);
                                double distToBlockTop = topOfCurrentBlock - currentY;

                                if (atClimbPeak && distToBlockTop <= 0.4D && distToBlockTop > 0.0D
                                        && nextNode.y <= MathHelper.floor(mob.posY) + 1) {
                                    double vaultX = mob.posX + facing.getXOffset() * 0.35D;
                                    double vaultY = topOfCurrentBlock + 0.05D;
                                    double vaultZ = mob.posZ + facing.getZOffset() * 0.35D;

                                    mob.setPosition(vaultX, vaultY, vaultZ);

                                    double dirX = nextNode.x + 0.5D - mob.posX;
                                    double dirZ = nextNode.z + 0.5D - mob.posZ;
                                    double dist = Math.sqrt(dirX * dirX + dirZ * dirZ);

                                    if (dist > 0.001D) {
                                        mob.motionX = (dirX / dist) * 0.25D;
                                        mob.motionZ = (dirZ / dist) * 0.25D;
                                    }

                                    mob.motionY = 0.1D;
                                    mob.fallDistance = 0.0F;
                                    mob.onGround = true;
                                } else {
                                    mob.motionY = 0.35D;
                                    mob.fallDistance = 0.0F;

                                    if (mob.onGround) {
                                        mob.setPosition(mob.posX, mob.posY + 0.08D, mob.posZ);
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else if (shouldStrafe && mob.collidedHorizontally && mob.getEntitySenses().canSee(target)) {
                    mob.motionY = 0.2D;
                    mob.fallDistance = 0.0F;
                    if (mob.onGround) {
                        mob.setPosition(mob.posX, mob.posY + 0.01D, mob.posZ);
                    }
                }
            }
        }

        tag.setTag(TAG_ROOT, susyTag);
    }

    // helpers

    private static double getWeaponRange(EntityLiving mob) {
        double maxAllowedRange = 20.0D;
        double range = -1.0D;

        for (EntityAITasks.EntityAITaskEntry entry : mob.tasks.taskEntries) {
            if (entry.action instanceof EntityAIAttackRanged) {
                try {
                    float attackRadius = ObfuscationReflectionHelper.getPrivateValue(
                            EntityAIAttackRanged.class,
                            (EntityAIAttackRanged) entry.action,
                            "attackRadius", "field_82642_h"
                    );
                    if (attackRadius > 0) { range = attackRadius; break; }
                } catch (Exception ignored) {}
            }

            // arbuz arbuz privet
            // Techguns EntityAIRangedAttack - reflect attackRange field.
            // why would techguns do this???
            // at least it's easy to remove when we excise it from the pack one day
            String taskClassName = entry.action.getClass().getSimpleName();
            if (taskClassName.equals("EntityAIRangedAttack")) {
                Class rawClass = entry.action.getClass();
                Object rawAction = entry.action;

                try {
                    java.lang.reflect.Field f = rawClass.getDeclaredField("attackRange");
                    f.setAccessible(true);
                    float attackRange = (float) f.get(rawAction);
                    if (attackRange > 0) { range = attackRange; break; }
                } catch (Exception ignored) {}

                try {
                    java.lang.reflect.Field f = rawClass.getDeclaredField("maxAttackDistance");
                    f.setAccessible(true);
                    float maxDist = (float) f.get(rawAction);
                    if (maxDist > 0) { range = maxDist; break; }
                } catch (Exception ignored) {}
            }
        }

        if (range <= 0.0D) {
            ItemStack heldItem = mob.getHeldItemMainhand();
            if (!heldItem.isEmpty()) {
                try {
                    java.lang.reflect.Field f = heldItem.getItem().getClass().getField("AI_attackRange");
                    float itemRange = (float) f.get(heldItem.getItem());
                    if (itemRange > 0) {
                        range = itemRange;
                    }
                } catch (Exception ignored) {}
                if (range <= 0.0D) {
                    Class<?> cls = heldItem.getItem().getClass().getSuperclass();
                    while (cls != null && range <= 0.0D) {
                        try {
                            java.lang.reflect.Field f = cls.getDeclaredField("AI_attackRange");
                            f.setAccessible(true);
                            float itemRange = (float) f.get(heldItem.getItem());
                            if (itemRange > 0) {
                                range = itemRange;
                            }
                        } catch (Exception ignored) {}
                        cls = cls.getSuperclass();
                    }
                }
            }
        }

        if (range <= 0.0D) {
            ItemStack heldItem = mob.getHeldItemMainhand();
            if (!heldItem.isEmpty() && heldItem.hasTagCompound()) {
                NBTTagCompound itemTag = heldItem.getTagCompound();
                if (itemTag != null && itemTag.hasKey("Range")) {
                    range = itemTag.getDouble("Range");
                }
            }
        }

        if (range <= 0.0D) range = 16.0D;
        return Math.min(range, maxAllowedRange);
    }

    private static boolean hasLineOfSight(World world, Vec3d fromPos, Vec3d toPos) {
        Vec3d dir = toPos.subtract(fromPos);
        double dist = dir.length();
        if (dist < 0.001) return true;
        Vec3d normalizedDir = dir.normalize();
        Vec3d origin = fromPos.add(normalizedDir.scale(0.1));
        Vec3d destination = fromPos.add(normalizedDir.scale(Math.max(0.1, dist - 0.2)));
        RayTraceResult trace = world.rayTraceBlocks(origin, destination, false, true, false);
        return trace == null || trace.typeOfHit == RayTraceResult.Type.MISS;
    }

    private static BlockPos findCoverPosition(EntityLiving mob, EntityLivingBase target, double weaponRange) {
        World world = mob.world;
        BlockPos mobPos = mob.getPosition();
        BlockPos targetPos = target.getPosition();
        int searchRadius = (int) Math.min(weaponRange, 20.0D);
        double maxDistSq = weaponRange * weaponRange;

        Vec3d targetEye    = target.getPositionVector().add(0, target.getEyeHeight(), 0);
        Vec3d targetFeet   = target.getPositionVector().add(0, 0.1, 0);

        BlockPos bestCover = null;
        double minCost = Double.MAX_VALUE;

        FactionAStar astar = new FactionAStar(world, mob);

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                int x = mobPos.getX() + dx;
                int z = mobPos.getZ() + dz;
                if (x == targetPos.getX() && z == targetPos.getZ()) continue;

                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos candidate = new BlockPos(x, mobPos.getY() + dy, z);

                    if (candidate.distanceSq(targetPos) > maxDistSq) continue;
                    if (!isWalkable(world, candidate)) continue;

                    Vec3d candidateFeet = new Vec3d(candidate.getX() + 0.5, candidate.getY() + 0.1, candidate.getZ() + 0.5);
                    Vec3d candidateEye  = new Vec3d(candidate.getX() + 0.5, candidate.getY() + mob.getEyeHeight(), candidate.getZ() + 0.5);

                    boolean targetSeesMobFeet = hasLineOfSight(world, targetEye, candidateFeet);
                    if (targetSeesMobFeet) continue;

                    boolean mobSeesTarget = hasLineOfSight(world, candidateEye, targetFeet);
                    if (!mobSeesTarget) continue;

                    net.minecraft.pathfinding.Path path = astar.findPath(mobPos, candidate);
                    if (path == null || path.getCurrentPathLength() == 0) {
                        continue;
                    }

                    int pathLength = path.getCurrentPathLength();
                    double distToTarget = Math.sqrt(candidate.distanceSq(targetPos));
                    double distFromMob  = Math.sqrt(candidate.distanceSq(mobPos));
                    double cost = pathLength + distFromMob * 0.5 - distToTarget * 0.1;

                    if (cost < minCost) {
                        minCost = cost;
                        bestCover = candidate;
                    }
                }
            }
        }

        return bestCover;
    }

    private static boolean isCoverValid(EntityLiving mob, EntityLivingBase target, BlockPos coverPos, double weaponRange) {
        if (target == null || target.isDead || !target.isEntityAlive()) return false;
        if (!isWalkable(mob.world, coverPos)) return false;
        if (coverPos.distanceSq(target.getPosition()) > weaponRange * weaponRange) return false;
        Vec3d targetEye = target.getPositionVector().add(0, 1.5, 0);
        Vec3d targetBottom = target.getPositionVector().add(0, 0.5, 0);
        Vec3d coverBottom = new Vec3d(coverPos.getX() + 0.5, coverPos.getY() + 0.5, coverPos.getZ() + 0.5);
        Vec3d coverEye = new Vec3d(coverPos.getX() + 0.5, coverPos.getY() + 1.5, coverPos.getZ() + 0.5);
        if (hasLineOfSight(mob.world, targetEye, coverBottom)) return false;
        return hasLineOfSight(mob.world, coverEye, targetBottom);
    }

    private static boolean isWalkable(World world, BlockPos pos) {
        return world.isAirBlock(pos) && world.isAirBlock(pos.up()) && world.isSideSolid(pos.down(), EnumFacing.UP);
    }

    private static void clearStoredCover(NBTTagCompound susyTag, NBTTagCompound tag) {
        susyTag.removeTag(TAG_COVER_X);
        susyTag.removeTag(TAG_COVER_Y);
        susyTag.removeTag(TAG_COVER_Z);
        susyTag.removeTag("wasInCover");
        tag.setTag(TAG_ROOT, susyTag);
    }

    private static String getFaction(EntityLivingBase entity) {
        NBTTagCompound tag = entity.getEntityData();
        if (!tag.hasKey(TAG_ROOT)) return "";
        NBTTagCompound susyTag = tag.getCompoundTag(TAG_ROOT);
        if (!susyTag.hasKey(TAG_FACTION)) return "";
        return susyTag.getString(TAG_FACTION);
    }

    private static boolean hasSmartAI(EntityLivingBase entity) {
        NBTTagCompound tag = entity.getEntityData();
        if (!tag.hasKey(TAG_ROOT)) return false;
        return tag.getCompoundTag(TAG_ROOT).getBoolean(TAG_SMART_AI);
    }

    private static void clearNavigatorPath(EntityLiving mob) {
        mob.getNavigator().clearPath();
    }
}
